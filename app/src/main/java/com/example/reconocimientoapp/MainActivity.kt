package com.example.reconocimientoapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth

    // Access a Cloud Firestore instance from your Activity
    private val db = FirebaseFirestore.getInstance()
    var nav=R.id.home
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when(item.itemId){
            R.id.home -> {
                if (nav != item.itemId) {
                    nav = item.itemId
                    replaceFragment(HomeFragment())
                    return@OnNavigationItemSelectedListener true
                }
            }
            R.id.face -> {
                if (nav != item.itemId) {
                    nav = item.itemId
                    replaceFragment(FaceFragment())
                    return@OnNavigationItemSelectedListener true
                }
            }
            R.id.cons -> {
                if (nav != item.itemId) {
                    nav = item.itemId
                    replaceFragment(ConsFragment())
                    return@OnNavigationItemSelectedListener true
                }
            }
        }
        false


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        bottom_navbar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        replaceFragment(HomeFragment())
        //setup

        val bundle = intent.extras
        val nomYApe = bundle?.getString("nomYApe")
        if(nomYApe!= null){
            val profileUpdate= userProfileChangeRequest {
                displayName = nomYApe
            }
            auth.currentUser!!.updateProfile(profileUpdate)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Log.v("Register", "user display name updated correctly")
                    }
                }
        }
        exitBtn.setOnClickListener {
            auth.signOut()
            val welcomeIntent = Intent(this, Welcome_Screen::class.java)
            startActivity(welcomeIntent)
        }
    }


     @RequiresApi(Build.VERSION_CODES.O)//esto es para la fecha de lastLogin
     override fun onStart(){
         super.onStart()
         val bundle = intent.extras
         val userDocRef = db.document("users/" + auth.currentUser!!.uid)

             val guestUser = hashMapOf(
                 "id" to auth.currentUser!!.uid,
                 "lastLogin" to LocalDateTime.now().toString()
             )
             val user = hashMapOf(
                 "id" to auth.currentUser!!.uid,
                 "nomYApe" to (bundle?.getString("nomYApe")
                     ?: auth.currentUser!!.displayName.toString()),
                 "email" to auth.currentUser!!.email.toString(),
                 "foto" to auth.currentUser!!.photoUrl.toString(),
                 "lastLogin" to LocalDateTime.now().toString()
             )
             if(auth.currentUser!!.isAnonymous){
                 userDocRef
                     .set(guestUser)
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
             else {
                 userDocRef
                     .set(user, SetOptions.merge())
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
    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }




}