package com.example.reconocimientoapp


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.fragment_face.*
import kotlinx.android.synthetic.main.fragment_face.view.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private var auth: FirebaseAuth = Firebase.auth
private val db = FirebaseFirestore.getInstance()
class FaceFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var preview: Preview?= null
    private var camera:Camera?= null
    private val mCamera: Camera? = null
    val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()

        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        .build()


    val detector = FirebaseVision.getInstance()
        .getVisionFaceDetector(realTimeOpts)

    private var imageAnalyzer:ImageAnalysis?=null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var mTTS: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }



    }


private var root: View? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        if(allPermissionsGranted()) {
            startCamera()
            cameraExecutor = Executors.newSingleThreadExecutor()
        }else{
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_CODE_PERMISSIONS, REQUIRED_PERMISSIONS
            )

        }

        root = inflater.inflate(R.layout.fragment_face, container, false)

        return root
    }

    var inicio = false
    var pestañeos = arrayListOf<String>()
    override fun onStart() {
        super.onStart()
        iniciarViaje.setOnClickListener {
            if(inicio==false) {
                root!!.iniciarViaje.setBackgroundResource(R.drawable.finalv)
                root!!.duracionViaje.setBase(SystemClock.elapsedRealtime())
                root!!.duracionViaje.start()
                inicio = true

            }
            else {
                root!!.iniciarViaje.setBackgroundResource(R.drawable.inicio)
                root!!.duracionViaje.stop()
                showAlert(pestañeos.size)

            }
        }
    }
    fun Fragment.vibratePhone() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)//esto es para la fecha
    private fun showAlert(pestañeos: Number){
        var totalSegundos = ((SystemClock.elapsedRealtime()-duracionViaje.base)/1000).toInt()
        var minutos=0
        var horas=0
        if(totalSegundos>=60){
            minutos= totalSegundos/60
            totalSegundos=totalSegundos-(minutos*60)
            if(minutos>=60){
                horas=minutos/60
                minutos=minutos-(horas*60)
            }
        }
        var h : String
        var s : String
        var m: String
        if(horas<10){
            h="0"+horas.toString()
        }else{
            h=horas.toString()
        }
        if(minutos<10){
            m="0"+minutos.toString()
        }else{
            m=minutos.toString()
        }
        if(totalSegundos<10){
            s="0"+totalSegundos.toString()
        }else{
            s=totalSegundos.toString()
        }

        var duracion = h+":"+m+":"+s
        var fatigas = (pestañeos.toInt()/3)
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Estadisticas del viaje")

        builder.setMessage("Duracion del viaje: $duracion\nCantidad de pestañeos largos: $pestañeos\n Cantidad de fatigas detectadas: $fatigas")
        val stats = hashMapOf(
            "Fatiga" to fatigas,
            "Bostezo" to 1,
            "PestaneoLargo" to pestañeos,
            "kmRecorrido" to 1,
            "tiempoTotal" to h.toInt()*60 + m.toInt() +6,
            "velocidadMedia" to 1,
            "id" to auth.currentUser!!.uid,
            "fecha" to LocalDateTime.now().toString()
        )
        builder.setPositiveButton("aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()


        db.collection("viajes").document()
            .set(stats)
            .addOnSuccessListener { Log.v("setViaje","Viaje guardado correctamente") }
            .addOnFailureListener { e -> Log.w("setViaje", "Error subiendo el viaje",e) }

    }


    var inicioContador=false



    private fun startCamera(){

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()

            val imageCapture = ImageCapture.Builder().build()
            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(100, 100))
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, CustomImageAnalyzer().apply {
                        setOnLumaListener(object : CustomImageAnalyzer.LumaListener {
                            override fun setOnLumaListener(imagen: FirebaseVisionImage) {
                                requireActivity().runOnUiThread {
                                    if (inicioContador == true && ((SystemClock.elapsedRealtime() - contador.getBase()) / 1000) >= 2 && inicio == true) {
                                        val notification: Uri =
                                            RingtoneManager.getDefaultUri(
                                                RingtoneManager.TYPE_NOTIFICATION
                                            )
                                        val r = RingtoneManager.getRingtone(
                                            getApplicationContext(),
                                            notification
                                        )
                                        r.play()
                                        vibratePhone()
                                        pestañeos.add(((((SystemClock.elapsedRealtime() - duracionViaje.getBase()) / 1000) / 60).toString()))
                                        /*mTTS = TextToSpeech(requireActivity(),TextToSpeech.OnInitListener { status->
                                            t.text=status.toString()
                                        })
                                        //mTTS.language= Locale.ROOT

                                        mTTS!!.speak("Are you sleeping", TextToSpeech.QUEUE_FLUSH, null,"")*/
                                        inicioContador = false
                                        root!!.contador.setBase(SystemClock.elapsedRealtime())
                                    }
                                    if (inicio == true) {
                                        detector.detectInImage(imagen)
                                            .addOnSuccessListener { faces ->
                                                if (faces.size != 0) {


                                                    if (faces[0].leftEyeOpenProbability < 0.3 && faces[0].rightEyeOpenProbability < 0.3) {
                                                        if (inicioContador == false) {
                                                            inicioContador = true
                                                            root!!.contador.setBase(SystemClock.elapsedRealtime())
                                                            root!!.contador.start()
                                                        }
                                                    } else {
                                                        inicioContador = false
                                                        root!!.contador.stop()
                                                    }

                                                }
                                            }
                                    } else {
                                        inicioContador = false
                                        root!!.contador.stop()
                                    }
                                    /*if (faces.size != 0) {
                                                        root!!.caracorrecto.visibility = View.VISIBLE
                                                        root!!.caraincorrecto.visibility = View.GONE
                                                    if (faces[0].rightEyeOpenProbability < 0.1000 || faces[0].leftEyeOpenProbability < 0.1000) {
                                                        root!!.ojoscerrados.visibility = View.VISIBLE
                                                        root!!.ojosabiertos.visibility = View.GONE
                                                        val notification: Uri =
                                                            RingtoneManager.getDefaultUri(
                                                                RingtoneManager.TYPE_NOTIFICATION
                                                            )
                                                        val r = RingtoneManager.getRingtone(
                                                            getApplicationContext(),
                                                            notification
                                                        )
                                                        r.play()
                                                    }else{
                                                        root!!.ojoscerrados.visibility = View.GONE
                                                        root!!.ojosabiertos.visibility = View.VISIBLE
                                                    }
                                                    if (faces[0].smilingProbability > 0.3777) {
                                                        root!!.sonrisabien.visibility = View.VISIBLE
                                                        root!!.sonrisamal.visibility = View.GONE
                                                        val notification: Uri =
                                                            RingtoneManager.getDefaultUri(
                                                                RingtoneManager.TYPE_NOTIFICATION
                                                            )
                                                        val r = RingtoneManager.getRingtone(
                                                            getApplicationContext(),
                                                            notification
                                                        )
                                                        r.play()
                                                    }else{
                                                        root!!.sonrisabien.visibility = View.GONE
                                                        root!!.sonrisamal.visibility = View.VISIBLE
                                                    }
    //                                                cara.text = "Reconocido correcto"
    //                                                if (faces[0].smilingProbability > 0.6777) {
    //                                                    sonrisa.text =
    //                                                        "sonrisa" + "%.3f".format(faces[0].smilingProbability)
    //                                                    val notification: Uri =
    //                                                        RingtoneManager.getDefaultUri(
    //                                                            RingtoneManager.TYPE_NOTIFICATION
    //                                                        )
    //                                                    val r = RingtoneManager.getRingtone(
    //                                                        getApplicationContext(),
    //                                                        notification
    //                                                    )
    //                                                    r.play()
    //                                                } else {
    //                                                    sonrisa.text = "NaN"
    //                                                }
    //                                                if (faces[0].rightEyeOpenProbability < 0.1000 || faces[0].leftEyeOpenProbability < 0.1000) {
    //                                                    val notification: Uri =
    //                                                        RingtoneManager.getDefaultUri(
    //                                                            RingtoneManager.TYPE_NOTIFICATION
    //                                                        )
    //                                                    val r = RingtoneManager.getRingtone(
    //                                                        getApplicationContext(),
    //                                                        notification
    //                                                    )
    //                                                    r.play()
    //                                                } else {
    //                                                    ojod.text =
    //                                                        "ojo derecho" + "%.3f".format(faces[0].rightEyeOpenProbability)
    //                                                    ojoi.text =
    //                                                        "ojo izquierdo" + "%.3f".format(faces[0].leftEyeOpenProbability)
    //                                                }
    //                                                ojod.text =
    //                                                    "ojo derecho" + "%.3f".format(faces[0].rightEyeOpenProbability)
    //                                                ojoi.text =
    //                                                    "ojo izquierdo" + "%.3f".format(faces[0].leftEyeOpenProbability)
    //                                                sonrisa.text =
    //                                                    "sonrisa" + "%.3f".format(faces[0].smilingProbability)
    //                                            } else {
    //                                                cara.text = "Reconocido incorrecto"
    //                                                ojod.text = "NaN"
    //                                                ojoi.text = "NaN"
    //                                                sonrisa.text = "NaN"
                                                }else{
                                                        root!!.caraincorrecto.visibility = View.VISIBLE
                                                        root!!.caracorrecto.visibility = View.GONE
                                                    root!!.ojosabiertos.visibility = View.GONE
                                                    root!!.ojoscerrados.visibility = View.GONE
                                                    root!!.sonrisamal.visibility= View.GONE
                                                    root!!.sonrisabien.visibility=View.GONE
                                                }*/


                                }
                            }


                        })
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()



            try {
                // Unbind use cases before rebinding

                cameraProvider.unbindAll()
                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
                preview?.setSurfaceProvider(previewView.surfaceProvider)

            } catch (exc: Exception) {
                Log.e("a", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private class CustomImageAnalyzer:ImageAnalysis.Analyzer{
        private lateinit var mListener:LumaListener



        interface LumaListener{
            fun setOnLumaListener(imagen: FirebaseVisionImage)
        }

        fun setOnLumaListener(mListener: LumaListener){
            this.mListener=mListener
        }



        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy?.image
            if (mediaImage != null) {
                val image = FirebaseVisionImage.fromMediaImage(mediaImage, Surface.ROTATION_270)
                mListener.setOnLumaListener(image)
                imageProxy.close()
            }


        }

        fun Image.toBitmap(): Bitmap {
            val yBuffer = planes[0].buffer // Y
            val uBuffer = planes[1].buffer // U
            val vBuffer = planes[2].buffer // V

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            //U and V are swapped
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

    }

    private fun allPermissionsGranted()= REQUIRED_CODE_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        )==PackageManager.PERMISSION_GRANTED

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUIRED_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG="Camerax"
        private const val REQUIRED_PERMISSIONS=10
        private val REQUIRED_CODE_PERMISSIONS= arrayOf(android.Manifest.permission.CAMERA)
        @JvmStatic fun newInstance(param1: String, param2: String) =
            FaceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

