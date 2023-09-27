package com.example.scaner


import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.example.scaner.databinding.ActivityMainBinding
import com.google.mlkit.vision.barcode.common.Barcode

class MainActivity : AppCompatActivity() {

    private val cameraPermission = android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startScanner()
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            requestCameraAndStartScanner()
        }
        binding.buttonFace.setOnClickListener {
            FaceDetectionActivity.start(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestCameraAndStartScanner(){
        if(isPermissionGranted(cameraPermission)) {
            startScanner()
        } else {
            requestCameraPermission()
        }
    }

    private fun startScanner() {
        ScannerActivity.startScanner(this) {barcodes ->
            barcodes.forEach { barcode ->
                when(barcode.valueType){
                    Barcode.TYPE_URL -> {
                        binding.success.isVisible = true
                        binding.QRType.text = "URL"
                        binding.QRContent.text = barcode.rawValue.toString()
                    }
                    Barcode.TYPE_CONTACT_INFO -> {
                        binding.success.isVisible = true
                        binding.QRType.text = "Contact"
                        binding.QRContent.text = barcode.rawValue.toString()
                    }
                    else -> {
                        binding.success.isVisible = true
                        binding.QRType.text = "Other"
                        binding.QRContent.text = barcode.rawValue.toString()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest {
                    openPermissionSettings()
                }
            } else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }

}