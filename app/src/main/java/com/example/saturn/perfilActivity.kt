package com.example.saturn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class perfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnhome = findViewById<ImageButton>(R.id.arcade)

        btnhome.setOnClickListener(){
            var intent = Intent(this, homeActivity::class.java)
            startActivity(intent);
        }

        btnchats.setOnClickListener(){
            var intent = Intent(this, chatsActivity::class.java)
            startActivity(intent);
        }

        btnmap.setOnClickListener(){
            var intent = Intent(this, mapActivity::class.java)
            startActivity(intent);
        }

    }
}