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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_home.*
import java.time.LocalDate


class homeActivity : AppCompatActivity() {

    private lateinit var context :Context
    lateinit var sensorManager: SensorManager;
    private  var proximitySensor : Sensor? =null


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

        val policy : StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val btnagregar = findViewById<ImageButton>(R.id.agregar)

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

        val Evento1 = Evento("Mario Kart", "Regístrate para jugar Mario Kart Tour","Nintendo",LocalDate.of(2022, 5, 5),10, R.drawable.download)
        val Evento2 = Evento("League of Legends", "Regístrate para jugar LOL","PC", LocalDate.of(2022, 5, 5) ,12, R.drawable.leagueoflegends)
        val Evento3 = Evento("Clash Royale", "Regístrate para jugar Clash Royale","Mobile", LocalDate.of(2022, 5, 6) ,8, R.drawable.clashroyale)
        val Evento4 = Evento("Call of Duty", "Regístrate para jugar COD","PlayStation", LocalDate.of(2022, 5, 7) ,15, R.drawable.callofduty)
        val Evento5 = Evento("Fornite", "Regístrate para jugar Fornite","PlayStation", LocalDate.of(2022, 5, 5) ,4, R.drawable.fornite)

        val listaEventos = listOf(Evento1,Evento2,Evento3, Evento4,Evento5)

        val adapter = EventosAdapter(this,listaEventos)

        lista.adapter = adapter

        lista.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this,EventoActivity::class.java)
            intent.putExtra("evento",listaEventos[position])
            startActivity(intent)
        }

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