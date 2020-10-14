package com.example.reconocimientoapp


import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorDirectChannel
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Chronometer
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.impl.CaptureProcessor
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraView
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.preview.CameraPreview
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_face.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
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
        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
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



    override fun onPause() {
        super.onPause()
        startCamera()
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


    @SuppressLint("WrongConstant")
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

                                            if(faces.size!=0) {
                                                val arriba = faces[0].getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).points
                                                val abajo = faces[0].getContour(FirebaseVisionFaceContour.LOWER_LIP_TOP).points
                                                mux.text=arriba[4].x.toString()
                                                muy.text=arriba[4].y.toString()
                                                max.text=abajo[4].x.toString()
                                                may.text=abajo[4].y.toString()
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
                    this as LifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
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