package com.example.minimalphone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BlockedAppsAdapter(
    private val apps: List<AppInfo>,
    private val onChange: (List<AppInfo>) -> Unit
) : RecyclerView.Adapter<BlockedAppsAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.appName)
        val toggle: SwitchCompat = view.findViewById(R.id.blockSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.name.text = app.name
        holder.toggle.setOnCheckedChangeListener(null)
        holder.toggle.isChecked = app.isBlocked

        holder.toggle.setOnCheckedChangeListener { _, checked ->
            app.isBlocked = checked
            onChange(apps)
        }
    }

    override fun getItemCount() = apps.size
}
