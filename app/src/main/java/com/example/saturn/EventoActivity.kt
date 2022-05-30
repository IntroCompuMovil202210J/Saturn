package com.example.saturn

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_evento.*
import kotlinx.android.synthetic.main.activity_evento.view.*

class EventoActivity : AppCompatActivity() {

    private lateinit var storageReference : StorageReference

    private val PATH_USERS:String ="users/"
    private val PATH_EVENT_PARTICIPANTS:String="participants/"
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var vel : ValueEventListener
    private lateinit var contactList : ArrayList<Usuario>
    private lateinit var evento : Evento
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento)

        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val btnhome = findViewById<ImageButton>(R.id.arcade)
        val btnregistro = findViewById<Button>(R.id.registrarseEvento)

        val imgEvent = findViewById<ImageView>(R.id.imagen)
        val nameEvent = findViewById<TextView>(R.id.nombre)
        val dscrptnEvent = findViewById<TextView>(R.id.descripcion)
        val pltfrmEvent = findViewById<TextView>(R.id.plataforma)
        val dateEvent = findViewById<TextView>(R.id.fecha)

        storageReference = FirebaseStorage.getInstance().reference
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

        val cosa : Bundle? = intent.extras
         evento = cosa?.get("evento") as Evento



        val storageReference : StorageReference=FirebaseStorage.getInstance().reference
        storageReference.child("images/events/"+evento.imageUri).downloadUrl.addOnSuccessListener {
            Glide.with(this).load(it).error(R.drawable.avatar).into(imgEvent)
        }

        nameEvent.text=evento.nombre
        dscrptnEvent.text=evento.descripcion
        pltfrmEvent.text=evento.plataforma
        dateEvent.text=evento.fecha
        connect()



    }


    @SuppressLint("Range")
    fun connect(){
        contactList = ArrayList()
        database = FirebaseDatabase.getInstance()
        var UIDS : String?=null

        myRef = database.getReference(PATH_EVENT_PARTICIPANTS+evento.participantesUID)
        vel = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contactList.clear()
                for(single : DataSnapshot in snapshot.children ){
                    UIDS = single.getValue() as String

                    var mySubRef : DatabaseReference = database.getReference(PATH_USERS+UIDS)

                    var vel1 = mySubRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            var usuario : Usuario? = snapshot.getValue(Usuario::class.java)
                             if (usuario != null ) {
                                 contactList.add(usuario)
                             }

                        }
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })

                }
                val adapter = UsuariosAdapter(this@EventoActivity,contactList)
                lista.adapter=adapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }
}