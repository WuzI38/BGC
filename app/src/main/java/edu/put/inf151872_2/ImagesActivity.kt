package edu.put.inf151872_2

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class ImagesActivity : AppCompatActivity() {
    private lateinit var mListView: ListView
    private lateinit var mAdapter: ImageAdapter
    private val mUploads = ArrayList<Upload>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)

        val id = intent.getStringExtra("ID").toString()

        mListView = findViewById(R.id.list_view)

        val mDatabaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("uploads")
        mUploads.clear()

        mAdapter = ImageAdapter(this@ImagesActivity, R.layout.image_item, mUploads)
        mListView.adapter = mAdapter

        // Register ListView for context menu
        registerForContextMenu(mListView)

        mDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUploads.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val uploadData = postSnapshot.getValue(Upload::class.java)
                    if (uploadData != null) {
                        val name = uploadData.name.toString()
                        uploadData.key = postSnapshot.key
                        if (name == id) {
                            mUploads.add(uploadData)
                        }
                    }
                }
                mAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ImagesActivity, databaseError.message, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.add(Menu.NONE, R.id.menu_delete, Menu.NONE, "Delete")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val position = info.position
        val upload = mAdapter.getItem(position)

        return when (item.itemId) {
            R.id.menu_delete -> {
                if (upload != null) {
                    deleteUpload(upload)
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun deleteUpload(upload: Upload) {
        val selectedKey = upload.key
        val imageUrl = upload.imageUrl.toString()

        val storageRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("uploads")

        // Check if the file exists before attempting to delete
        storageRef.metadata.addOnSuccessListener { metadata ->
            if (metadata != null && metadata.sizeBytes > 0) {
                storageRef.delete().addOnSuccessListener {
                    if (selectedKey != null) {
                        databaseRef.child(selectedKey).removeValue().addOnSuccessListener {
                            Toast.makeText(this@ImagesActivity, "Item deleted", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this@ImagesActivity, "Failed to delete item from database", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ImagesActivity, "Invalid item key", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this@ImagesActivity, "Failed to delete item from storage", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@ImagesActivity, "File does not exist in storage", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this@ImagesActivity, "Failed to retrieve file metadata", Toast.LENGTH_SHORT).show()
        }
    }


}