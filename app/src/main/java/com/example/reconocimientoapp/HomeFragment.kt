package com.example.reconocimientoapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_home.*

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



        userRef.get()
            .addOnSuccessListener { docSnapshot ->
                val userDoc = docSnapshot.data
                var title = "Bienvenido de vuelta, "

                if (auth.currentUser!!.isAnonymous) {
                    mainTitle.text = "¡Registrate para ver tus estadisticas!"
                    mainTitle.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    textView6.visibility = View.GONE
                    registerback.visibility = View.VISIBLE
                } else {
                    mainTitle.text = title + userDoc!!.getValue("nomYApe")
                    chargeData()
                }
            }

    }

    fun chargeData() {
        val params = arrayOf("233 hs","4","32", "12", "66 km/h","12%")
        val titles = arrayOf("Tiempo de viaje total","Fatigas detectadas" ,"Pestaneo largo", "Bostezos", "Velocidad media","Porcentaje de viaje fatigado")

        title0.text = titles[0]
        title1.text = titles[1]
        title2.text = titles[2]
        title3.text = titles[3]
        title4.text = titles[4]
        title5.text = titles[5]
        param0.text = params[0]
        param1.text = params[1]
        param2.text = params[2]
        param3.text = params[3]
        param4.text = params[4]
        param5.text = params[5]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

}