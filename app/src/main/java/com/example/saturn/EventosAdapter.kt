package com.example.saturn

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_evento.view.*
import kotlinx.android.synthetic.main.evento.view.*

class EventosAdapter(private val mContext: Context, private val listaEventos: List<Evento>) : ArrayAdapter<Evento>(mContext, 0,listaEventos) {



    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.evento,parent,false)

        val eventoNuevo = listaEventos[position]
        val storageReference : StorageReference = FirebaseStorage.getInstance().reference

        layout.nombreEvento.text = eventoNuevo.nombre
        layout.descripcionEvento.text = eventoNuevo.descripcion
        layout.plataformasEvento.text = eventoNuevo.plataforma
        layout.fechaEvento.text= eventoNuevo.fecha
        layout.participantesEvento.text = eventoNuevo.participantes.toString()

        storageReference.child("images/event/"+eventoNuevo.imageUri).downloadUrl.addOnSuccessListener {
            Glide.with(this.context).load(it).error(R.drawable.avatar).into(layout.imagenEvento)
        }


        return layout
    }
}