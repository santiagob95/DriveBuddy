package com.example.reconocimientoapp


import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.fragment_face.*
import kotlinx.android.synthetic.main.fragment_face.view.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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
        startCamera()
        cameraExecutor=Executors.newSingleThreadExecutor()
        root = inflater.inflate(R.layout.fragment_face, container, false)
        return root
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
                .build()

                .also {
                    it.setAnalyzer(cameraExecutor, CustomImageAnalyzer().apply {
                        setOnLumaListener(object : CustomImageAnalyzer.LumaListener {
                            override fun setOnLumaListener(imagen: FirebaseVisionImage) {
                                requireActivity().runOnUiThread {
                                    detector.detectInImage(imagen)
                                        .addOnSuccessListener { faces ->

                                            if (faces.size != 0) {
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
                                            }

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
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUIRED_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera()
            }else{
                Log.e("e", "No se puede abrir")
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

