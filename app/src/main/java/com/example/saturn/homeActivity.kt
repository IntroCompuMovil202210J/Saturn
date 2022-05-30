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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
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


        val policy : StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val btnagregar = findViewById<ImageButton>(R.id.agregar)

        connect()

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

        btnagregar.setOnClickListener{
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
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(proximitySensorListener)
    }
}