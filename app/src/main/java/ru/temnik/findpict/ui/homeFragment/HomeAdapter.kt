package ru.temnik.findpict.ui.homeFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_image.view.*
import ru.temnik.findpict.R
import ru.temnik.findpict.entityDTO.ImageDTO
import javax.inject.Inject


class HomeImageAdapter @Inject constructor(): RecyclerView.Adapter<HomeImageAdapter.ViewHolder>() {

    companion object{
        private var onImgListener:OnImageClickListener? = null
    }
    var items = mutableListOf<ImageDTO>()
    var isLoading = false



    fun updateItems(newItems: List<ImageDTO>,loading: Boolean = false) {
        if(loading){
            items.addAll(newItems)
        }else{
            items = newItems.toMutableList()
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position],position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        val imageView: ImageView = itemView.imageView
        private val imageInfo: TextView = itemView.image_info_view
        lateinit var item:ImageDTO
        init{
            itemView.setOnClickListener(this)
        }
        fun bind(item:ImageDTO,position: Int){
            Glide.with(itemView.context)
                .load(item.previewURL)
                .into(imageView)
            val imageSize = "${item.imageWidth}x${item.imageHeight}"
            imageInfo.text=imageSize
            //
            imageView.tag = position
            val transitionName = imageView.context.getString(R.string.transition_name)
            imageView.transitionName ="$transitionName - $position"
            //
            this.item=item
        }

        override fun onClick(v: View?) {
           onImgListener?.onImageClick(this)
        }
    }


    fun onAttach(onImgListener:OnImageClickListener){
        HomeImageAdapter.onImgListener=onImgListener
    }

    fun onDetach(){
        onImgListener=null
    }

    interface OnImageClickListener{
        fun onImageClick(holder:ViewHolder)
    }
}