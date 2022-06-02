package com.example.saturn

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat.from
import androidx.biometric.*
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import org.w3c.dom.Text
import java.io.File
import java.text.SimpleDateFormat
import java.util.*



class logActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var txtEmail:EditText
    private lateinit var txtContra:EditText
    private lateinit var btnIniciar:Button
    private lateinit var btnCancelar:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        mAuth = FirebaseAuth.getInstance();
        btnIniciar=findViewById(R.id.iniciar)
        btnCancelar=findViewById(R.id.retroceder)
        txtEmail=findViewById(R.id.email)
        txtContra=findViewById(R.id.contra)

        var executor = ContextCompat.getMainExecutor(this)
       var biometricPrompt = BiometricPrompt(this, executor,  object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext,"$errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    loginWithFingerprint()
                    Toast.makeText(applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        var promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setNegativeButtonText("Cancel")
            .build()
        val biometricManager = BiometricManager.from(this)
        var canAuthenticate:Boolean = false
        when(biometricManager.canAuthenticate(BIOMETRIC_STRONG) ){
            BiometricManager.BIOMETRIC_SUCCESS->
                canAuthenticate = true
        }

        var path = applicationContext.filesDir
        var directory = File(path,"credentials")
        if(File(directory,"credentials.txt").exists() && canAuthenticate){
            biometricPrompt.authenticate(promptInfo)
        }

        btnIniciar.setOnClickListener{
            authenticateWithFB()
        }

        btnCancelar.setOnClickListener(){
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent);
        }
    }

    private fun loginWithFingerprint(){
        val lineList = mutableListOf<String>()
        var path = applicationContext.filesDir
        var directory = File(path,"credentials")

        File(directory, "credentials.txt").useLines { lines ->lines.forEach { lineList.add(it) }}
        txtEmail.setText( lineList.elementAt(0))
        txtContra.setText(lineList.elementAt(1))
    }

    private fun writeToFile(email : String, pass : String){

        var path = applicationContext.filesDir
        var directory = File(path,"credentials")
        directory.mkdirs()


        var info : String = email+"\n"+pass
        var file=File(directory,"credentials.txt")

        file.createNewFile()
        file.writeText(info)
    }

    private fun authenticateWithFB() {
        val email = txtEmail.text.toString()
        val pass = txtContra.text.toString()
        if(validateForm(email,pass)){
            mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(OnCompleteListener {
                if(it.isSuccessful){
                    writeToFile(email, pass)

                    updateUI(mAuth.currentUser?.email.toString())

                }else{
                    var message = it.exception?.message.toString()
                    var toast = Toast.makeText(this, message,Toast.LENGTH_SHORT).show()
                }
            })
        }else{
            val toast = Toast.makeText(this,"Por favor ingrese el correo y la contraseña correctamente",Toast.LENGTH_SHORT).show()
        }

    }



    private fun validateForm(email:String, pass:String): Boolean {
        var valid:Boolean=false
        if(email.contains('@') and email.contains('.')){
            if(pass.length>=6){
                valid = true;
            }
        }
        return valid;
    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser!=null){
            updateUI(mAuth.currentUser!!.email.toString())
        }
    }

    private fun updateUI(email:String) {
        if(email!=null){
            val intent = Intent(this, homeActivity::class.java)
            intent.putExtra("user",email)
            startActivity(intent);
        }else{
            txtEmail.hint="Ingrese su email"
            txtContra.hint="ingrese su contra"
        }
    }

}