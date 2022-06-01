package com.example.saturn

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_evento.*
import kotlinx.android.synthetic.main.activity_evento.view.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_participantes.*
import kotlinx.android.synthetic.main.activity_participantes.lista
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

class EventoActivity : AppCompatActivity() {

    private lateinit var storageReference : StorageReference
    private lateinit var evento : Evento

    private lateinit var mapa : MapView
    private lateinit var sensorManager : SensorManager
    private var lightSensor : Sensor? =null
    private lateinit var mGeocoder: Geocoder
    private var destiny : Marker? = null

    private val PATH_EVENT:String ="events/"

    private val PATH_EVENT_PARTICIPANTS:String="participants/"
    private val PATH_EVENTS:String ="events/"
    private lateinit var mStorageRef : StorageReference
    private lateinit var storage: FirebaseStorage
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var mAuth : FirebaseAuth

    private lateinit var mEvento : Evento



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento)

        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val btnhome = findViewById<ImageButton>(R.id.arcade)
        val btnregistro = findViewById<Button>(R.id.registrarseEvento)
        val btnParticipants = findViewById<Button>(R.id.participantes)

        val imgEvent = findViewById<ImageView>(R.id.imagen)
        val nameEvent = findViewById<TextView>(R.id.nombre)
        val dscrptnEvent = findViewById<TextView>(R.id.descripcion)
        val pltfrmEvent = findViewById<TextView>(R.id.plataforma)
        val dateEvent = findViewById<TextView>(R.id.fecha)
        mGeocoder = Geocoder(baseContext)
        mAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()




        storageReference = FirebaseStorage.getInstance().reference
        var cont = 0;


        btnParticipants.setOnClickListener{
            var intent = Intent(this, participantesActivity::class.java)
            intent.putExtra("participantes",evento.participantesUID.toString())
            startActivity(intent);
        }
        btnhome.setOnClickListener{
            var intent = Intent(this, homeActivity::class.java)
            startActivity(intent);
        }

        btnperfil.setOnClickListener{
            var intent = Intent(this, perfilActivity::class.java)
            startActivity(intent);
        }

        btnchats.setOnClickListener{
            var intent = Intent(this, chatsActivity::class.java)
            startActivity(intent);
        }

        btnmap.setOnClickListener{
            var intent = Intent(this, mapActivity::class.java)
            startActivity(intent);
        }

        btnregistro.setOnClickListener{
            editarEvento()

        }

        val cosa : Bundle? = intent.extras
         evento = cosa?.get("evento") as Evento



        val storageReference : StorageReference=FirebaseStorage.getInstance().reference
        storageReference.child("images/events/"+evento.imageUri).downloadUrl.addOnSuccessListener {
            Glide.with(this).load(it).error(R.drawable.avatar).into(imgEvent)
        }

        nameEvent.text=evento.nombre
        dscrptnEvent.text=evento.descripcion
        pltfrmEvent.text=evento.plataforma
        dateEvent.text=evento.fecha

        sensorStuff()
        setMap()
    }

    private fun createOverlayEvents(): MapEventsOverlay {

        return MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                p.longitude = evento.lon!!
                p.latitude = evento.lat!!

                longPressOnMap(p)
                return true
            }
        })

    }

    private fun longPressOnMap(p : GeoPoint){

        var intent = Intent(this,mapActivity::class.java)
        intent.putExtra("destinoLat", p.latitude)
        intent.putExtra("destinoLon", p.longitude)
        startActivity(intent)
    }

    private fun sensorStuff(){
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onPause() {
        super.onPause()
        mapa.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapa.onResume()
        modeManage()

    }

    private fun modeManage(){
        val uiManager : UiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uiManager.nightMode == UiModeManager.MODE_NIGHT_YES ){
            mapa.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
    }

    private fun registrar(){
        var user : FirebaseUser? = mAuth.currentUser


        myRef=database.getReference(PATH_EVENT_PARTICIPANTS+evento.participantesUID)

        myRef.addListenerForSingleValueEvent( object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                var contactList : ArrayList<String> = ArrayList<String>()
                var yaEsta : Boolean = false
                if(snapshot.exists()){
                    for(single : DataSnapshot in snapshot.children ){
                        var UID = single.getValue()as String
                        if(UID != user?.uid) {
                            contactList.add(UID)
                        }else{
                            yaEsta=true
                        }
                    }
                    if(!yaEsta){
                        if (user != null) {
                            contactList.add(user.uid)
                            mEvento?.participantes = mEvento?.participantes?.minus(1)
                            var subRef = database.getReference(PATH_EVENTS+evento.participantesUID)

                            myRef.setValue(contactList)
                            subRef.setValue(mEvento)
                            var toast = Toast.makeText(this@EventoActivity,"Listo, se te ha registrado exitosamente en este evento",Toast.LENGTH_LONG).show()
                            var intent = Intent ( this@EventoActivity,homeActivity::class.java)
                            startActivity(intent)
                        }
                    }else{
                        var toast = Toast.makeText(this@EventoActivity,"usted ya se encuentra registrado para este evento",Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun editarEvento(){

      myRef = database.getReference(PATH_EVENTS+evento.participantesUID)
      myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                    mEvento  = snapshot.getValue(Evento::class.java)!!
                    if(Integer.parseInt(mEvento?.participantes!!.toString()) >0){

                        registrar()
                    }else{
                        var toast = Toast.makeText(this@EventoActivity,"Lo sentimos, ya no hay cupos disponibles en este evento",Toast.LENGTH_SHORT).show()
                    }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

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
        var eventLocation :GeoPoint= GeoPoint(evento.lat as Double,evento.lon as Double)
        destiny = setMarker(eventLocation)
        mapa.overlays.add(destiny)
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

}