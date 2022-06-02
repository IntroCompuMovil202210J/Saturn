package com.example.saturn

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.UiModeManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.saturn.chat.chatsActivity
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import java.io.IOException
import kotlin.collections.ArrayList

class mapActivity : AppCompatActivity() {

    private val PATH_USERS:String ="users/"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var localRequest : LocationRequest
    val LOCATION_MAP_PERMMISION : Int = 114
    private lateinit var mapa : MapView
    private lateinit var sensorManager : SensorManager
    private var lightSensor : Sensor? =null
    private lateinit var roadManager : RoadManager
    private lateinit var mGeocoder: Geocoder
    private var destiny : Marker? = null
    private var newMarker : Marker? = null
    private var userMarker: Marker? = null
    private var roadOverlay : Polyline? = null
    private lateinit var mAuth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var user : FirebaseUser
    private lateinit var fab : View
    private var initial :Boolean = true
    private lateinit var bundle : Bundle

    private var lightSensorListener = object : SensorEventListener{
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

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            var lastLocation: Location = p0.lastLocation
            var place =GeoPoint(lastLocation.latitude, lastLocation.longitude, lastLocation.altitude)
            var keyAuth: String? = user.uid

            if (newMarker != null) {
                mapa.overlays.remove(newMarker)
            }
            newMarker = setMarker(place);
            mapa.overlays.add(newMarker)

            myRef=database.getReference(PATH_USERS+keyAuth)

            myRef.addListenerForSingleValueEvent( object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    var mUser= snapshot.getValue(Usuario::class.java)
                    if(mUser != null) {
                        mUser.lat = newMarker?.position?.latitude
                        mUser.lon = newMarker?.position?.longitude
                        myRef.setValue(mUser)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            if(destiny!=null && userMarker == null){
                drawRoute(newMarker?.position!!,destiny?.position!!)
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val btnhome = findViewById<ImageButton>(R.id.arcade)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)

        fab = findViewById(R.id.fab)
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!
        fusedLocationClient =LocationServices.getFusedLocationProviderClient(this)
        roadManager = OSRMRoadManager(this,"ANDROID")
        mGeocoder = Geocoder(baseContext)



        sensorStuff()
        setMap()
        getLastLocation()
        val policy :StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy)

        mapa.overlays.add(createOverlayEvents())

        if(intent.hasExtra("destinoLat")&&intent.hasExtra("destinoLon")){
            bundle= intent.extras!!
            var place : GeoPoint = GeoPoint(bundle?.get("destinoLat") as Double,bundle.get("destinoLon")as Double)
            buscarDestino(place)
        }
        if(intent.hasExtra("usuarioEmail")){
            bundle= intent.extras!!
            var emailUsu : String = bundle.get("usuarioEmail") as String
            buscarUsuaio(emailUsu)
        }

        btnperfil.setOnClickListener{
            mapa.onCancelPendingInputEvents()
            mapa.onPause()
            if (fusedLocationClient!= null) {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
            sensorManager.unregisterListener(lightSensorListener)
            val intent = Intent(this, perfilActivity::class.java)
            startActivity(intent);
        }

        btnchats.setOnClickListener{
            val intent = Intent(this, chatsActivity::class.java)
            intent.putExtra("user",mAuth.currentUser?.email.toString())
            startActivity(intent);
        }

        btnhome.setOnClickListener{

            val intent = Intent(this, homeActivity::class.java)
            startActivity(intent);
        }

        fab.setOnClickListener{

            val mapViewController = mapa.controller
            mapViewController.setZoom(19.0)
            mapViewController.setCenter(newMarker?.position )
        }
    }

    private fun buscarDestino(p : GeoPoint){
        if(destiny != null){
            mapa.overlays.remove(destiny)
        }
        destiny=setMarker(p)
        mapa.overlays.add(destiny)
        mapa.invalidate()

    }

    private fun buscarUsuaio(email: String){

        myRef=database.getReference(PATH_USERS)

        myRef.addValueEventListener( object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(single:DataSnapshot in snapshot.children){
                    var mUser= single.getValue(Usuario::class.java)
                    if(mUser != null && mUser.email == email) {
                        if(userMarker!= null){
                            mapa.overlays.remove(userMarker)
                        }
                        var newPoint : GeoPoint = GeoPoint(mUser.lat as Double,mUser.lon as Double)
                        userMarker = setMarker(newPoint)
                        mapa.overlays.add(userMarker)
                        drawRoute(newPoint,destiny?.position!!)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

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
        drawRoute(destiny!!.position,newMarker!!.position)
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
                        drawRoute(destiny!!.position,newMarker!!.position)
                    }
                }else{
                    var toast = Toast.makeText(this,"Direccion no encontrado",Toast.LENGTH_SHORT).show()
                }

            }catch(e:IOException){
                var toast = Toast.makeText(this,"La direccion esta vacia",Toast.LENGTH_SHORT).show()
            }
        }
        var toast = Toast.makeText(this,"Escriba una direccion",Toast.LENGTH_SHORT).show()
    }


    private fun sensorStuff(){
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onPause() {
        super.onPause()
        mapa.onPause()
        sensorManager.unregisterListener(lightSensorListener)

    }

    override fun onResume() {
        super.onResume()
        mapa.onResume()
        sensorManager.registerListener(lightSensorListener,lightSensor,SensorManager.SENSOR_DELAY_NORMAL)
        modeManage()
        getLastLocation()

    }

    private fun drawRoute(start:GeoPoint,finish : GeoPoint){
        var routePoints : ArrayList<GeoPoint> = ArrayList<GeoPoint>()
        routePoints.add(start)
        routePoints.add(finish)
        var road : Road = roadManager.getRoad(routePoints)
        if(mapa!=null){
            if(roadOverlay!=null){
                mapa.overlays.remove(roadOverlay)
            }

            roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay!!.outlinePaint.strokeWidth=10F
            roadOverlay!!.outlinePaint.setColor(Color.RED)
            mapa.overlays.add(roadOverlay)
        }

    }


    private fun modeManage(){
        val uiManager : UiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uiManager.nightMode == UiModeManager.MODE_NIGHT_YES ){
            mapa.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
    }

    private fun setMap(){
        val ctx= applicationContext
        org.osmdroid.config.Configuration.getInstance().load(ctx,PreferenceManager.getDefaultSharedPreferences(ctx))

        mapa = findViewById(R.id.osmMap)
        mapa.setTileSource(MAPNIK)
        mapa.isClickable = true
        mapa.setBuiltInZoomControls(true)
        mapa.setMultiTouchControls(true)
        mapa.setUseDataConnection(true)
        modeManage()
    }

    private fun getLastLocation(){

        if(checkPermission()){
            if(LocationEnable()){
                fusedLocationClient.lastLocation.addOnCompleteListener {
                    getLocation()
                }
            }else{
                val toast = Toast.makeText(this,"Porfavor active la localizacion",Toast.LENGTH_SHORT).show()
            }
        }else{
            AskPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(){
        localRequest = LocationRequest.create().apply {
            this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            this.interval=100
            this.fastestInterval=3000
            this.setMaxWaitTime(1000)
        }
        fusedLocationClient.requestLocationUpdates(localRequest,locationCallback, Looper.myLooper()!! )

    }

    private fun setMarker(place : GeoPoint):Marker{
        val mapViewController = mapa.controller
        var marker = Marker(mapa)
        var address : List<Address> = mGeocoder.getFromLocation(place.latitude,place.longitude,2)
        marker.title = address[0].getAddressLine(0)+", "+address[0].subLocality
        marker.position=place
        marker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM)
        if(initial){
            mapViewController.setZoom(19.0)
            mapViewController.setCenter(place)
            initial=false;
        }

        return marker
    }



    private fun checkPermission(): Boolean{
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
    }

    private fun AskPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
            var builder: AlertDialog.Builder = AlertDialog.Builder(this)
                .setTitle("Acces location permission")
                .setMessage("Se solicita permiso para poderacceder a su localización")
                .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                    ActivityCompat.requestPermissions(this, Array(1) { android.Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_MAP_PERMMISION)
                }
                .setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
                    var i = Intent(this, MainActivity::class.java)
                    startActivity(i)
                }
            builder.show()
        }else{
            ActivityCompat.requestPermissions(this,Array(1){android.Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_MAP_PERMMISION)
        }
    }


    private fun LocationEnable():Boolean{

        var locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_MAP_PERMMISION){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val toast = Toast.makeText(this,"ya se tiene permisos",Toast.LENGTH_SHORT ).show()
            }
        }
    }
}