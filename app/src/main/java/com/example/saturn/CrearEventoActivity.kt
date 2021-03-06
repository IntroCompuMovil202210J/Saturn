package com.example.saturn

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.UiModeManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.saturn.chat.chatsActivity
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_evento.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class CrearEventoActivity : AppCompatActivity() {

    private val PATH_EVENT:String ="events/"
    private var GALERY_CODE: Int = 113
    private val PATH_EVENT_IMGES:String ="images/events/"
    private val PATH_EVENT_PARTICIPANTS:String="participants/"
    private lateinit var mStorageRef : StorageReference
    private lateinit var storage: FirebaseStorage
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var imageUri: Uri


    private lateinit var nameGame:TextView
    private lateinit var description :TextView
    private lateinit var participants :TextView
    private lateinit var date : String
    private lateinit var plataform : Spinner
    private lateinit var btnBuscarImg: Button
    private lateinit var btnRgstr:Button
    private lateinit var imgP : ImageView


    private lateinit var month: String
    private lateinit var day: String
    private lateinit var year: String
    private lateinit var datePicker : DatePicker
    private val today = Calendar.getInstance()

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
            AskPermissionLocation()
        }
    }

    private fun checkPermission(): Boolean{
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
    }

    private fun AskPermissionLocation(){
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
        setContentView(R.layout.activity_crear_evento)


        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val btnhome = findViewById<ImageButton>(R.id.arcade)

        var btncancelar = findViewById<Button>(R.id.back)

        datePicker = findViewById(R.id.datePicker)
        btnRgstr = findViewById(R.id.next)
        btnBuscarImg= findViewById(R.id.buscarIma)
        plataform =findViewById(R.id.plataformas)
        participants = findViewById(R.id.editParticipantes)
        description = findViewById(R.id.editDescripcion)
        nameGame = findViewById(R.id.editNombre)
        imgP = findViewById(R.id.imageP)
        mAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        mStorageRef = storage.reference
        database = FirebaseDatabase.getInstance()



        datePickerInitializr()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val policy : StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getLastLocation()


        btnBuscarImg.setOnClickListener{
            AskPermission()
            selectIamge()
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

        btncancelar.setOnClickListener{
            var intent = Intent(this, homeActivity::class.java)
            startActivity(intent);
        }

        btnRgstr.setOnClickListener{
            if(revizar()){
                var monthNum = datePicker.month + 1
                month = monthNum.toString()
                day = datePicker.dayOfMonth.toString()
                year = datePicker.year.toString()
                date=day+"/"+month+"/"+year
                registrarEvento()

            }
        }
    }


    private fun selectIamge(){
        var pickImage = Intent(Intent.ACTION_PICK)
        pickImage.setType("image/*")

        if(intent.resolveActivity(packageManager)!= null){
            startActivityForResult(pickImage,GALERY_CODE)
        }else{
            val toast = Toast.makeText(this,"Unable to open galery", Toast.LENGTH_SHORT).show()
        }
    }

    private fun datePickerInitializr(){

        datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)) { view, yearNumber, monthNumber, dayNumber ->
            var monthNum = monthNumber + 1
            month = monthNum.toString()
            day = dayNumber.toString()
            year=yearNumber.toString()
        }
    }

    private fun revizar():Boolean {
        if (!nameGame.text.isEmpty()) {
            if (!description.text.isEmpty()) {
                if (!participants.text.isEmpty() && participants.text != "0") {
                    var monthNum = datePicker.month + 1
                    month = monthNum.toString()
                    day = datePicker.dayOfMonth.toString()
                    year = datePicker.year.toString()
                    if (Integer.parseInt(month) >= today.get(Calendar.MONTH) && Integer.parseInt(day) >= today.get(Calendar.DAY_OF_MONTH) && Integer.parseInt(year) >= today.get(Calendar.YEAR)
                    ) {
                        return true
                    } else {
                        val toast = Toast.makeText(
                            this,
                            "Ingresa una fecha valida por favor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    participants.error = "Requerido"
                }
             } else {
                description.error = "Requerido"
            }
        }else{
            nameGame.error = "Requerido"
        }
        return false
    }


    private fun registrarEvento(){

            var user : FirebaseUser? = mAuth.currentUser
            var mEvento:Evento = Evento()

            mEvento.nombre=nameGame.text.toString()
            mEvento.descripcion=description.text.toString()
            mEvento.fecha=date
            mEvento.participantes=Integer.parseInt(participants.text.toString())
            mEvento.plataforma = plataform.selectedItem.toString()

            var imageUID:String = UUID.randomUUID().toString()

            var ref:StorageReference = mStorageRef.child(PATH_EVENT_IMGES+imageUID)
            ref.putFile(imageUri)
            mEvento.imageUri=imageUID
            mEvento.ownerUID = user?.uid

            val intent = Intent(this,seleccionarLugarActivity::class.java)
            intent.putExtra("evento",mEvento)
            startActivity(intent)
    }


    private fun AskPermission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                var builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    .setTitle("Gallery permission")
                    .setMessage("Se solicita permiso para poder tener acceso a su galeria")
                    .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                        ActivityCompat.requestPermissions(this, Array(1) { android.Manifest.permission.READ_EXTERNAL_STORAGE }, GALERY_CODE )
                    }
                    .setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
                        var i = Intent(this, MainActivity::class.java)
                        startActivity(i)
                    }
                builder.show()
            }else{
                ActivityCompat.requestPermissions(this,Array(1){android.Manifest.permission.READ_EXTERNAL_STORAGE},GALERY_CODE)
            }
        }else{
            val toast = Toast.makeText(this,"you already granted permissons",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            GALERY_CODE->
                if(resultCode== RESULT_OK){
                    try{
                        imageUri = data?.data as Uri
                        var imageStream = contentResolver.openInputStream(imageUri)
                        var selectedImage = BitmapFactory.decodeStream(imageStream)
                        imgP.setImageBitmap(selectedImage)
                    }catch( e: FileNotFoundException){
                        var toast= Toast.makeText(this,"File not found",Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

}