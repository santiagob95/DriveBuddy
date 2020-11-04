package com.example.reconocimientoapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.squareup.okhttp.internal.Internal.instance
import kotlinx.android.synthetic.main.fragment_face.*
import kotlinx.android.synthetic.main.modaldialog.*
import kotlinx.android.synthetic.main.modaldialog.view.*
import kotlinx.android.synthetic.main.pausadialog.*


class ConfiguracionDialog: DialogFragment() {
    private var NOTIFICACION: String? = null
    private var FLASH: String? = null
    private var VIBRACION: String? = null
    private var TTS: String? = null
    private var root: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);
        return inflater.inflate(R.layout.pausadialog, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            NOTIFICACION=it.getString(ConfiguracionDialog.ARG_NOTIFICACION)
            FLASH = it.getString(ConfiguracionDialog.ARG_FLASH)
            VIBRACION=it.getString(ConfiguracionDialog.ARG_VIBRACION)
            TTS = it.getString(ConfiguracionDialog.ARG_TTS)
        }
    }


    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.50).toInt()
        dialog!!.window?.setLayout(width, height)
        switchNotificacion.isChecked= NOTIFICACION?.toBoolean()!!
        switchVibracionFlash.isChecked=FLASH?.toBoolean()!!
        switchVibracion.isChecked=VIBRACION?.toBoolean()!!
        switchTextToSpeech.isChecked=TTS?.toBoolean()!!
        switchNotificacion.setOnClickListener {
            NOTIFICACION=switchNotificacion.isChecked.toString()
        }
        switchVibracion.setOnClickListener {
            VIBRACION=switchVibracion.isChecked.toString()
        }
        switchTextToSpeech.setOnClickListener {
            TTS=switchTextToSpeech.isChecked.toString()
        }
        switchVibracionFlash.setOnClickListener {
            FLASH=switchVibracionFlash.isChecked.toString()
        }
        guardarData.setOnClickListener {
            requireActivity().vibracion.text=VIBRACION
            requireActivity().notification.text=NOTIFICACION
            requireActivity().flash.text=FLASH
            requireActivity().texttospeech.text=TTS
            dismiss()
        }
    }

    companion object {
        const val TAG = "myDialog"
        private const val ARG_NOTIFICACION = "argNotificacion"
        private const val ARG_FLASH = "argFlash"
        private const val ARG_VIBRACION = "argVibracion"
        private const val ARG_TTS = "argTTS"

        fun newInstance(notificacion: String, flash: String,vibracion:String,tts:String) = ConfiguracionDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_NOTIFICACION, notificacion)
                putString(ARG_FLASH, flash)
                putString(ARG_VIBRACION, vibracion)
                putString(ARG_TTS, tts)
            }
        }
    }

}