package com.example.reconocimientoapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.squareup.okhttp.internal.Internal.instance
import kotlinx.android.synthetic.main.modaldialog.*


class MyCustomDialog: DialogFragment() {

    var displayMessage: String? = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);
        return inflater.inflate(R.layout.modaldialog, container, false)
    }

    override fun onStart() {
        super.onStart()
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



    fun recibirData (){

    }

}