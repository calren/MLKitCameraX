package com.sample.android.cameraxmlkit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.sample.android.cameraxmlkit.databinding.ActivityMainBinding
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var bitmapBuffer: Bitmap
    private var imageRotationDegrees: Int = 0

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private val detector: FirebaseVisionBarcodeDetector by lazy {
        FirebaseVision.getInstance().visionBarcodeDetector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        handlePermissions()
    }

    fun handlePermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA
            )
        } else {
            Log.i(TAG, "All Permissions Granted Already")
            bindCameraUseCases()
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindCameraUseCases() {
        Log.i(TAG, "Binding camera use cases")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            Log.i(TAG, "Camera provider ready")

            val cameraProvider = cameraProviderFuture.get()

            // Set up the view finder use case to display camera preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .setTargetRotation(binding.previewView.display.rotation)
                .build()

            // Set up the image analysis use case which will process frames in real time
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .setTargetRotation(binding.previewView.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // TODO set imageAnalysis analyzer

            // Create a new camera selector each time, enforcing lens facing
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            // Apply declared configs to CameraX using the same lifecycle owner
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis
            )

            // Use the camera object to link our preview use case with the view
            preview.setSurfaceProvider(binding.previewView.createSurfaceProvider(camera.cameraInfo))


        }, ContextCompat.getMainExecutor(this))

        binding.previewView.post {


            //        cameraProviderFuture.addListener(Runnable {
//
//            val cameraProvider = cameraProviderFuture.get()
//
//            // Set up the view finder use case to display camera preview
//            val preview = Preview.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .setTargetRotation(binding.previewView.display.rotation)
//                .build()
//
//            // Set up the image analysis use case which will process frames in real time
//            val imageAnalysis = ImageAnalysis.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .setTargetRotation(binding.previewView.display.rotation)
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .build()
//
//            imageAnalysis.setAnalyzer(executor, ImageAnalysis.Analyzer { image ->
//                // TODO handle image in pause state?
//                Log.i(TAG, "Image detected")
//
//                // Do analysis and reporting here
//                image.image?.also {
//                    detector.detectInImage(FirebaseVisionImage.fromMediaImage(it, 0))
//                        .addOnSuccessListener {
//                            for (barcode in it) {
//                                Log.i(TAG, "Barcode detected: ${barcode.displayValue}")
//                            }
//                        }
//                        .addOnFailureListener {
//                            Log.i(TAG, "Unsuccessful barcode detection ${it.stackTrace}")
//                        }
//                }
//            })
//
//            // Create a new camera selector each time, enforcing lens facing
//            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
//
//            // Apply declared configs to CameraX using the same lifecycle owner
//            cameraProvider.unbindAll()
//            val camera = cameraProvider.bindToLifecycle(
//                this as LifecycleOwner, cameraSelector, preview, imageAnalysis
//            )
//
//            // Use the camera object to link our preview use case with the view
//            preview.setSurfaceProvider(binding.previewView.createSurfaceProvider(camera.cameraInfo))
//
//        }, ContextCompat.getMainExecutor(this))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.i(TAG, "Camera permission granted")
                    bindCameraUseCases()
                } else {
                    Log.e(TAG, "Camera permission NOT granted")
                }
                return
            }
        }
    }
}

private const val TAG = "MainActivity"
private const val MY_PERMISSIONS_REQUEST_CAMERA = 1
