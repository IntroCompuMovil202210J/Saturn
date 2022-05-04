package com.example.saturn

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.evento.view.*

class EventosAdapter(private val mContext: Context, private val listaEventos: List<Evento>) : ArrayAdapter<Evento>(mContext, 0,listaEventos) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.evento,parent,false)

        val eventoNuevo = listaEventos[position]

        layout.nombreEvento.text = eventoNuevo.nombre
        layout.descripcionEvento.text = eventoNuevo.descripcion
        layout.plataformasEvento.text = eventoNuevo.plataforma
        layout.fechaEvento.text= eventoNuevo.fecha.toString()
        layout.participantesEvento.text = eventoNuevo.participantes.toString()
        layout.imagenEvento.setImageResource(eventoNuevo.imagen)
        return layout
    }
}