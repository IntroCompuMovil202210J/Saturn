package com.example.saturn


import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.friends.view.*

class UsuariosAdapter(private val mContext: Context, private val listaUsuarios: List<Usuario>) : ArrayAdapter<Usuario>(mContext, 0,listaUsuarios) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.friends,parent,false)

        val usuarioNuevo = listaUsuarios[position]
        val storageReference : StorageReference=FirebaseStorage.getInstance().reference
            storageReference.child("images/profile/"+usuarioNuevo.imageUri).downloadUrl.addOnSuccessListener {
                Glide.with(this.context).load(it).error(R.drawable.avatar).into(layout.avatar)
            }
        layout.Name.text=usuarioNuevo.nickname
        
        return layout
    }
    
}