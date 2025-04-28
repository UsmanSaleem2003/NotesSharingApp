package com.example.notessharingapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(
    private val folders: List<String>,
    private val onFolderClick: (String) -> Unit,
    private val onFolderLongClick: (String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private val selectedFolders = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folders[position], selectedFolders.contains(folders[position]))
    }

    override fun getItemCount() = folders.size

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFolderName: TextView = itemView.findViewById(R.id.tvFolderName)

        fun bind(folderName: String, isSelected: Boolean) {
            tvFolderName.text = folderName

            if (isSelected) {
                tvFolderName.setBackgroundResource(R.drawable.folder_item_border_selected)
            } else {
                tvFolderName.setBackgroundResource(R.drawable.folder_item_border)
            }

            // Tap → Open
            itemView.setOnClickListener {
                if (selectedFolders.isEmpty()) {
                    onFolderClick(folderName) // open folder
                } else {
                    toggleSelection(folderName)
                }
            }

            // Long Press → Select
            itemView.setOnLongClickListener {
                toggleSelection(folderName)
                onFolderLongClick(folderName)
                true
            }
        }

        private fun toggleSelection(folderName: String) {
            if (selectedFolders.contains(folderName)) {
                selectedFolders.remove(folderName)
            } else {
                selectedFolders.add(folderName)
            }
            notifyItemChanged(adapterPosition)
        }
    }


    fun getSelectedFolders(): List<String> {
        return selectedFolders.toList()
    }
}
