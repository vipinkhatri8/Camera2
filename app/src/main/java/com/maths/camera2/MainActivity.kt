package com.maths.camera2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_REQUEST = 1888
        private const val GALLERY_REQUEST_CODE = 1000
        private const val REQUEST_WRITE_STORAGE = 112
        private const val REQUEST_CAMERA = 113
    }

    private lateinit var mBottomSheetDialog: BottomSheetDialog
    private lateinit var mContext: Context
    private lateinit var imgAttachment: ImageView
    private var file2: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this
        imgAttachment = findViewById(R.id.imgAttachment)
        val btnOpenBottomSheet: Button = findViewById(R.id.btnOpenBottomSheet)

        btnOpenBottomSheet.setOnClickListener { bottomSheetDialog() }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        }
    }

    private fun bottomSheetDialog() {
        mBottomSheetDialog = BottomSheetDialog(mContext)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
        mBottomSheetDialog.setContentView(sheetView)
        val llCamera: LinearLayout = sheetView.findViewById(R.id.ll_camera)
        val llGallery: LinearLayout = sheetView.findViewById(R.id.ll_gallery)

        llCamera.setOnClickListener { getCamera() }
        llGallery.setOnClickListener { getGallery() }
        mBottomSheetDialog.show()
    }

    private fun getGallery() {
        mBottomSheetDialog.dismiss()
        val intent1 = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent1, GALLERY_REQUEST_CODE)
    }

    private fun getCamera() {
        mBottomSheetDialog.dismiss()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        file2 = getOutputMediaFile()
        if (file2 != null) {
            val uri = FileProvider.getUriForFile(mContext, "${applicationContext.packageName}.provider", file2!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intent, CAMERA_REQUEST)
        } else {
            Log.e("MainActivity", "Error creating media file, check storage permissions.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    if (file2 != null) {
                        val myBitmap = BitmapFactory.decodeFile(file2!!.absolutePath)
                        imgAttachment.setImageBitmap(myBitmap)
                        compressImage(myBitmap)
                    } else {
                        Log.e("MainActivity", "File is null in CAMERA_REQUEST")
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        try {
                            val inputStream = contentResolver.openInputStream(selectedImageUri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            imgAttachment.setImageBitmap(bitmap)
                            compressImage(bitmap)
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                    } else {
                        Log.e("MainActivity", "Selected image URI is null in GALLERY_REQUEST_CODE")
                    }
                }
            }
        }
    }

    private fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CameraDemo")

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(mediaStorageDir.path + File.separator + "IMG_$timeStamp.jpg")
    }

    private fun compressImage(bitmap: Bitmap) {
        val MAX_IMAGE_SIZE = 1000 * 1024
        var streamLength = MAX_IMAGE_SIZE
        var compressQuality = 80
        val bmpStream = ByteArrayOutputStream()
        while (streamLength >= MAX_IMAGE_SIZE && compressQuality > 1) {
            try {
                bmpStream.flush()
                bmpStream.reset()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            compressQuality -= 1
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            Log.d("test upload", "Quality: $compressQuality")
            Log.d("test upload", "Size: $streamLength")
        }

        if (file2 != null) {
            try {
                val fo = FileOutputStream(file2!!)
                fo.write(bmpStream.toByteArray())
                fo.flush()
                fo.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Log.e("MainActivity", "File is null when trying to compress image")
        }
    }
}


/*
package com.maths.camera2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_REQUEST = 1888
        private const val GALLERY_REQUEST_CODE = 1000
        private const val REQUEST_WRITE_STORAGE = 112
        private const val REQUEST_CAMERA = 113
    }

    private lateinit var mBottomSheetDialog: BottomSheetDialog
    private lateinit var mContext: Context
    private lateinit var imgAttachment: ImageView
    private var file2: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this
        imgAttachment = findViewById(R.id.imgAttachment)
        val btnOpenBottomSheet: Button = findViewById(R.id.btnOpenBottomSheet)

        btnOpenBottomSheet.setOnClickListener { bottomSheetDialog() }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        }
    }

    private fun bottomSheetDialog() {
        mBottomSheetDialog = BottomSheetDialog(mContext)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
        mBottomSheetDialog.setContentView(sheetView)
        val llCamera: LinearLayout = sheetView.findViewById(R.id.ll_camera)
        val llGallery: LinearLayout = sheetView.findViewById(R.id.ll_gallery)

        llCamera.setOnClickListener { getCamera() }
        llGallery.setOnClickListener { getGallery() }
        mBottomSheetDialog.show()
    }

    private fun getGallery() {
        mBottomSheetDialog.dismiss()
        val intent1 = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent1, GALLERY_REQUEST_CODE)
    }

    private fun getCamera() {
        mBottomSheetDialog.dismiss()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        file2 = getOutputMediaFile()
        val uri = FileProvider.getUriForFile(mContext, "${applicationContext.packageName}.provider", file2!!)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, CAMERA_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    val myBitmap = BitmapFactory.decodeFile(file2!!.absolutePath)
                    imgAttachment.setImageBitmap(myBitmap)
                    compressImage(myBitmap)
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri = data?.data
                    try {
                        val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imgAttachment.setImageBitmap(bitmap)
                        compressImage(bitmap)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CameraDemo")

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(mediaStorageDir.path + File.separator + "IMG_$timeStamp.jpg")
    }


    private fun compressImage(bitmap: Bitmap) {
        val MAX_IMAGE_SIZE = 1000 * 1024
        var streamLength = MAX_IMAGE_SIZE
        var compressQuality = 80
        val bmpStream = ByteArrayOutputStream()
        while (streamLength >= MAX_IMAGE_SIZE && compressQuality > 1) {
            try {
                bmpStream.flush()
                bmpStream.reset()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            compressQuality -= 1
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            Log.d("test upload", "Quality: $compressQuality")
            Log.d("test upload", "Size: $streamLength")
        }

        try {
            val fo = FileOutputStream(file2!!)
            fo.write(bmpStream.toByteArray())
            fo.flush()
            fo.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
*/