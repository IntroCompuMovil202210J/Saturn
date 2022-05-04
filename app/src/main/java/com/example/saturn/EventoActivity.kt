package com.example.saturn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_evento.*

class EventoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento)

        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val btnhome = findViewById<ImageButton>(R.id.arcade)
        var btnregistro = findViewById<Button>(R.id.registrarseEvento)
        var cont = 0;

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
            if(cont == 0){ var toast = Toast.makeText(this,"Registrado exitosamente",Toast.LENGTH_SHORT).show()
                cont++
            }
            else{
                var toast = Toast.makeText(this,"¡Ya estás registrado en este evento!",Toast.LENGTH_SHORT).show()
            }

        }

        val evento = intent.getSerializableExtra("evento") as Evento

        nombre.text = evento.nombre
        descripcion.text = evento.descripcion
        plataforma.text = evento.plataforma
        fecha.text = evento.fecha.toString()
        participantes.text = evento.participantes.toString()
        imagen.setImageResource(evento.imagen)
    }
}