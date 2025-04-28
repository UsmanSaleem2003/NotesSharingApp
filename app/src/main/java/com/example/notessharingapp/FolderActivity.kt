package com.example.notessharingapp

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class FolderActivity : AppCompatActivity() {

    private lateinit var rvImages: RecyclerView
    private lateinit var btnImportFiles: Button
    private lateinit var btnImportImages: Button
    private lateinit var btnCaptureImage: Button
    private lateinit var btnShareNotes: Button

    private val images = mutableListOf<ImageItem>()
    private lateinit var imageAdapter: ImageAdapter
    private var folderName: String = ""

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder)

        rvImages = findViewById(R.id.rvImages)
        btnImportFiles = findViewById(R.id.btnImportFiles)
        btnImportImages = findViewById(R.id.btnImportImages)
        btnCaptureImage = findViewById(R.id.btnCaptureImage)
        btnShareNotes = findViewById(R.id.btnShareNotes)

        folderName = intent.getStringExtra("FOLDER_NAME") ?: "DefaultFolder"

        imageAdapter = ImageAdapter(images) { selectedImage ->
            // You can handle when user selects/deselects an image
            // For now, no need to do anything special here
        }
        rvImages.layoutManager = GridLayoutManager(this, 2)
        rvImages.adapter = imageAdapter

        btnImportFiles.setOnClickListener {
            pickImage()
        }

        btnImportImages.setOnClickListener {
            pickImage()
        }

        btnCaptureImage.setOnClickListener {
            captureImage()
        }

        btnShareNotes.setOnClickListener {
            shareImages()
        }

        loadImagesFromFolder()
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun saveImageToMediaStore(bitmap: Bitmap, title: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, title)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            val outputStream = contentResolver.openOutputStream(it)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            outputStream?.close()
        }

        return uri
    }

    private fun saveImageUriToFolder(uri: Uri) {
        val sharedPref = getSharedPreferences("folders_pref", MODE_PRIVATE)
        val editor = sharedPref.edit()
        val key = "folder_$folderName"

        val currentUris = sharedPref.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        currentUris.add(uri.toString())

        editor.putStringSet(key, currentUris)
        editor.apply()
    }

    private fun loadImagesFromFolder() {
        images.clear()

        val sharedPref = getSharedPreferences("folders_pref", MODE_PRIVATE)
        val key = "folder_$folderName"

        val uriStrings = sharedPref.getStringSet(key, emptySet())

        uriStrings?.forEach { uriString ->
            val uri = Uri.parse(uriString)
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val dateTime = getCurrentDateTime()
                val title = "Saved Image"
                images.add(ImageItem(bitmap, title, dateTime, uri))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        imageAdapter.notifyDataSetChanged()
    }

    private fun shareImages() {
        val selectedImages = imageAdapter.getSelectedImages()

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Select images to share!", Toast.LENGTH_SHORT).show()
            return
        }

        val imageUris = ArrayList<Uri>()
        for (imageItem in selectedImages) {
            imageUris.add(imageItem.uri)
        }

        if (imageUris.isNotEmpty()) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share selected notes via"))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    val uri = data?.data
                    uri?.let {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        val dateTime = getCurrentDateTime()
                        val title = "Imported Image"
                        val savedUri = saveImageToMediaStore(bitmap, title)
                        savedUri?.let { finalUri ->
                            saveImageUriToFolder(finalUri)
                            images.add(ImageItem(bitmap, title, dateTime, finalUri))
                            imageAdapter.notifyItemInserted(images.size - 1)
                        }
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        val dateTime = getCurrentDateTime()
                        val title = "Captured Image"
                        val savedUri = saveImageToMediaStore(it, title)
                        savedUri?.let { finalUri ->
                            saveImageUriToFolder(finalUri)
                            images.add(ImageItem(it, title, dateTime, finalUri))
                            imageAdapter.notifyItemInserted(images.size - 1)
                        }
                    }
                }
            }
        }
    }
}
