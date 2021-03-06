package com.example.saturn

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.UiModeManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.preference.PreferenceManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.saturn.chat.chatsActivity
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_evento_especifico.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

class eventoEspecificoActivity : AppCompatActivity() {

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

    val LOCATION_MAP_PERMMISION : Int = 114
    private val PATH_USERS:String ="users/"
    private lateinit var user : FirebaseUser
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var localRequest : LocationRequest


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            user= mAuth.currentUser!!
            var lastLocation: Location = p0.lastLocation
            var place =
                GeoPoint(lastLocation.latitude, lastLocation.longitude, lastLocation.altitude)
            var keyAuth: String? = user.uid


            myRef = database.getReference(PATH_USERS + keyAuth)

            myRef.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    var mUser = snapshot.getValue(Usuario::class.java)
                    if (mUser != null) {
                        mUser.lat = place.latitude
                        mUser.lon = place.longitude
                        myRef.setValue(mUser)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
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

    private fun getLastLocation(){

        if(checkPermission()){
            if(LocationEnable()){
                fusedLocationClient.lastLocation.addOnCompleteListener {
                    getLocation()
                }
            }else{
                val toast = Toast.makeText(this,"Porfavor active la localizacion", Toast.LENGTH_SHORT).show()
            }
        }else{
            AskPermission()
        }
    }

    private fun checkPermission(): Boolean{
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
    }

    private fun AskPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
            var builder: AlertDialog.Builder = AlertDialog.Builder(this)
                .setTitle("Acces location permission")
                .setMessage("Se solicita permiso para poderacceder a su localizaci??n")
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

        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)|| locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_MAP_PERMMISION){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val toast = Toast.makeText(this,"ya se tiene permisos", Toast.LENGTH_SHORT ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento_especifico)

        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val btnhome = findViewById<ImageButton>(R.id.arcade)
        val btnCancelRgtr = findViewById<Button>(R.id.cancelarRegistro)
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val policy : StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getLastLocation()

        btnParticipants.setOnClickListener{
            var intent = Intent(this, participantesActivity::class.java)
            intent.putExtra("participantes",evento.participantesUID.toString())
            intent.putExtra("evento",evento)
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
            intent.putExtra("user",mAuth.currentUser?.email.toString())
            startActivity(intent);
        }

        btnmap.setOnClickListener{
            var intent = Intent(this, mapActivity::class.java)
            startActivity(intent);
        }

        btnCancelRgtr.setOnClickListener{
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

    private fun desRegistrar(){
        var user : FirebaseUser? = mAuth.currentUser


        myRef=database.getReference(PATH_EVENT_PARTICIPANTS+evento.participantesUID)

        myRef.addListenerForSingleValueEvent( object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                var contactList : ArrayList<String> = ArrayList<String>()

                if(snapshot.exists()){
                    for(single : DataSnapshot in snapshot.children ){
                        var UID = single.getValue()as String
                        if(UID != user?.uid) {
                            contactList.add(UID)
                        }
                    }
                    mEvento?.participantes = mEvento?.participantes?.plus(1)
                    var subRef = database.getReference(PATH_EVENTS+evento.participantesUID)

                    myRef.setValue(contactList)
                    subRef.setValue(mEvento)
                    var toast = Toast.makeText(this@eventoEspecificoActivity,"Listo, ya no estas registrado en este evento",Toast.LENGTH_SHORT).show()
                    var intent = Intent ( this@eventoEspecificoActivity,homeActivity::class.java)
                    startActivity(intent)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                var toast = Toast.makeText(this@eventoEspecificoActivity,"Lo sentimos,no se ha podido cancelar el registro",Toast.LENGTH_SHORT).show()
                var intent = Intent ( this@eventoEspecificoActivity,homeActivity::class.java)
                startActivity(intent)
            }
        })
    }

    private fun editarEvento(){

        myRef = database.getReference(PATH_EVENTS+evento.participantesUID)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                mEvento  = snapshot.getValue(Evento::class.java)!!
                if(evento.ownerUID != user.uid){
                    desRegistrar()
                }else{
                    var toast = Toast.makeText(this@eventoEspecificoActivity,"Lo sentimos, como creador del evento no puedes hacer eso",Toast.LENGTH_SHORT).show()
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