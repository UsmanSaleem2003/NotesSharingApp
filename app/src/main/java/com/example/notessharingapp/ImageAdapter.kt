package com.example.notessharingapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ImageAdapter(
    private val images: List<ImageItem>,
    private val onItemSelected: (ImageItem) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val selectedItems = mutableSetOf<ImageItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageItem = images[position]
        holder.bind(imageItem, selectedItems.contains(imageItem))
    }

    override fun getItemCount() = images.size

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)

        fun bind(imageItem: ImageItem, isSelected: Boolean) {
            imageView.setImageBitmap(imageItem.bitmap)
            tvTitle.text = imageItem.title
            tvDateTime.text = imageItem.dateTime

            // Highlight if selected
            itemView.setBackgroundColor(if (isSelected) Color.LTGRAY else Color.WHITE)

            itemView.setOnClickListener {
                if (selectedItems.contains(imageItem)) {
                    selectedItems.remove(imageItem)
                } else {
                    selectedItems.add(imageItem)
                }
                notifyItemChanged(adapterPosition)
                onItemSelected(imageItem) // notify FolderActivity
            }
        }
    }

    fun getSelectedImages(): List<ImageItem> {
        return selectedItems.toList()
    }
}
