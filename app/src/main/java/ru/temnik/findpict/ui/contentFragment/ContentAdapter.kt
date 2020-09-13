package ru.temnik.findpict.ui.contentFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_image.view.*
import ru.temnik.findpict.R
import ru.temnik.findpict.entityDTO.ImageDTO
import javax.inject.Inject


class ContentAdapter @Inject constructor(): RecyclerView.Adapter<ContentAdapter.ViewHolder>() {
    private var items = mutableListOf<ImageDTO>()
    var isLoading = false

    fun updateItems(items: List<ImageDTO>) {
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.imageView
        private val imageInfo: TextView = itemView.image_info_view

        fun bind(item:ImageDTO){
            Glide.with(itemView.context)
                .load(item.previewURL)
                .into(image)
            val imageSize = "${item.imageWidth}x${item.imageHeight}"
            imageInfo.text=imageSize
        }
    }
}