package com.example.appblocker.ui.blocked

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appblocker.data.InstalledApp
import com.example.appblocker.databinding.ItemInstalledAppBinding

class InstalledAppAdapter(
    private val onToggle: (InstalledApp, Boolean) -> Unit
) : ListAdapter<InstalledApp, InstalledAppAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<InstalledApp>() {
            override fun areItemsTheSame(oldItem: InstalledApp, newItem: InstalledApp) =
                oldItem.packageName == newItem.packageName
            override fun areContentsTheSame(oldItem: InstalledApp, newItem: InstalledApp) =
                oldItem == newItem
        }
    }

    inner class VH(val b: ItemInstalledAppBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemInstalledAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = getItem(position)
        holder.b.appIcon.setImageDrawable(app.icon)
        holder.b.appName.text = app.label
        holder.b.appPackage.text = app.packageName
        holder.b.blockSwitch.setOnCheckedChangeListener(null)
        holder.b.blockSwitch.isChecked = app.isBlocked
        holder.b.blockSwitch.setOnCheckedChangeListener { _, checked ->
            // Optimistic UI: reflect immediately
            if (app.isBlocked != checked) {
                app.isBlocked = checked
                onToggle(app, checked)
            }
        }
    }

    fun find(pkg: String): InstalledApp? = currentList.firstOrNull { it.packageName == pkg }
}