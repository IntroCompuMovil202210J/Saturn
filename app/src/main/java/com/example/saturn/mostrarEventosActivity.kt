package com.example.saturn

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.lista
import kotlinx.android.synthetic.main.activity_participantes.*
import java.util.*
import kotlin.collections.ArrayList

class mostrarEventosActivity : AppCompatActivity() {

    private val PATH_EVENTS:String ="events/"
    private val PATH_EVENT_PARTICIPANTS:String="participants/"

    private lateinit var EventList : ArrayList<Evento>

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var vel : ValueEventListener
    private var owner:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mostrar_eventos)

        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val btnhome = findViewById<ImageButton>(R.id.arcade)
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()

        val bundle: Bundle? = intent?.extras
        owner = bundle?.get("owner") as Boolean

        connect()

        lista.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, EventoActivity::class.java)
            intent.putExtra("evento", EventList[position])
            intent.putExtra("owner",owner)
            startActivity(intent)
        }

        btnhome.setOnClickListener{
            var intent = Intent(this, homeActivity::class.java)
            startActivity(intent)
        }

        btnperfil.setOnClickListener{
            if(myRef!=null){
                myRef.removeEventListener(vel)
            }
            var intent = Intent(this, perfilActivity::class.java)
            startActivity(intent);
        }

        btnchats.setOnClickListener{
            if(myRef!=null){
                myRef.removeEventListener(vel)
            }
            var intent = Intent(this, chatsActivity::class.java)
            startActivity(intent);
        }

        btnmap.setOnClickListener{
            if(myRef!=null){
                myRef.removeEventListener(vel)
            }
            var intent = Intent(this, mapActivity::class.java)
            startActivity(intent);
        }
    }

    @SuppressLint("Range")
    private fun connect() {
        EventList = ArrayList()

        val user = mAuth.currentUser
        myRef = database.getReference(PATH_EVENTS)
        vel = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                EventList.clear()
                if(owner){

                    for (single: DataSnapshot in snapshot.children) {
                        var evento: Evento? = single.getValue(Evento::class.java)
                        if (evento != null && evento.ownerUID == user?.uid) {
                            EventList.add(evento)
                        }
                    }
                    val adapter = EventosAdapter(this@mostrarEventosActivity, EventList)
                    lista.adapter = adapter
                }else{
                    for (single: DataSnapshot in snapshot.children) {
                        var evento: Evento? = single.getValue(Evento::class.java)
                        if (evento != null && evento.ownerUID != user?.uid) {
                            val subRef = database.getReference(PATH_EVENT_PARTICIPANTS+evento.participantesUID)
                            var vel1 = subRef.addValueEventListener(object:ValueEventListener {
                                override fun onDataChange(subSnapshot: DataSnapshot) {
                                    if(subSnapshot.exists()){
                                        for(another : DataSnapshot in subSnapshot.children ){
                                            var UID = another.getValue()as String

                                            if(UID==user?.uid){
                                                EventList.add(evento)
                                            }
                                        }
                                    }
                                    val adapter = EventosAdapter(this@mostrarEventosActivity, EventList)
                                    lista.adapter = adapter
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })
                        }
                    }

                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}