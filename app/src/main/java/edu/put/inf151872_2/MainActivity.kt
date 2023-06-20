package edu.put.inf151872_2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
/*import java.time.LocalDate
import java.time.format.DateTimeFormatter*/

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val username = sharedPref.getString("Username", null)

        val syncDate = (if (intent.getStringExtra("Error") == null) sharedPref.getString("SyncDate", null) else "None")

        var i = Intent(this, LogActivity::class.java)

        if (username == null || syncDate == null) {
            startActivity(i)
        }

        val expansionCount = DBHandler.getInstance(this)?.countElements(isExpansion = true)
        val gamesCount = DBHandler.getInstance(this)?.countElements(isExpansion = false)

        val textUser =  findViewById<TextView>(R.id.text_user)
        val textDate =  findViewById<TextView>(R.id.text_date)
        val textGames =  findViewById<TextView>(R.id.text_games)
        val textExpansions =  findViewById<TextView>(R.id.text_expansions)

        textUser.text = username
        textDate.text = syncDate
        "Games: ${gamesCount.toString()}".also { textGames.text = it }
        "Expansions: ${expansionCount.toString()}".also { textExpansions.text = it }

        val userTile = findViewById<CardView>(R.id.user_tile)
        userTile.setOnClickListener {
            startActivity(i)
        }

        val timeTile = findViewById<CardView>(R.id.time_tile)
        timeTile.setOnClickListener {
            /*val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            if(currentDate.format(formatter) != syncDate) {
                i = Intent(this, LoaderActivity::class.java)
                startActivity(i)
            }*/
            i = Intent(this, LoaderActivity::class.java)
            startActivity(i)
        }

        val diceTile = findViewById<CardView>(R.id.dice_tile)
        diceTile.setOnClickListener {
            i = Intent(this, GamesActivity::class.java)
            i.putExtra("Type", false)
            startActivity(i)
        }

        val plusTile = findViewById<CardView>(R.id.plus_tile)
        plusTile.setOnClickListener {
            i = Intent(this, GamesActivity::class.java)
            i.putExtra("Type", true)
            startActivity(i)
        }
    }
}