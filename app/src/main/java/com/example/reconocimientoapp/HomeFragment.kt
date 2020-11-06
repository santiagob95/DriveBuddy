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
import java.math.RoundingMode
import java.text.DecimalFormat

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
        loadUserData()
        loadViajesData()
        registerback.setOnClickListener{
            val anonID = auth.currentUser?.uid
            auth.signOut()
            activity?.let{
                val intent = Intent (it, RegisterActivity::class.java).apply{
                    putExtra("anonID", anonID)
                }
                it.startActivity(intent)
            }
        }
    }
    private fun loadViajesData(){
        val docRef =  db.collection("/viajes").whereEqualTo("id", auth.currentUser!!.uid)
        docRef.get()
            .addOnFailureListener { exception ->
                fatigaTotal.text = "0"
                Log.v("GetDoc", "Error getting documents: ", exception)
            }
            .addOnSuccessListener { documents ->
                Log.v("GetDoc", "Doc created CORRECTLY")
                val total = object {
                    var fatiga=0
                    var bostezo=0
                    var pestLargo =0
                    var kmtotales =0
                    var tiempoViajeTotal =0.0
                    var velMedia =0

                }
                var contDoc = 0
                for(document in documents){

                    if( document.exists() ) {
                        Log.v("GetDoc", "\n-------Doc:\n"+document.data.values)
                        total.fatiga += document.data.getValue("Fatiga").toString().toInt()
                        total.bostezo += document.data.getValue("Bostezo").toString().toInt()
                        total.pestLargo += document.data.getValue("PestaneoLargo").toString().toInt()
                        total.kmtotales +=document.data.getValue("kmRecorrido").toString().toInt()
                        total.tiempoViajeTotal += document.data.getValue("tiempoTotal").toString().toDouble()
                        total.velMedia += document.data.getValue("velocidadMedia").toString().toInt()
                        contDoc++

                    }
                }

                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.CEILING
                root!!.fatigaTotal.text = if(contDoc == 0) "0" else total.fatiga.toString()
                root!!.tiempoViajeTotal.text =if(contDoc == 0) "0" else df.format(total.tiempoViajeTotal) + " hs"
                root!!.pestLargoTotal.text = if(contDoc == 0) "0" else total.pestLargo.toString()
                root!!.bostezosTotal.text = if(contDoc == 0) "0" else total.bostezo.toString()
                root!!.velMedia.text = if(contDoc == 0) "0" else (total.velMedia/contDoc).toString() +" km/h"
                root!!.kmTotales.text = if(contDoc == 0) "0" else total.kmtotales.toString() +" km"

            }

    }
    private fun loadUserData() {
        val userRef = db.collection("users").document(auth.currentUser!!.uid)
        userRef.get().addOnSuccessListener { docSnapshot ->
            val userDoc = docSnapshot.data
            val title = "Bienvenido de vuelta, "

            if (auth.currentUser!!.isAnonymous) {
                root!!.mainTitle.text = "¡Registrate para ver tus estadisticas!"
                root!!.textView6.visibility = View.INVISIBLE
                root!!.registerback.visibility = View.VISIBLE
                root!!.txtregis.visibility = View.VISIBLE
            } else {
                root!!.mainTitle.text = title + userDoc!!.getValue("nomYApe")
                root!!.mainTitle.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                //test
                //chargeData()
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