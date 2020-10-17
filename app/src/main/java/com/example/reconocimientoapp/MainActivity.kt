package com.example.reconocimientoapp

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_home.*
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth


    // Access a Cloud Firestore instance from your Activity
    private val db = FirebaseFirestore.getInstance()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId){
            R.id.home -> {
                println("home pressed")
                replaceFragment(HomeFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.face -> {
                println("face pressed")
                replaceFragment(FaceFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.cons -> {
                println("cons pressed")
                replaceFragment(ConsFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_navbar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        replaceFragment(HomeFragment())
        //setup
        exitBtn.setOnClickListener {
            auth.signOut()
            val welcomeIntent = Intent(this,Welcome_Screen::class.java)
            startActivity(welcomeIntent)
        }
    }

     private fun isUserInFirestore(): Boolean {
         val usersRef = db.collection("users/").whereEqualTo("id",auth.currentUser!!.uid )
         val exists = usersRef.get().isSuccessful

         Log.v("docInDB", "el user ${auth.currentUser!!.uid} existe? $exists")
         return false
     }
     @RequiresApi(Build.VERSION_CODES.O)//esto es para la fecha de lastLogin
     override fun onStart(){
         super.onStart()
         val bundle = intent.extras
         val nomYApe = bundle?.getString("nomYApe")
         Log.d("Nombre y apellido", "el nombre y apellido es: $nomYApe")
         //No existe un documento para ese usuario (caso que se acabe de registrar)
         if(!isUserInFirestore()){
             val guestUser = hashMapOf(
                 "id" to auth.currentUser!!.uid,
                 "lastLogin" to LocalDateTime.now().toString()
             )
             val user = hashMapOf(
                 "id" to auth.currentUser!!.uid,
                 "nomYApe" to if (!auth.currentUser!!.displayName.isNullOrBlank() ) auth.currentUser!!.displayName.toString() else nomYApe,
                 "email" to auth.currentUser!!.email.toString(),
                 "foto" to auth.currentUser!!.photoUrl.toString(),
                 "lastLogin" to LocalDateTime.now().toString()
             )
             if(auth.currentUser!!.isAnonymous){
                 db.document("users/"+auth.currentUser!!.uid)
                     .set(guestUser)
                     .addOnSuccessListener {
                         Log.d("Firestore DB", "Document added with ID: ${auth.currentUser!!.email.toString()}")
                     }
                     .addOnFailureListener { e ->
                         Log.w("Firestore DB", "Error adding document", e)
                     }
             }
             else {
                 db.document("users/" + auth.currentUser!!.uid)
                     .set(user)
                     .addOnSuccessListener {
                         Log.d(
                             "Firestore DB",
                             "Document added with ID: ${auth.currentUser!!.email.toString()}"
                         )
                     }
                     .addOnFailureListener { e ->
                         Log.w("Firestore DB", "Error adding document", e)
                     }
             }
         }
    }
    private fun replaceFragment (fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }



}