package com.example.reconocimientoapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.squareup.okhttp.internal.Internal.instance
import kotlinx.android.synthetic.main.modaldialog.*
import kotlinx.android.synthetic.main.modaldialog.view.*


class MyCustomDialog: DialogFragment() {
    private var duracion: String? = null
    private var pestañeos: String? = null
    private var fatiga: String? = null
    private var bostezo: String? = null
    var displayMessage: String? = ""
    private var root: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);
        return inflater.inflate(R.layout.modaldialog, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            duracion=it.getString(ARG_DURACION)
            pestañeos = it.getString(ARG_PESTAÑEOS)
            fatiga=it.getString(ARG_FATIGA)
            bostezo = it.getString(ARG_BOSTEZO)
        }
    }
    override fun onStart() {
        super.onStart()
        totalTime.text=duracion
        totalPestaneo.text=pestañeos
        totalFatiga.text=fatiga
        totalBostezo.text=bostezo
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, height)


        otroBtn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                dismiss()
            }})

        estatsBtn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = activity!!.intent
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
                activity!!.overridePendingTransition(0, 0)
                activity!!.finish()

                activity!!.overridePendingTransition(0, 0)
                startActivity(intent)
            }})
    }




    companion object {
        const val TAG = "myDialog"
        private const val ARG_DURACION = "argDuracion"
        private const val ARG_BOSTEZO = "argBostezo"
        private const val ARG_FATIGA = "argFatiga"
        private const val ARG_PESTAÑEOS = "argPestañeos"

        fun newInstance(duracion: String, bostezo: String,fatiga:String,pesta:String) = MyCustomDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_DURACION, duracion)
                putString(ARG_BOSTEZO, bostezo)
                putString(ARG_FATIGA, fatiga)
                putString(ARG_PESTAÑEOS, pesta)
            }
        }
    }

}