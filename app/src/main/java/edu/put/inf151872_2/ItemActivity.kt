package edu.put.inf151872_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class ItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        val id = intent.getStringExtra("ID").toString()

        val image = intent.getStringExtra("Image")
        val players = intent.getStringExtra("Players")
        val playtime = intent.getStringExtra("Playtime")
        val rating = intent.getStringExtra("Rating")
        val rank = intent.getStringExtra("Rank")
        val description = intent.getStringExtra("Description")

        val imageView: ImageView = findViewById(R.id.game_img)
        val textView = findViewById<TextView>(R.id.text)

        if (image != null) {
            Glide.with(this)
                .load(image)
                .into(imageView)
        }

        val playersText = "Players: $players"
        val playtimeText = "Playtime: $playtime"
        val ratingText = "Rating: $rating"
        val rankText = "Rank: $rank"
        val descriptionText = "Description: $description"

        val fullText = "$playersText\n$playtimeText\n$ratingText\n$rankText\n\n$descriptionText"
        textView.text = fullText

        imageView.setOnClickListener {
            val i = Intent(this, UploaderActivity::class.java)
            i.putExtra("ID", id)
            startActivity(i)
        }
    }

}