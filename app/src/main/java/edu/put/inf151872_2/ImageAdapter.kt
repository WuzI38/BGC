package edu.put.inf151872_2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide

class ImageAdapter(
    private val ctx: Context,
    private val resources: Int,
    private val items: List<Upload>
) : ArrayAdapter<Upload>(ctx, resources, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
        val view: View = convertView ?: layoutInflater.inflate(resources, null)

        val imageView: ImageView = view.findViewById(R.id.image_view_upload)

        val item: Upload = items[position]

        // Use Glide to load the image from the URL
        Glide.with(ctx)
            .load(item.imageUrl)
            .into(imageView)

        return view
    }
}

