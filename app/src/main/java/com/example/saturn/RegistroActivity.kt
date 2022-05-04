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
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.FileNotFoundException
import java.util.*

class RegistroActivity : AppCompatActivity() {
    private val PATH_USERS:String ="users/"
    private val PATH_IMGES:String ="images/profile/"
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
    private lateinit var mStorageRef :StorageReference
    private lateinit var storage:FirebaseStorage
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registo)

        asignarXml()
        btnIniciar.setOnClickListener{
            var valido = revizar()
            if(valido){
                registrarUsuario()
            }
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
        contra = findViewById(R.id.editPassword)
        plataformas = findViewById(R.id.plataformas)
        image = findViewById(R.id.imageP)
        btnBuscarImg = findViewById(R.id.buscarIma)
        storage = FirebaseStorage.getInstance()
        mStorageRef = storage.reference
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()

    }

    private fun revizar():Boolean{
        if(!nombre.text.isEmpty()){
            if(!edad.text.isEmpty()){
                if(!email.text.isEmpty() && email.text.contains('@') && email.text.contains('.')){
                    if(!nickname.text.isEmpty()){
                        if(!contra.text.isEmpty() && contra.text.length>=6){
                            return true
                        }else if(contra.text.isEmpty()){
                           contra.error="Requerido"
                        }else{
                            contra.error="6 caracteres minimo"
                        }
                    }else{
                        nickname.error="Requerido"
                    }
                }else if(email.text.isEmpty()){
                    email.error="Requerido"
                }else{
                   email.error = "Formato no aceptado"
                }
            }else{
                edad.error="Requerido"
            }
       }else{
            nombre.error="Requerido"
        }
        return false
    }

    private fun registrarUsuario(){

        mAuth.createUserWithEmailAndPassword( email.text.toString(),contra.text.toString()).addOnCompleteListener {
            if(it.isSuccessful){
                var user : FirebaseUser? = mAuth.currentUser
                if(user!=null){
                    var UserProfileBuilder:UserProfileChangeRequest.Builder = UserProfileChangeRequest.Builder()
                    var mUser:Usuario = Usuario()

                    UserProfileBuilder.setDisplayName(nombre.text.toString())
                    UserProfileBuilder.setPhotoUri(imageUri)
                    user.updateProfile(UserProfileBuilder.build())


                    mUser.nombre=nombre.text.toString()
                    mUser.edad=edad.text.toString()
                    mUser.email=email.text.toString()
                    mUser.nickname=nickname.text.toString()
                    mUser.contra=contra.text.toString()
                    mUser.plataformas = plataformas.selectedItem.toString()

                    var imageUID:String = UUID.randomUUID().toString()


                    var ref:StorageReference = mStorageRef.child(PATH_IMGES+imageUID)
                    var imagePath :String =imageUri.path.toString()
                    ref.putFile(imageUri)
                    mUser.imageUri=imageUID


                    var keyAuth: String? = user.uid
                    myRef=database.getReference(PATH_USERS+keyAuth)
                    myRef.setValue(mUser)
                    updateUI(user.email.toString())
                }
            }else{
                var toast = Toast.makeText(this,"gdfgnudfshgdfsuihvboisd",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(email:String) {
        if(email!=null){
            val intent = Intent(this, homeActivity::class.java)
            intent.putExtra("user",email)
            startActivity(intent);
        }
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
                        imageUri = data?.data as Uri
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