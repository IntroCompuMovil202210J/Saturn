package com.example.saturn

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_evento.*
import java.util.*

class CrearEventoActivity : AppCompatActivity() {

    private val PATH_USERS:String ="users/"
    private val PATH_IMGES:String ="images/profile/"
    private lateinit var mStorageRef : StorageReference
    private lateinit var storage: FirebaseStorage
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var imageUri: Uri


    private lateinit var nameGame:TextView
    private lateinit var description :TextView
    private lateinit var participants :TextView
    private lateinit var date : String
    private lateinit var plataform : Spinner
    private lateinit var btnBuscarImg: Button
    private lateinit var btnCrear:Button


    private lateinit var month: String
    private lateinit var day: String
    private lateinit var year: String
    private lateinit var datePicker : DatePicker
    private val today = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_evento)


        val btnmap = findViewById<ImageButton>(R.id.mapa)
        val btnchats = findViewById<ImageButton>(R.id.chats)
        val btnperfil = findViewById<ImageButton>(R.id.persona)
        val btnhome = findViewById<ImageButton>(R.id.arcade)

        var btncancelar = findViewById<Button>(R.id.back)

        datePicker = findViewById(R.id.datePicker)
        btnCrear = findViewById(R.id.next)
        btnBuscarImg= findViewById(R.id.buscarIma)
        plataform =findViewById(R.id.plataformas)
        participants = findViewById(R.id.editParticipantes)
        description = findViewById(R.id.editDescripcion)
        nameGame = findViewById(R.id.editNombre)


        datePickerInitializr()


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

        btncancelar.setOnClickListener{
            var intent = Intent(this, homeActivity::class.java)
            startActivity(intent);
        }

    }

    private fun datePickerInitializr(){

        datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)
        ) { view, yearNumber, monthNumber, dayNumber ->
            var monthNum = monthNumber + 1
            month = monthNum.toString()
            day = dayNumber.toString()
            year=yearNumber.toString()
        }
    }

    private fun revizar():Boolean {
        if (!nameGame.text.isEmpty()) {
            if (!description.text.isEmpty()) {
                if (!participants.text.isEmpty() && participants.text != "0") {
                    if (Integer.parseInt(month) >= today.get(Calendar.MONTH) && Integer.parseInt(day) >= today.get(
                            Calendar.DAY_OF_MONTH
                        ) && Integer.parseInt(year) >= today.get(Calendar.YEAR)
                    ) {
                        return true
                    } else {
                        val toast = Toast.makeText(
                            this,
                            "Ingresa una fecha valida por favor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    participants.error = "Requerido"
                }
             } else {
                description.error = "Requerido"
            }
        }else{
            nameGame.error = "Requerido"
        }
        return false
    }


    private fun registrarEvento(){


        var user : FirebaseUser? = mAuth.currentUser
        if(user!=null){
            var UserProfileBuilder: UserProfileChangeRequest.Builder = UserProfileChangeRequest.Builder()
            var mEvento:Evento = Evento()

            mEvento.nombre=nameGame.text.toString()
            mEvento.descripcion=description.text.toString()
            mEvento.fecha=date
            mEvento.participantes=Integer.parseInt(participants.text.toString())
            mEvento.plataforma = plataform.selectedItem.toString()

            var imageUID:String = UUID.randomUUID().toString()


            var ref:StorageReference = mStorageRef.child(PATH_IMGES+imageUID)
            var imagePath :String =imageUri.path.toString()
            ref.putFile(imageUri)
            mEvento.imageUri=imageUID


         }
    }


}