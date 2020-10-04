package com.example.reconocimientoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_welcome__screen.*
import kotlinx.coroutines.internal.ThreadSafeHeap

class Welcome_Screen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        Thread.sleep(1000) //sacar esto despues, es para que se vea la pantalla de carga
        setTheme(R.style.AppTheme_NoActionBar)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome__screen)

        welcomeBtn.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        guestBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        registroBtn.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            // start your next activity
            startActivity(intent)
        }


    }
}
