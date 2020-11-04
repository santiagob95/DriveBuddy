package com.example.reconocimientoapp


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.media.AudioManager
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
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.reconocimientoapp.FaceFragment.Companion.newInstance
import com.example.reconocimientoapp.MyCustomDialog.Companion.newInstance
import com.facebook.FacebookSdk.getApplicationContext
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.fragment_face.*
import kotlinx.android.synthetic.main.fragment_face.view.*
import kotlinx.android.synthetic.main.modaldialog.*
import kotlinx.android.synthetic.main.modaldialog.view.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.ByteArrayOutputStream
import java.lang.reflect.Array.newInstance
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.properties.Delegates


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private var auth: FirebaseAuth = Firebase.auth
private val db = FirebaseFirestore.getInstance()
class FaceFragment : Fragment() ,EasyPermissions.PermissionCallbacks,EasyPermissions.RationaleCallbacks, TextToSpeech.OnInitListener {

    private val LOCATION_PERM=124

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var animacion = false
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

    private var isDone:Boolean by Delegates.observable(false){property, oldValue, newValue ->
        if(newValue){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private var imageAnalyzer:ImageAnalysis?=null
    private lateinit var cameraExecutor: ExecutorService
    private  var mTTS: TextToSpeech?=null
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
            fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(requireContext())
            askForLocationPermission()
            createLocationRequest()

            locationCallback = object :LocationCallback(){
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?:return
                    if(!isDone){
                        val speedToInt = locationResult.lastLocation.speed.toInt()
                        calcSpeed(speedToInt)

                    }else{
                        root!!.speeds.text="jeje"
                    }
                }
            }
            mTTS= TextToSpeech(context,this)
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

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun hasLocationPermissions():Boolean{
        return EasyPermissions.hasPermissions(requireContext(),android.Manifest.permission.ACCESS_FINE_LOCATION)
    }


    private fun calcSpeed(speed:Int){
        root!!.speeds.text=speed.toString()+"km/h"

    }

    private fun startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }



