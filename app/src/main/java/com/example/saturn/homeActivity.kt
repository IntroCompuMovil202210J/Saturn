package com.example.saturn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_home.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


class homeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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
}