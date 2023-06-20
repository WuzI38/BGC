package edu.put.inf151872_2

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask


class UploaderActivity : AppCompatActivity() {

    private lateinit var mButtonChooseImage: Button
    private lateinit var mButtonUpload: Button
    private lateinit var mTextViewShowUploads: TextView
    private lateinit var mImageView: ImageView
    private lateinit var mProgressBar: ProgressBar

    private lateinit var id : String

    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var mImageUri: Uri

    private var mStorageRef: StorageReference? = null
    private var mDatabaseRef: DatabaseReference? = null

    private var mUploadTask: StorageTask<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uploader)

        FirebaseApp.initializeApp(this)

        id = intent.getStringExtra("ID").toString()

        mButtonChooseImage = findViewById(R.id.button_choose_image)
        mButtonUpload = findViewById(R.id.button_upload)
        mTextViewShowUploads = findViewById(R.id.text_view_show_uploads)
        mImageView = findViewById(R.id.image_view)
        mProgressBar = findViewById(R.id.progress_bar)

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads")

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                mImageUri = it
                Glide.with(this)
                    .load(mImageUri)
                    .into(mImageView)
            }
        }

        mButtonChooseImage.setOnClickListener {
            openFileChooser()
        }

        mButtonUpload.setOnClickListener {
            if (mUploadTask != null && mUploadTask!!.isInProgress) {
                Toast.makeText(this@UploaderActivity, "Upload in progress", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if(::mImageUri.isInitialized) {
                    uploadFile()
                }
            }
        }


        mTextViewShowUploads.setOnClickListener {
            val i = Intent(this, ImagesActivity::class.java)
            i.putExtra("ID", id)
            startActivity(i)
        }
    }

    private fun openFileChooser() {
        getContent.launch("image/*")
    }

    private fun getFileExtension(uri: Uri): String? {
        val cR: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun uploadFile() {
        val filename = "${id}_${System.currentTimeMillis()}"
        val filenameFull = "${filename}.${getFileExtension(mImageUri)}"
        val fileReference = mStorageRef!!.child(filenameFull)
        mUploadTask = fileReference.putFile(mImageUri)
            .addOnSuccessListener {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    mProgressBar.progress = 0
                }, 500)
                Toast.makeText(this@UploaderActivity, "Upload successful", Toast.LENGTH_LONG).show()

                // Get the download URL
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()

                    val upload = Upload(id, imageUrl)
                    val uploadId = mDatabaseRef!!.push().key
                    mDatabaseRef!!.child(uploadId!!).setValue(upload)
                }.addOnFailureListener {
                    // Handle failure to retrieve the download URL
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@UploaderActivity,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnProgressListener { taskSnapshot ->
                val progress =
                    100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                mProgressBar.progress = progress.toInt()
            }
    }

}
