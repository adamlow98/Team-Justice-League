package com.example.PainRate

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.example.PainRate.accessingnet.PostClass
import com.example.PainRate.model.AnalysisResult
import com.example.PainRate.utils.JsonMapper
import java.io.File

private const val REQUEST_CODE = 1
private const val FILE_NAME = "photo.jpg"



// photo file variable representing the location where the camera application should write the image captured
private lateinit var photoFile: File

class Scanning : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning)

        // Policy issue
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())

        // on-click listener for button which will go back to the dev page (homepage)
        val imgvwHome = findViewById<ImageView>(R.id.imgvwHome)
        imgvwHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // on-click listener for button which will launch the intent to open the camera
        val btnCapture = findViewById<Button>(R.id.btnCapture)
        btnCapture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            // specifies where the photo taken lives
            val fileProvider = FileProvider.getUriForFile(this, "com.example.PainRate.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            // checks if there's any camera application on the device
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                // means that it is valid and can call successfully
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            }
            else {
                // means that it is not valid
                Toast.makeText(this, "Unable to open the camera", Toast.LENGTH_SHORT).show()
            }
        }

        // on-click listener for button which will go to the results page
        val btnStartAnalysis = findViewById<Button>(R.id.btnStartAnalysis)
        btnStartAnalysis.setOnClickListener {
            val intent = Intent(Scanning@this, Results::class.java)

            // Get result from assessment server
            val conn = PostClass()

            // Sending image to
            val bPath = "C:/Users/Adam/Team-Justice-League/PainRate/app/src/main/assets"
            val bFile = File(bPath)
            if(!bFile.exists()) {
                println("File not found!")
                Log.i("NumberGenerated", "File not found!1");
            } else {
                println("File found!")
                conn.BaseOkHttp(bFile)
            }

            // Sending image to assessment module
            val path = "C:/Users/Adam/Team-Justice-League/PainRate/app/src/main/assets"
            val file = File(path)
            var analysisResult: AnalysisResult? = null
            if(!file.exists()) {
                println("File not found!")
                Log.i("NumberGenerated", "File not found!2");
            } else {
                val result= conn.clientOkHttp(file)
                println(result)
                Log.i("NumberGenerated", result);
                analysisResult = JsonMapper.mapToAnalysisResult(result)
            }

            // Include data into intent and call activity
            intent.putExtra(Results.RE, analysisResult)
            startActivity(intent)
        }
    }

    private fun getPhotoFile(fileName: String): File {
        // Use 'getExternalFileDir' on Context to access package-specific directories.
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // check request code and result code
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // means successfully taken a picture from the camera

            // reads image from specified file to put to the ImageView
            // val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            val path = "C:/Users/Adam/Team-Justice-League/PainRate/app/src/main/assets"
            val file = File(path)
            val takenImage = BitmapFactory.decodeFile(file.absolutePath)
            val imageView = findViewById<ImageView>(R.id.imgvwPhoto)
            imageView.setImageBitmap(takenImage)
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}