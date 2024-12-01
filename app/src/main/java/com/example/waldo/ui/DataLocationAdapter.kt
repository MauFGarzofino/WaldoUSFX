package com.example.waldo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waldo.Models.HistoryKid
import com.example.waldo.Models.LocationData
import com.example.waldo.R
import com.example.waldo.ui.HistoryAdapter.HistoryViewHolder

class DataLocationAdapter (private var historyLocation: List<LocationData>) : RecyclerView.Adapter<DataLocationAdapter.HistoryLocationViewHolder>() {

    class HistoryLocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val latitude: TextView = view.findViewById(R.id.Dl_latitude)
        val longitude: TextView = view.findViewById(R.id.Dl_longitude)
        val batteryLevel: TextView = view.findViewById(R.id.Dl_battery_level)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryLocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.data_location_item, parent, false)
        return HistoryLocationViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyLocation.size
    }

    override fun onBindViewHolder(holder: HistoryLocationViewHolder, position: Int) {
        val location = historyLocation[position]
        holder.latitude.text = location.latitude.toString()
        holder.longitude.text = location.longitude.toString()
        holder.batteryLevel.text = location.batteryLevel.toString() + "%"
    }
}