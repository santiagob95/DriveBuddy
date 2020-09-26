package com.example.reconocimientoapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //setup
        val bundle = intent.extras
        val email = bundle?.getString("email")
        setup(email?:"")

        //Guardado de datos (mantener sesion iniciada)
        val prefs = getSharedPreferences(getString(R.string.prefs_file),Context.MODE_PRIVATE).edit()
        prefs.putString("email",email)
        prefs.apply()
    }
    private fun setup(email:String) {
        title = "Inicio"
        emailText.text = "Bienvenido " + email
        logOutBtn.setOnClickListener {
            //Borrado de datos de sesion
            val prefs = getSharedPreferences(getString(R.string.prefs_file),Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            LoginManager.getInstance().logOut()
            FirebaseAuth.getInstance().signOut()
            onBackPressed()

    }

    }
}