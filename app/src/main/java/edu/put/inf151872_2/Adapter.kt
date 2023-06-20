package edu.put.inf151872_2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class Adapter(
    private val ctx: Context,
    private val resources: Int,
    private val items: List<BoardGame>
) : ArrayAdapter<BoardGame>(ctx, resources, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
        val view: View = convertView ?: layoutInflater.inflate(resources, null)

        val imageView: ImageView = view.findViewById(R.id.row_image)
        val titleTextView: TextView = view.findViewById(R.id.row_title)
        val yearTextView: TextView = view.findViewById(R.id.row_text)

        val item: BoardGame = items[position]

        // Use Glide to load the image from the URL
        Glide.with(ctx)
            .load(item.imageUrl)
            .into(imageView)

        titleTextView.text = item.title
        yearTextView.text = item.releaseYear.toString()

        return view
    }
}

