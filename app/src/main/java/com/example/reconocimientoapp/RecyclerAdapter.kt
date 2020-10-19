package com.example.reconocimientoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter (private var titulos: List<String>, private var contenidos: List<String>, private var imagenes: List<Int>) :
RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val itemTitulo: TextView = itemView.findViewById(R.id.textView3)
        val itemContenido: TextView = itemView.findViewById(R.id.textView4)
        val itemImagen: ImageView = itemView.findViewById(R.id.imageView2)

        init {
            itemView.setOnClickListener { v: View ->
                val position: Int = adapterPosition
                Toast.makeText(itemView.context, "Este es el consejo # ${position + 1}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemTitulo.text = titulos[position]
        holder.itemContenido.text = contenidos[position]
        holder.itemImagen.setImageResource(imagenes[position])
    }

    override fun getItemCount(): Int {
        return titulos.size
    }

}