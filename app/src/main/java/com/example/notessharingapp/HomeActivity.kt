package com.example.notessharingapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import android.net.Uri
import androidx.core.content.FileProvider

class HomeActivity : AppCompatActivity() {

    private lateinit var rvFolders: RecyclerView
    private lateinit var fabAddFolder: FloatingActionButton
    private lateinit var btnShareFolders: Button
    private val folders = mutableListOf<String>()
    private lateinit var folderAdapter: FolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        rvFolders = findViewById(R.id.rvFolders)
        fabAddFolder = findViewById(R.id.fabAddFolder)
        btnShareFolders = findViewById(R.id.btnShareFolders)

        folderAdapter = FolderAdapter(
            folders,
            onFolderClick = { folderName ->
                // Open the folder normally on tap
                val intent = Intent(this, FolderActivity::class.java)
                intent.putExtra("FOLDER_NAME", folderName)
                startActivity(intent)
            },
            onFolderLongClick = { folderName ->
                // Just toggle selection highlight, no need to open
            }
        )

        rvFolders.layoutManager = LinearLayoutManager(this)
        rvFolders.adapter = folderAdapter

        fabAddFolder.setOnClickListener {
            showCreateFolderDialog()
        }

        btnShareFolders.setOnClickListener {
            shareSelectedFolders()
        }

        loadFolders()
    }

    private fun showCreateFolderDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create New Folder")

        val input = EditText(this)
        input.hint = "Enter folder name"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Create") { dialog, _ ->
            val folderName = input.text.toString().trim()
            if (folderName.isNotEmpty()) {
                folders.add(folderName)
                folderAdapter.notifyItemInserted(folders.size - 1)
                saveFolders()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun saveFolders() {
        val sharedPref = getSharedPreferences("folders_pref", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putStringSet("folders", folders.toSet())
        editor.apply()
    }

    private fun loadFolders() {
        val sharedPref = getSharedPreferences("folders_pref", MODE_PRIVATE)
        val savedFolders = sharedPref.getStringSet("folders", emptySet())
        folders.clear()
        folders.addAll(savedFolders ?: emptySet())
        folderAdapter.notifyDataSetChanged()
    }

    private fun shareSelectedFolders() {
        val selectedFolders = folderAdapter.getSelectedFolders()
        if (selectedFolders.isEmpty()) {
            Toast.makeText(this, "Select folders to share!", Toast.LENGTH_SHORT).show()
            return
        }

        val zipFiles = mutableListOf<File>()
        for (folder in selectedFolders) {
            val zipFile = createZipForFolder(folder)
            zipFile?.let { zipFiles.add(it) }
        }

        if (zipFiles.isNotEmpty()) {
            val uris = ArrayList<Uri>()
            for (file in zipFiles) {
                val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                uris.add(uri)
            }

            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "application/zip"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Folders"))
        }
    }

    private fun createZipForFolder(folderName: String): File? {
        val sharedPref = getSharedPreferences("folders_pref", MODE_PRIVATE)
        val uriStrings = sharedPref.getStringSet("folder_$folderName", emptySet())

        if (uriStrings.isNullOrEmpty()) return null

        val zipFile = File(cacheDir, "$folderName.zip")
        val zos = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

        try {
            for (uriString in uriStrings) {
                val uri = Uri.parse(uriString)
                val inputStream = contentResolver.openInputStream(uri)
                val entryName = uri.lastPathSegment ?: "image.jpg"

                zos.putNextEntry(ZipEntry(entryName))

                inputStream?.copyTo(zos, bufferSize = 1024)

                zos.closeEntry()
                inputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            zos.close()
        }
        return zipFile
    }
}
