package com.example.saturn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth

class perfilActivity : AppCompatActivity() {

    private lateinit var btnmap: ImageButton
    private lateinit var btnchats:ImageButton
    private lateinit var btnhome:ImageButton
    private lateinit var mAuth:FirebaseAuth
    private lateinit var email : String
    private lateinit var logOut : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        btnmap = findViewById(R.id.mapa)
        btnchats = findViewById(R.id.chats)
        btnhome= findViewById(R.id.arcade)
        mAuth = FirebaseAuth.getInstance()
        email = intent.getStringExtra("email").toString()
        logOut = findViewById(R.id.logoutbtn)


        btnhome.setOnClickListener(){
            val intent = Intent(this, homeActivity::class.java)
            startActivity(intent);
        }

        btnchats.setOnClickListener(){
            val intent = Intent(this, chatsActivity::class.java)
            startActivity(intent);
        }

        btnmap.setOnClickListener(){
            val intent = Intent(this, mapActivity::class.java)
            startActivity(intent);
        }

        logOut.setOnClickListener(){
            mAuth.signOut()
            val toMain = Intent(this, MainActivity::class.java)
            toMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(toMain)
        }

    }
}