package com.example.reconocimientoapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_cons.*
import androidx.recyclerview.widget.LinearLayoutManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var tituloslist = mutableListOf<String>()
    private var contenidoslist = mutableListOf<String>()
    private var imageneslist = mutableListOf<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        postToList()
        rv_recyclerView.layoutManager = LinearLayoutManager(this.context)
        rv_recyclerView.adapter = RecyclerAdapter(tituloslist, contenidoslist, imageneslist)
    }

    private fun addToList(titulo: String, contenido: String, imagen: Int){
        tituloslist.add(titulo)
        contenidoslist.add(contenido)
        imageneslist.add(imagen)
    }

    private fun postToList(){
        addToList(titulo = "Realiza algo de actividad antes de un viaje largo.", contenido = "Dar un paseo o algo de ejercicio moderado antes de conducir ayuda a despejarse y activar el cuerpo.", imagen = R.drawable.ic_volume)
        addToList(titulo = "Recordá beber mucho líquido", contenido = "La deshidratación causa fatiga, así que hay que beber para evitarlo, sobre todo en verano.", imagen = R.drawable.ic_volume)
        /*addToList(titulo = "Tené cuidado con los estimulantes.", contenido = "El café, el té o las bebidas energéticas pueden evitar la fatiga a corto plazo, pero pasados sus efectos volverá a aparecer.", imagen = R.drawable.ic_volume)
        addToList(titulo = "Nada de alimentos pesados.", contenido = "Una comida copiosa conlleva una digestión más pesada y al incremento de la fatiga.", imagen = R.drawable.ic_volume)
        addToList(titulo = "¡No te quedes dormido!", contenido = "Es esencial haber descansado lo suficiente si se va a conducir de noche.", imagen = R.drawable.ic_volume)
        addToList(titulo = "Especial atención en la última hora de conducción.", contenido = "Cuando se está llegando al destino hay mayor cansancio acumulado y se baja la guardia, hay que mantenerse atentos.", imagen = R.drawable.ic_volume)
        addToList(titulo = "Alimentos ricos en hierro.", contenido = "La fatiga puede aumentar por la falta de nutrientes, así que mejorar ese aspecto ayuda.", imagen = R.drawable.ic_volume)
        addToList(titulo = "¡No manejes incómodo!", contenido = "Recordá usar ropa cómoda y mantener una buena postura de conducción.", imagen = R.drawable.ic_volume)
        addToList(titulo = "Toma una ducha bien fría.", contenido = "Un baño frío estimula el cuerpo y el shock de temperatura te ayudará a despertar.", imagen = R.drawable.ic_volume)
        addToList(titulo = "¡Cambia cosas!", contenido = "La monotonía no te ayuda a permanecer alerta. Trata de meter variedad en tu día para mantenerte en movimiento.", imagen = R.drawable.ic_volume)*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cons, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ConsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ConsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}