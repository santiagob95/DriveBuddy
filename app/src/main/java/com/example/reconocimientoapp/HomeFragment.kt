package com.example.reconocimientoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private var auth: FirebaseAuth = Firebase.auth
private val db = FirebaseFirestore.getInstance()
/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {


    override fun onStart() {
        super.onStart()
        val userRef = db.collection("users").document(auth.currentUser!!.uid)
        val docRef = db.collection("/viajes").whereEqualTo("id", auth.currentUser!!.uid)

        userRef.get().addOnSuccessListener { docSnapshot ->
            val userDoc = docSnapshot.data
            var title = "Bienvenido de vuelta, "

            if (auth.currentUser!!.isAnonymous) {
                root!!.mainTitle.text = "¡Registrate para ver tus estadisticas!"
                root!!.textView6.visibility = View.INVISIBLE
                root!!.registerback.visibility = View.VISIBLE
                root!!.txtregis.visibility = View.VISIBLE
            } else {
                root!!.mainTitle.text = title + userDoc!!.getValue("nomYApe")
                root!!.mainTitle.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                //test
                chargeData()
            }

        }
        docRef.get()
            .addOnFailureListener { exception ->
                fatigaTotal.text = "0"
                Log.v("Get Document", "Error getting documents: ", exception)
            }
            .addOnSuccessListener { documents ->
                var total = object {
                    var fatiga=0
                    var bostezo=0
                    var pestLargo =0
                    var kmtotales =0
                    var tiempoViajeTotal =0
                    var velMedia =0

                }
                var contDoc = 0;
                for(document in documents){
                   if( document.exists() ) {
                       total.fatiga += document.data!!.getValue("Fatiga").toString().toInt()
                       total.bostezo += document.data!!.getValue("Bostezo").toString().toInt()
                       total.pestLargo += document.data!!.getValue("PestaneoLargo").toString().toInt()
                       total.kmtotales +=document.data!!.getValue("kmRecorrido").toString().toInt()
                       total.tiempoViajeTotal += document.data!!.getValue("tiempoTotal").toString().toInt()
                       total.velMedia += document.data!!.getValue("velocidadMedia").toString().toInt()
                       contDoc++
                   }
                }
                fatigaTotal.text = if(contDoc == 0) "0" else total.fatiga.toString()
                tiempoViajeTotal.text =if(contDoc == 0) "0" else total.tiempoViajeTotal.toString() + " hs"
                pestLargoTotal.text = if(contDoc == 0) "0" else total.pestLargo.toString()
                bostezosTotal.text = if(contDoc == 0) "0" else total.bostezo.toString()
                velMedia.text = if(contDoc == 0) "0" else (total.velMedia/contDoc).toString() +" km/h"
                kmTotales.text = if(contDoc == 0) "0" else total.kmtotales.toString() +" km"
            }

        registerback.setOnClickListener{
            activity?.let{
                val intent = Intent (it, RegisterActivity::class.java)
                it.startActivity(intent)
            }
        }

    }

    fun chargeData() {
        val viajesRef = db.collection("viajes").document(auth.currentUser!!.uid)
        viajesRef.get().addOnSuccessListener { docSnapshot ->
            val viajesDoc = docSnapshot.data

            val titles = arrayOf("Tiempo de viaje total","Fatigas detectadas" ,"Pestaneo largo", "Bostezos", "Velocidad media","Kilometros recorridos")

            root!!.title0.text = titles[0]
            root!!.title1.text = titles[1]
            root!!.title2.text = titles[2]
            root!!.title3.text = titles[3]
            root!!.title4.text = titles[4]
            root!!.title5.text = titles[5]
            try {
                root!!.tiempoViajeTotal.text = viajesDoc!!.getValue("tiempoTotal").toString() + " hs"
                root!!.fatigaTotal.text = viajesDoc!!.getValue("Fatiga").toString()
                root!!.pestLargoTotal.text = viajesDoc!!.getValue("PestaneoLargo").toString()
                root!!.bostezosTotal.text = viajesDoc!!.getValue("Bostezo").toString()
                root!!.velMedia.text = viajesDoc!!.getValue("velocidadMedia").toString() + " km/h"
                root!!.kmTotales.text = viajesDoc!!.getValue("kmRecorrido").toString() + " km"
            }catch (e:Exception) {
                Toast.makeText(getActivity(),e.message + " + No se encontro en la base de datos",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private var root: View? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root= inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }
}