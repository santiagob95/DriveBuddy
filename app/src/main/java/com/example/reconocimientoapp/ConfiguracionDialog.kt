package com.example.reconocimientoapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.pausadialog.*

class ConfiguracionDialog: DialogFragment() {
    private var root: View? = null
    private val vibracionBool = "vibracionBool"
    private val notificationBool = "notificationBool"
    private val textToSpeechBool = "textToSpeechBool"
    private val flashBool = "flashBool"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);
        return inflater.inflate(R.layout.pausadialog, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val prefs = getActivity()?.getSharedPreferences(getString(R.string.prefs_file),Context.MODE_PRIVATE)
        val editor = prefs!!.edit()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.50).toInt()
        dialog!!.window?.setLayout(width, height)

        switchVibracion?.let{switchVibracion.isChecked = prefs!!.getBoolean(vibracionBool, true) ?: true}
        switchVibracion?.let{switchNotificacion.isChecked = prefs!!.getBoolean(notificationBool, true) ?: true}
        switchVibracion?.let{switchTextToSpeech.isChecked = prefs!!.getBoolean(textToSpeechBool, true) ?: true}
        switchVibracion?.let{switchVibracionFlash.isChecked = prefs!!.getBoolean(flashBool, true) ?: true}

        guardarData.setOnClickListener {
            switchVibracion?.isChecked?.let { it -> editor.putBoolean(vibracionBool, it) };
            editor.commit();
            switchNotificacion?.isChecked?.let { it -> editor.putBoolean(notificationBool, it) };
            editor.commit();
            switchTextToSpeech?.isChecked?.let { it -> editor.putBoolean(textToSpeechBool, it) };
            editor.commit();
            switchVibracionFlash?.isChecked?.let { it -> editor.putBoolean(flashBool, it) };
            editor.commit();
            dismiss();
         }

    }
}