package com.example.saturn

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class seleccionarLugarActivity : AppCompatActivity() {

    private lateinit var evento : Evento

    private val PATH_EVENT:String ="events/"

    private val PATH_EVENT_PARTICIPANTS:String="participants/"
    private lateinit var mStorageRef : StorageReference
    private lateinit var storage: FirebaseStorage
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var mAuth : FirebaseAuth

    private lateinit var mapa : MapView
    private lateinit var sensorManager : SensorManager
    private var lightSensor : Sensor? =null
    private lateinit var roadManager : RoadManager
    private lateinit var mGeocoder: Geocoder
    private var destiny : Marker? = null

    private lateinit var btnRgstr : Button
    private lateinit var lugar : EditText
    private lateinit var bundle : Bundle

    private var lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                if(event.values[0]<10000){
                    mapa.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                }else if(event.values[0]>=10000){
                    mapa.overlayManager.tilesOverlay.setColorFilter(null)
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_lugar)

        mAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        mStorageRef = storage.reference
        database = FirebaseDatabase.getInstance()
        roadManager = OSRMRoadManager(this,"ANDROID")
        mGeocoder = Geocoder(baseContext)
        lugar = findViewById(R.id.editText)
        btnRgstr = findViewById(R.id.agregar)

        bundle = intent.extras!!

        setMap()
        val policy : StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        btnRgstr.setOnClickListener{
            registrarEvento()
        }
        mapa.overlays.add(createOverlayEvents())
        lugar.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEND){
                searchPlace(lugar.text.toString())
                true
            } else {
                false
            }
        }


    }

    private fun createOverlayEvents(): MapEventsOverlay {

        return MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                longPressOnMap(p)
                return true
            }
        })

    }

    private fun longPressOnMap(p : GeoPoint){
        if(destiny != null){
            mapa.overlays.remove(destiny)
        }
        destiny=setMarker(p)
        mapa.overlays.add(destiny)
        mapa.invalidate()
    }

    private fun searchPlace(lugar : String){
        if(!lugar.isEmpty()){
            try{
                val addresses : List<Address> = mGeocoder.getFromLocationName(lugar,2)
                if(!addresses.isEmpty()){
                    val result : Address = addresses.get(0)
                    var position : GeoPoint = GeoPoint(result.latitude,result.longitude)
                    if(mapa != null){
                        if(destiny != null){
                            mapa.overlays.remove(destiny)
                        }
                        destiny=setMarker(position)
                        mapa.overlays.add(destiny)
                    }
                }else{
                    var toast = Toast.makeText(this,"Direccion no encontrado", Toast.LENGTH_SHORT).show()
                }

            }catch(e: IOException){
                var toast = Toast.makeText(this,"La direccion esta vacia", Toast.LENGTH_SHORT).show()
            }
        }
        var toast = Toast.makeText(this,"Escriba una direccion", Toast.LENGTH_SHORT).show()
    }

    private fun setMarker(place : GeoPoint):Marker{
        val mapViewController = mapa.controller
        var marker = Marker(mapa)
        var address : List<Address> = mGeocoder.getFromLocation(place.latitude,place.longitude,2)
        marker.title = address[0].getAddressLine(0)+", "+address[0].subLocality
        marker.position=place
        marker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM)
        mapViewController.setZoom(19.0)
        mapViewController.setCenter(place)


        return marker
    }

    private fun setMap(){
        val ctx= applicationContext
        org.osmdroid.config.Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        mapa = findViewById(R.id.osmMap)
        mapa.setTileSource(TileSourceFactory.MAPNIK)
        mapa.isClickable = true
        mapa.setBuiltInZoomControls(true)
        mapa.setMultiTouchControls(true)
        mapa.setUseDataConnection(true)
        modeManage()
    }

    private fun modeManage(){
        val uiManager : UiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uiManager.nightMode == UiModeManager.MODE_NIGHT_YES ){
            mapa.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
    }

    private fun registrarEvento(){
        var participantes : ArrayList<String> = ArrayList<String> ()


        var user : FirebaseUser? = mAuth.currentUser
        var mEvento:Evento = bundle.get("evento") as Evento

        mEvento.lat=destiny?.position?.latitude
        mEvento.lon = destiny?.position?.longitude


        myRef=database.getReference(PATH_EVENT)
        var keyAuth: String? = myRef.push().key
        mEvento.participantesUID=keyAuth
        myRef=database.getReference(PATH_EVENT+keyAuth)
        myRef.setValue(mEvento)
        myRef=database.getReference(PATH_EVENT_PARTICIPANTS+keyAuth)
        participantes.add(user?.uid.toString())
        myRef.setValue(participantes)

        val intent = Intent(this,homeActivity::class.java)
        startActivity(intent)

    }
}