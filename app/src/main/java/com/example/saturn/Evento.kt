package com.example.saturn

import java.io.Serializable
import java.sql.Time
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class Evento(val nombre:String, val descripcion:String, val plataforma:String, val fecha:LocalDate, val participantes:Int, val imagen:Int) : Serializable {



}