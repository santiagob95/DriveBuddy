package com.example.reconocimientoapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.modaldialog.*

class MyCustomDialog: DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);
        return inflater.inflate(R.layout.modaldialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, height)


        recibirData("12","12","12","12")

        otroBtn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Toast.makeText(context, "otro viaje", Toast.LENGTH_SHORT).show()
            }})

        estatsBtn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Toast.makeText(context, "Ir a estadisticas", Toast.LENGTH_SHORT).show()
            }})
    }

    fun toastfunction(){
        Toast.makeText(context, "simple test", Toast.LENGTH_SHORT).show()
    }

    fun recibirData (time:String, pestaneo: String, fatiga:String, bostezo:String){
        totalTime.text = time
        totalPestaneo.text = pestaneo
        totalFatiga.text = fatiga
        totalBostezo.text = bostezo
    }

}