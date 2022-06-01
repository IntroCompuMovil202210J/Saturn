package com.example.saturn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class perfilActivity : AppCompatActivity() {

    private val PATH_USERS:String ="users/"
    private val PATH_IMGES:String ="images/profile/"
    private lateinit var vel : ValueEventListener
    private lateinit var myRef: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var btnmap: ImageButton
    private lateinit var btnchats:ImageButton
    private lateinit var btnhome:ImageButton
    private lateinit var mAuth:FirebaseAuth
    private lateinit var email : String
    private lateinit var logOut : Button
    private lateinit var picture : ImageView
    private lateinit var nameLay : TextView
    private lateinit var nickLay : TextView
    private lateinit var plataform : TextView
    private lateinit var storageReference : StorageReference
    private lateinit var myEvents:Button
    private lateinit var registeredEvents : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        initializr()

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

        myEvents.setOnClickListener(){
            val intent = Intent(this,mostrarEventosActivity::class.java)
            intent.putExtra("owner",true)
            startActivity(intent)

        }

        registeredEvents.setOnClickListener(){
            val intent = Intent(this,mostrarEventosActivity::class.java)
            intent.putExtra("owner",false)
            startActivity(intent)
        }

    }

    private fun connect(){
        vel = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(single : DataSnapshot in snapshot.children ){
                    var usuario : Usuario? = single.getValue(Usuario::class.java)
                    if (usuario != null && usuario.email == mAuth.currentUser?.email ) {
                        storageReference.child("images/profile/"+usuario.imageUri).downloadUrl.addOnSuccessListener {
                            Glide.with(baseContext).load(it).error(R.drawable.avatar).into(picture)
                        }
                        nameLay.text=usuario.nombre
                        nickLay.text=usuario.nickname
                        plataform.text = usuario.plataformas
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun initializr(){
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference(PATH_USERS)
        btnmap = findViewById(R.id.mapa)
        picture = findViewById(R.id.perfil)
        btnchats = findViewById(R.id.chats)
        btnhome= findViewById(R.id.arcade)
        mAuth = FirebaseAuth.getInstance()
        email = intent.getStringExtra("email").toString()
        logOut = findViewById(R.id.logoutbtn)
        nameLay = findViewById(R.id.Nom)
        nickLay = findViewById(R.id.nick)
        plataform = findViewById(R.id.plataform)
        storageReference = FirebaseStorage.getInstance().reference
        myEvents= findViewById(R.id.eventos)
        registeredEvents = findViewById(R.id.participante)

        connect()
    }

    override fun onPause() {
        super.onPause()
        if(myRef!=null){
            myRef.removeEventListener(vel)
        }
    }

}