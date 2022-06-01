package com.example.saturn

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_evento.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_participantes.*
import kotlinx.android.synthetic.main.activity_participantes.lista

class participantesActivity : AppCompatActivity() {

    private val PATH_USERS:String ="users/"
    private val PATH_EVENT_PARTICIPANTS:String="participants/"
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var vel : ValueEventListener
    private lateinit var contactList : ArrayList<Usuario>
    private lateinit var evento:Evento
    private lateinit var participantesUID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_participantes)
        val bundle : Bundle? = intent.extras
        evento = bundle?.get("evento") as Evento
        participantesUID = bundle?.get("participantes") as String
        connect()

        lista.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this,mapActivity::class.java)
            intent.putExtra("usuarioEmail",contactList[position].email)
            intent.putExtra("destinoLat", evento.lat)
            intent.putExtra("destinoLon", evento.lon)
            startActivity(intent)
        }
    }

    @SuppressLint("Range")
    fun connect(){
        contactList = ArrayList()
        database = FirebaseDatabase.getInstance()
        var UID : String
        var UIDS : ArrayList<String> = ArrayList<String>()

        myRef = database.getReference(PATH_EVENT_PARTICIPANTS+participantesUID)
        vel = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for(single : DataSnapshot in snapshot.children ){
                        UID = single.getValue()as String
                        UIDS.add(UID)
                        var mySubRef : DatabaseReference = database.getReference(PATH_USERS+UID)
                        var vel1 = mySubRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                contactList.clear()
                                var usuario : Usuario? = snapshot.getValue(Usuario::class.java)
                                if (usuario != null ) {
                                    contactList.add(usuario)
                                }

                                val adapter = UsuariosAdapter(this@participantesActivity ,contactList)
                                lista.adapter=adapter
                            }
                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })


                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onPause() {
        super.onPause()
        if(myRef!=null){
            myRef.removeEventListener(vel)
        }
    }

    override fun onResume() {
        super.onResume()
        connect()
    }
}