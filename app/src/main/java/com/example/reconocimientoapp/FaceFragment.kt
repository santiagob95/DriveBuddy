package com.example.reconocimientoapp


import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.impl.CaptureProcessor
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.fragment_face.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FaceFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var preview:Preview?= null
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



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        startCamera()
        cameraExecutor=Executors.newSingleThreadExecutor()
        return inflater.inflate(R.layout.fragment_face, container, false)
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
                .setTargetResolution(Size(140,140))
                .build()

                .also {
                    it.setAnalyzer(cameraExecutor, CustomImageAnalyzer().apply {
                        setOnLumaListener(object : CustomImageAnalyzer.LumaListener {
                            override fun setOnLumaListener(imagen: FirebaseVisionImage) {
                                requireActivity().runOnUiThread {
                                    detector.detectInImage(imagen)
                                        .addOnSuccessListener { faces ->

                                            if(faces.size!=0){
                                                cara.text ="Reconocido correcto"
                                                ojod.text="ojo derecho"+"%.2f".format(faces[0].rightEyeOpenProbability)
                                                ojoi.text="ojo izquierdo"+"%.2f".format(faces[0].leftEyeOpenProbability)
                                                sonrisa.text="sonrisa"+"%.2f".format(faces[0].smilingProbability)
                                            }else{
                                                cara.text ="Reconocido incorrecto"
                                                ojod.text="NaN"
                                                ojoi.text="NaN"
                                                sonrisa.text="NaN"
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
                val image = FirebaseVisionImage.fromMediaImage(mediaImage,0)
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