package edu.put.inf151872_2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        val textField: EditText = findViewById(R.id.account_name_setup)
        val buttonDownload: Button = findViewById(R.id.button_setup)

        val errorMsg = intent.getStringExtra("Error")
        if(errorMsg != null) {
            textField.hint = errorMsg
        }

        buttonDownload.setOnClickListener {
            val username = textField.text.toString()
            if (username.isNotEmpty()) {
                val i = Intent(this, LoaderActivity::class.java)
                i.putExtra("Username", username)
                startActivity(i)
            }
        }
    }
}