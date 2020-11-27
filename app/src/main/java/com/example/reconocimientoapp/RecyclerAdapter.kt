package com.example.reconocimientoapp

import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class RecyclerAdapter(
    private var titulos: List<String>,
    private var contenidos: List<String>,
    private var imagenes: List<Int>
) :
RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), TextToSpeech.OnInitListener{
        val itemTitulo: TextView = itemView.findViewById(R.id.textView3)
        val itemContenido: TextView = itemView.findViewById(R.id.textView4)
        val itemImagen: ImageView = itemView.findViewById(R.id.imageView2)
        private var tts: TextToSpeech? = null;

        init {
            itemView.isEnabled = false;
            tts = TextToSpeech(itemView.context, this)

            itemView.setOnClickListener { v: View ->
                processTextToSpeech()
            }
        }

        override fun onInit(status: Int) {
            if(status == TextToSpeech.SUCCESS){
                val locSpanish = Locale("spa", "MEX")
                val result = tts!!.setLanguage(locSpanish)
                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED ){
                    Log.e("TTS", "¡No se puede reproducir el lenguaje especificado!")
                }else {
                    itemView.isEnabled = true;
                }
            }else{
                Log.e("TTS", "¡Falló la inicialización!")
            }
        }

        private fun processTextToSpeech(){
            val position: Int = adapterPosition
            val text = contenidos[position]
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
            Toast.makeText(itemView.context, "Reproduciendo el consejo # ${position + 1}", Toast.LENGTH_LONG).show()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
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