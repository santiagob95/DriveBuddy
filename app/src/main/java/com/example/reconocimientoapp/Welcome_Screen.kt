package com.example.reconocimientoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_welcome__screen.*

class Welcome_Screen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome__screen)

        welcomeBtn.setOnClickListener{
            val intent = Intent(this, AuthActivity::class.java)
            // start your next activity
            startActivity(intent)
        }

        guestBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            // start your next activity
            startActivity(intent)
        }

    }
}