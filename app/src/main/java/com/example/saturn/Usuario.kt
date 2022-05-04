package com.example.saturn

import android.net.Uri
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Usuario (
    var nombre: String?=null,
    var edad: String?=null,
    var email: String?=null,
    var nickname: String?=null,
    var contra: String?=null,
    var plataformas : String?=null,
    var imageUri: String?=null){}