package com.example.saturn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView


class chatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnhome = findViewById<ImageButton>(R.id.arcade)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val text = findViewById<TextView>(R.id.DavidG)
        val imagenPerfil = findViewById<ImageView>(R.id.imageView8)


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