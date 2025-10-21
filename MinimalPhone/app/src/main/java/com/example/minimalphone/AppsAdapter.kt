package com.example.minimalphone

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class AppsAdapter(
    private val context: Context,
    private val apps: List<AppInfo>
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun getItemCount() = apps.size


    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName)
        val openButton: Button = view.findViewById(R.id.openButton)
        val blockButton: Button = view.findViewById(R.id.blockButton)
    }
}


    // Open App and Block App buttons from the Prototype
//    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
//        val app = apps[position]
//        holder.appName.text = app.name
//
//        // Grey out if blocked
//        holder.itemView.alpha = if (app.isBlocked) 0.5f else 1.0f
//
//        // Enable/disable Open button based on block state
//        holder.openButton.isEnabled = !app.isBlocked
//        holder.openButton.alpha = if (app.isBlocked) 0.5f else 1.0f
//
//        // Open app button
//        holder.openButton.setOnClickListener {
//            if (app.isBlocked) {
//                Toast.makeText(context, "${app.name} is blocked", Toast.LENGTH_SHORT).show()
//            } else {
//                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
//                if (launchIntent != null) {
//                    context.startActivity(launchIntent)
//                } else {
//                    Toast.makeText(context, "${app.name} not installed", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        // Block/Unblock button
//        holder.blockButton.setOnClickListener {
//            app.isBlocked = !app.isBlocked
//            notifyItemChanged(position)
//        }
//    }

