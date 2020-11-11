package com.example.reconocimientoapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_cons.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private val db = FirebaseFirestore.getInstance()

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
        postToList()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        rv_recyclerView.layoutManager = LinearLayoutManager(this.context)
        rv_recyclerView.adapter = RecyclerAdapter(tituloslist, contenidoslist, imageneslist)

    }

    private fun addToList(titulo: String, contenido: String){
        tituloslist.add(titulo)
        contenidoslist.add(contenido)
        imageneslist.add(R.drawable.ic_volume)
    }


    private fun postToList(){
        val consRef = db.collection("consejos")
        consRef.get()
            .addOnSuccessListener { consejos->
                Log.v("GetConsejo", "Get consejo operation, succesful")
                for (consejo in consejos){
                    if (consejo.exists()) {
                            val t = consejo.data.getValue("titulo").toString()
                            val c = consejo.data.getValue("descripcion").toString()
                            addToList(
                                titulo = t,
                                contenido = c
                            )

                        Log.v("GetConsejo", "Añadi: {$t}")
                    }
                }
                rv_recyclerView.adapter = RecyclerAdapter(tituloslist, contenidoslist, imageneslist)
            }
            .addOnFailureListener { exception ->
                Log.v("GetConsejo", "Error getting documents: ", exception)
            }
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