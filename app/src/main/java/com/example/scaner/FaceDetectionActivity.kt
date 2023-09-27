package com.example.scaner

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.scaner.databinding.ActivityFaceDetectionBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

class FaceDetectionActivity : AppCompatActivity() {

    lateinit var binding: ActivityFaceDetectionBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {
                processCameraProvider = cameraProviderFuture.get()
                bindCameraPreview()
                bindInputAnalyser()
            }, ContextCompat.getMainExecutor(this)
        )
    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)

        processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
    }

    private fun bindInputAnalyser() {

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build()
        )

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()


        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(detector, imageProxy)
        }

        processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        detector: com.google.mlkit.vision.face.FaceDetector,
        imageProxy: ImageProxy
    ){
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener {faces ->
                binding.faceBoxOverlay.clear()

                faces.forEach{face ->  
                    val box = FaceBox(binding.faceBoxOverlay, face, imageProxy.cropRect)
                    binding.faceBoxOverlay.add(box)
                }

            }.addOnFailureListener{

            }.addOnCompleteListener {
                imageProxy.close()
            }
    }

    companion object {
        fun start(context: Context) {
            Intent(context, FaceDetectionActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }
}