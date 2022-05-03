package com.example.saturn

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.FileNotFoundException

class RegistroActivity : AppCompatActivity() {

    private lateinit var nombre: EditText
    private lateinit var edad: EditText
    private lateinit var email: EditText
    private lateinit var nickname: EditText
    private lateinit var contra: EditText
    private lateinit var plataformas: Spinner
    private lateinit var btnIniciar: Button
    private lateinit var btnCancelar: Button
    private lateinit var btnBuscarImg: Button
    private lateinit var image : ImageView
    private var GALERY_CODE: Int = 113

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registo)

        asignarXml()
        btnIniciar.setOnClickListener{
            revizar()
        }

        btnCancelar.setOnClickListener(){
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent);
        }

        btnBuscarImg.setOnClickListener(){
            AskPermission()
            selectIamge()
        }
    }

    private fun asignarXml(){
        btnIniciar=findViewById(R.id.next)
        btnCancelar=findViewById(R.id.back)
        nombre=findViewById(R.id.editNombre)
        edad=findViewById(R.id.editEdad)
        email=findViewById(R.id.email)
        nickname=findViewById(R.id.editNick)
        contra = findViewById(R.id.contra)
        plataformas = findViewById(R.id.plataformas)
        image = findViewById(R.id.imageP)
        btnBuscarImg = findViewById(R.id.buscarIma)
    }

    private fun revizar(){
        if(!nombre.text.isEmpty()){
            if(!edad.text.isEmpty()){
                if(!email.text.isEmpty() && email.text.contains('@') && email.text.contains('.')){
                    if(!nickname.text.isEmpty()){
                        if(!contra.text.isEmpty() && contra.text.length>=6){
                            registrarUsuiario()
                        }else if(!contra.text.isEmpty()){
                           val toast=Toast.makeText(this, "el campo de contraseña se encuentra vacio", Toast.LENGTH_SHORT).show()
                        }else{
                            val toast=Toast.makeText(this, "La contraseña debe ser de minimo 6 caracteres", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        val toast=Toast.makeText(this, "El nickname esta vacio", Toast.LENGTH_SHORT).show()
                    }
                }else if(!email.text.isEmpty()){
                    val toast=Toast.makeText(this, "El email esta vacio", Toast.LENGTH_SHORT).show()
                }else{
                    val toast=Toast.makeText(this, "Por favor revizar que el email este bien escrito ", Toast.LENGTH_SHORT).show()
                }
            }else{
                val toast=Toast.makeText(this, "Por favor ingrese su edad", Toast.LENGTH_SHORT).show()
            }
       }else{
            val toast=Toast.makeText(this, "Por favor ingrese su nombre", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registrarUsuiario(){
        var intent = Intent(this, homeActivity::class.java)
        startActivity(intent);
    }

    private fun selectIamge(){
        var pickImage = Intent(Intent.ACTION_PICK)
        pickImage.setType("image/*")

        if(intent.resolveActivity(packageManager)!= null){
            startActivityForResult(pickImage,GALERY_CODE)
        }else{
            val toast = Toast.makeText(this,"Unable to open galery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            GALERY_CODE->
                if(resultCode== RESULT_OK){
                    try{
                        val imageUri: Uri = data?.data as Uri
                        var imageStream = contentResolver.openInputStream(imageUri)
                        var selectedImage = BitmapFactory.decodeStream(imageStream)
                        image.setImageBitmap(selectedImage)
                    }catch( e: FileNotFoundException){
                        var toast= Toast.makeText(this,"File not found",Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun AskPermission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                var builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    .setTitle("Gallery permission")
                    .setMessage("Se solicita permiso para poder tener acceso a su galeria")
                    .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                        ActivityCompat.requestPermissions(this, Array(1) { android.Manifest.permission.READ_EXTERNAL_STORAGE }, GALERY_CODE )
                    }
                    .setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
                        var i = Intent(this, MainActivity::class.java)
                        startActivity(i)
                    }
                builder.show()
            }else{
                ActivityCompat.requestPermissions(this,Array(1){android.Manifest.permission.READ_EXTERNAL_STORAGE},GALERY_CODE)
            }
        }else{
            val toast = Toast.makeText(this,"you already granted permissons",Toast.LENGTH_SHORT).show()
        }
    }
}