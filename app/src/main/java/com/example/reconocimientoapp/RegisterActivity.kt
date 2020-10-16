package com.example.reconocimientoapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_auth.idEmail
import kotlinx.android.synthetic.main.activity_auth.idPassword
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.backloginBtn
import java.time.LocalDateTime

class RegisterActivity : AppCompatActivity() {
    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        backloginBtn.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        setup()
    }

    private fun showHome(nomYApe:String) {
        val homeIntent = Intent(this, MainActivity::class.java).apply{
            putExtra("nomYApe",nomYApe)
        }
        startActivity(homeIntent)
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Hubo un error autenticando al usuario")
        builder.setPositiveButton("aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    private fun showAlert(err:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(err)
        builder.setPositiveButton("aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    private fun setup() {
        title = "Autenticacion"

        //EMAIL Y PASSWORD
        registrarBtn.setOnClickListener {
            if (idEmail.text.isNotEmpty() && idPassword.text.isNotEmpty() && idRePassword.text.isNotEmpty()) {
                if(idNyA.text.isNotEmpty()) {
                    if(idPassword.text.toString() == idRePassword.text.toString()){
                        auth.createUserWithEmailAndPassword(
                            idEmail.text.toString(),
                            idPassword.text.toString()
                        ).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                                showHome(idNyA.text.toString())
                            } else {
                                showAlert()
                            }
                        }
                    }
                    else
                        showAlert("Las contraseñas no coinciden.")

                }
                else
                    showAlert("Introduzca su nombre y apellido.")
            }
            else
                showAlert("Introduzca mail y contraseña.")
        }
    }
}