package edu.put.inf151872_2

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GamesActivity : AppCompatActivity() {
    private var list: MutableList<BoardGame>? = null
    private var sortByTitle = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)

        val type = intent.getBooleanExtra("Type", false)
        val listView = findViewById<ListView>(R.id.item_list)
        list = DBHandler.getInstance(this)?.getElements(isExpansion = type) as MutableList<BoardGame>?

        listView.adapter = list?.let { Adapter(this, R.layout.list_row, it) }

        val itemSortTextView = findViewById<TextView>(R.id.item_sort)
        updateSortTextViewText()

        itemSortTextView.setOnClickListener {
            sortByTitle = !sortByTitle
            sortList()
            updateSortTextViewText()
            (listView.adapter as Adapter?)?.notifyDataSetChanged()
        }

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val item = list?.get(position)
                val intent = Intent(this, ItemActivity::class.java)
                if (item != null) {
                    val playersStr = if (item.minPlayers != item.maxPlayers) "${item.minPlayers}-${item.maxPlayers}" else item.minPlayers.toString()
                    intent.putExtra("Image", item.imageUrl)
                    intent.putExtra("Name", item.title)
                    intent.putExtra("ID", item.bggId.toString())
                    intent.putExtra("Players", playersStr)
                    intent.putExtra("Playtime", item.playTime.toString())
                    intent.putExtra("Rating", String.format("%.3f", item.rating))
                    intent.putExtra("Rank", item.bggRank.toString())
                    intent.putExtra("Description", item.description)
                }
                startActivity(intent)
            }
    }

    private fun sortList() {
        list?.let {
            if (sortByTitle) {
                it.sortBy { game -> game.title }
            } else {
                it.sortBy { game -> game.releaseYear }
            }
        }
    }

    private fun updateSortTextViewText() {
        val itemSortTextView = findViewById<TextView>(R.id.item_sort)
        itemSortTextView.text = if (sortByTitle) {
            "Sort: By title"
        } else {
            "Sort: By year"
        }
    }
}
