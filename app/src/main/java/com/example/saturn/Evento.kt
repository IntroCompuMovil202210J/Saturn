package com.example.saturn

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
import java.sql.Time
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@IgnoreExtraProperties
data class Evento(
    var nombre:String?=null,
    var descripcion:String?=null,
    var plataforma:String?=null,
    var fecha:String?=null,
    var participantes:Int?=null,
    var imageUri: String?=null,
    var lat:Double?=null,
    var lon:Double?=null,
    var participantesUID : String? = null,
    var ownerUID: String?=null ):Serializable