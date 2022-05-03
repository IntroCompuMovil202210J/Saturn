package com.example.saturn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {


    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        mAuth = FirebaseAuth.getInstance();

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

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser!=null){
            val intent = Intent(this, homeActivity::class.java)
            intent.putExtra("user", mAuth.currentUser!!.email.toString())
            startActivity(intent);
        }
    }

}