    private fun askForLocationPermission(){

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if(hasLocationPermissions())
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->

                }
        else{
            EasyPermissions.requestPermissions(
                this,
                "Necesitamos permiso de tu ubicacion para acceder a la velocidad en la que te mueves",
                LOCATION_PERM,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun createLocationRequest(){
        locationRequest=LocationRequest.create().apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    var inicio = false
    var pestañeos = arrayListOf<Int>()
    var bostezos= arrayListOf<Int>()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        var cambio = false
        cambioface.setOnClickListener{
            if (cambio == false) {
                root!!.back_fondo.visibility = View.VISIBLE
                cambio = true
            }else{
                root!!.back_fondo.visibility = View.INVISIBLE
                cambio = false
            }
        }


        configuracion.setOnClickListener {
            val fragManager: FragmentManager = (activity as AppCompatActivity).supportFragmentManager
            val dialog = ConfiguracionDialog()
            dialog.show(fragManager , "OpcionesFragment")
        }


        iniciarViaje.setOnClickListener {
            if(inicio==false) {
                val audio: AudioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val currentVolume: Int = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVolume: Int = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val percent = 1.0f
                val seventyVolume = (maxVolume * percent).toInt()
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0)
                mTTS!!.speak("Drive Buddy te desea un buen viaje!", TextToSpeech.QUEUE_FLUSH, null)
                root!!.iniciarViaje.setBackgroundResource(R.drawable.stop)
                root!!.pausarViaje.visibility = View.VISIBLE
                root!!.configuracion.visibility = View.GONE
                root!!.duracionViaje.setBase(SystemClock.elapsedRealtime())
                root!!.duracionViaje.start()
                inicio = true

            }
            else {
                root!!.iniciarViaje.setBackgroundResource(R.drawable.start)
                root!!.pausarViaje.visibility = View.INVISIBLE
                root!!.configuracion.visibility = View.VISIBLE
                root!!.duracionViaje.stop()
                inicio=false
                postStats()
                customModal()

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

    fun customModal() {
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
        val fragManager: FragmentManager = (activity as AppCompatActivity).supportFragmentManager
        val dialog = MyCustomDialog.newInstance(duracion,bostezos.size.toString(),(pestañeos.size/3).toString(),pestañeos.size.toString())

        dialog.show(fragManager , "MyCustomFragment")
        bostezos.clear()
        pestañeos.clear()

    }

    fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (Math.random() * (end - start + 1)).toInt() + start
    }

    @RequiresApi(Build.VERSION_CODES.O)//esto es para la fecha
    private fun postStats(){

        val fatigas = (pestañeos.size/3)
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val stats = hashMapOf(
            "Fatiga" to fatigas,//fatigas
            "Bostezo" to bostezos.size,
            "PestaneoLargo" to pestañeos.size, //cantPest
            "kmRecorrido" to rand(90,650),
            "tiempoTotal" to (((rand(4500,36000))/100.0)/60), //entre 45 min y 6 horas
            "velocidadMedia" to rand(20,180),
            "id" to auth.currentUser!!.uid,
            "fecha" to LocalDateTime.now().toString()
        )
        db.collection("viajes").document()
            .set(stats)
            .addOnSuccessListener { Log.v("setViaje","Viaje guardado correctamente") }
            .addOnFailureListener { e -> Log.w("setViaje", "Error subiendo el viaje",e)
            }


    }


    var inicioContador=false
    var inicioContadorBostezos=false


    fun View.blink(
        times: Int = Animation.INFINITE,
        duration: Long = 120L,
        offset: Long = 20L,
        minAlpha: Float = 0.0f,
        maxAlpha: Float = 1.0f,
        repeatMode: Int = Animation.REVERSE
    ) {
        root!!.alerta_flash.visibility = View.VISIBLE
        startAnimation(AlphaAnimation(minAlpha, maxAlpha).also {
            it.duration = duration
            it.startOffset = offset
            it.repeatMode = repeatMode
            it.repeatCount = times
        })
        root!!.alerta_flash.visibility = View.INVISIBLE
    }

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
                                        mTTS!!.speak("Abre esos ojos! no te quedes dormido",TextToSpeech.QUEUE_FLUSH,null)
                                        pestañeos.add(((((SystemClock.elapsedRealtime() - duracionViaje.getBase()) / 1000) / 60).toInt()))
                                        inicioContador = false
                                        alerta_flash.blink(20)
                                        root!!.contador.setBase(SystemClock.elapsedRealtime())
                                    }
                                    if (inicioContadorBostezos == true && ((SystemClock.elapsedRealtime() - contadorBostezo.getBase()) / 1000) >= 2 && inicio == true) {
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
                                        mTTS!!.speak("Bostezando? estás con sueño?",TextToSpeech.QUEUE_FLUSH,null)
                                        bostezos.add(((((SystemClock.elapsedRealtime() - duracionViaje.getBase()) / 1000) / 60).toInt()))
                                        inicioContador = false
                                        alerta_flash.blink(20)
                                        root!!.contadorBostezo.setBase(SystemClock.elapsedRealtime())
                                    }
                                    if (inicio == true) {
                                        detector.detectInImage(imagen)
                                            .addOnSuccessListener { faces ->
                                                if (faces.size != 0) {

                                                    root!!.recOk.setBackgroundResource(R.drawable.reconocimientook)
                                                    if ((faces[0].leftEyeOpenProbability < 0.3 && faces[0].rightEyeOpenProbability < 0.3)) {
                                                        if (inicioContador == false) {
                                                            inicioContador = true
                                                            root!!.contador.setBase(SystemClock.elapsedRealtime())
                                                            root!!.contador.start()
                                                        }
                                                    } else {
                                                        inicioContador = false
                                                        root!!.contador.stop()
                                                    }
                                                    if ((faces[0].smilingProbability>0.66)) {
                                                        if (inicioContadorBostezos == false) {
                                                            inicioContadorBostezos = true
                                                            root!!.contadorBostezo.setBase(SystemClock.elapsedRealtime())
                                                            root!!.contadorBostezo.start()
                                                        }
                                                    } else {
                                                        inicioContadorBostezos = false
                                                        root!!.contadorBostezo.stop()
                                                    }

                                                }else{
                                                    root!!.recOk.setBackgroundResource(R.drawable.reconocimientobad)
                                                }
                                            }
                                    } else {
                                        inicioContador = false
                                        inicioContadorBostezos=false
                                        root!!.contadorBostezo.stop()
                                        root!!.contador.stop()
                                    }

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
                val image = FirebaseVisionImage.fromMediaImage(mediaImage,Surface.ROTATION_270)
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
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(requireActivity(),perms)){
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){
            val yes = "Allow"
            val no= "Deny"

        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        TODO("Not yet implemented")
    }

    override fun onRationaleDenied(requestCode: Int) {
        TODO("Not yet implemented")
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

    override fun onInit(p0: Int) {

    }


}