package com.example.reconocimientoapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.squareup.okhttp.internal.Internal.instance
import kotlinx.android.synthetic.main.activity_welcome__screen.*
import kotlinx.android.synthetic.main.activity_welcome__screen.welcomeBtn
import kotlinx.android.synthetic.main.modaldialog.*
import kotlinx.android.synthetic.main.modaldialog.view.*
import kotlinx.android.synthetic.main.pausadialog.*
import java.security.KeyStore


class ConfiguracionDialog: DialogFragment() {
    private var root: View? = null
    private val prefs = activity?.getSharedPreferences(getString(R.string.prefs_file),Context.MODE_PRIVATE)!!
    private val vibracionBool = "vibracionBool"
    private val notificacionBool = "notificacionBool"
    private val textToSpeechBool = "textToSpeechBool"
    private val vibracionFlashBool = "vibracionFlashBool"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);

        switchVibracion.isChecked = prefs.getString(vibracionBool, null)?.toBoolean() ?: true
        switchNotificacion.isChecked = prefs.getString(notificacionBool,null)?.toBoolean() ?: true
        switchTextToSpeech.isChecked = prefs.getString(textToSpeechBool, null)?.toBoolean() ?: true
        switchVibracionFlash.isChecked = prefs.getString(vibracionFlashBool,null )?.toBoolean() ?: true

        return inflater.inflate(R.layout.pausadialog, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.50).toInt()
        dialog!!.window?.setLayout(width, height)

        guardarData.setOnClickListener {
            prefs.edit().putString(vibracionBool, switchVibracion.isChecked.toString())
            prefs.edit().putString(notificacionBool, switchNotificacion.isChecked.toString())
            prefs.edit().putString(textToSpeechBool, switchTextToSpeech.isChecked.toString())
            prefs.edit().putString(vibracionFlashBool, switchVibracionFlash.isChecked.toString())
            prefs.edit().apply()
        }
    }
}