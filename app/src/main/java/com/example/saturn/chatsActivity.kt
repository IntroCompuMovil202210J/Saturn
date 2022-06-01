package com.example.saturn

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.osmdroid.util.GeoPoint


class chatsActivity : AppCompatActivity() {

    val LOCATION_MAP_PERMMISION : Int = 114
    private val PATH_USERS:String ="users/"
    private lateinit var user : FirebaseUser
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var localRequest : LocationRequest
    private lateinit var mAuth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference

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
                val toast = Toast.makeText(this,"ya se tiene permisos", Toast.LENGTH_SHORT ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)


        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnhome = findViewById<ImageButton>(R.id.arcade)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val text = findViewById<TextView>(R.id.DavidG)
        val imagenPerfil = findViewById<ImageView>(R.id.imageView8)
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val policy : StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getLastLocation()

        btnperfil.setOnClickListener{
            var intent = Intent(this, perfilActivity::class.java)
            startActivity(intent);
        }

        btnhome.setOnClickListener{
            var intent = Intent(this, homeActivity::class.java)
            startActivity(intent);
        }

        btnmap.setOnClickListener{
            var intent = Intent(this, mapActivity::class.java)
            startActivity(intent);
        }

        text.setOnClickListener(){
            var intent = Intent(this, conversacionActivity::class.java)
            startActivity(intent);

        }
        imagenPerfil.setOnClickListener(){
            var intent = Intent(this, conversacionActivity::class.java)
            startActivity(intent);

        }

    }
}