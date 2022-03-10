package com.example.saturn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var btnregistro = findViewById<Button>(R.id.registrarse)
        var btnInicio = findViewById<Button>(R.id.inicio)

        btnregistro.setOnClickListener{
            var intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent);
        }

        btnInicio.setOnClickListener(){
            var intent = Intent(this, logActivity::class.java)
            startActivity(intent);
        }

    }

}