package com.example.saturn


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.R.drawable
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.saturn.chat.chatsActivity
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import org.osmdroid.util.GeoPoint
import java.time.LocalDate


class homeActivity : AppCompatActivity() {

    private val PATH_EVENTS:String ="events/"


    private lateinit var context :Context
    lateinit var sensorManager: SensorManager;
    private  var proximitySensor : Sensor? =null

    private lateinit var EventList : ArrayList<Evento>

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var vel : ValueEventListener

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

        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)|| locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_MAP_PERMMISION){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val toast = Toast.makeText(this,"ya se tiene permisos",Toast.LENGTH_SHORT ).show()
            }
        }
    }



    private var proximitySensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            var layout = findViewById<FrameLayout>(R.id.frame1)
            var layout2 = findViewById<FrameLayout>(R.id.frame2)
            if (event != null) {
                if (event.values[0] < 4) {
                    println("Proximidad de:" + event.values[0])
                    layout.setBackgroundColor(Color.BLACK)
                    layout2.setVisibility(View.INVISIBLE)


                } else if (event.values[0] >= 4) {
                    println("Proximidad de:" + event.values[0])
                    layout.setBackgroundResource(R.drawable.listgrad)
                    layout2.setVisibility(View.VISIBLE)
                }

            }
        }
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val policy : StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val btnagregar = findViewById<ImageButton>(R.id.agregar)

        getLastLocation()
        connect()

        btnperfil.setOnClickListener{
            if(myRef!=null){
                myRef.removeEventListener(vel)
            }
            var intent = Intent(this, perfilActivity::class.java)
            startActivity(intent);
        }

        btnchats.setOnClickListener{
            if(myRef!=null){
                myRef.removeEventListener(vel)
            }
            var intent = Intent(this, chatsActivity::class.java)
            intent.putExtra("user",mAuth.currentUser?.email.toString())
            startActivity(intent);
        }

        btnmap.setOnClickListener{
            if(myRef!=null){
                myRef.removeEventListener(vel)
            }
            var intent = Intent(this, mapActivity::class.java)
            startActivity(intent);
        }

        btnagregar.setOnClickListener{
            if(myRef!=null){
                myRef.removeEventListener(vel)
            }
            var intent = Intent(this, CrearEventoActivity::class.java)
            startActivity(intent);
        }

        lista.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this,EventoActivity::class.java)
            intent.putExtra("evento",EventList[position])
            startActivity(intent)
        }

    }

    @SuppressLint("Range")
    fun connect(){
        EventList = ArrayList()

        myRef = database.getReference(PATH_EVENTS)
        vel = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                EventList.clear()
                for(single : DataSnapshot in snapshot.children ){
                    var evento : Evento? = single.getValue(Evento::class.java)
                    if (evento != null && evento.participantes!! >=1 ) {
                        EventList.add(evento)
                    }
                }

                val adapter = EventosAdapter(this@homeActivity,EventList)
                lista.adapter=adapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(proximitySensorListener,proximitySensor,SensorManager.SENSOR_DELAY_FASTEST)
        connect()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(proximitySensorListener)

    }
}