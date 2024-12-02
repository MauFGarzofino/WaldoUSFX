package com.example.waldo.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waldo.Models.HistoryKid
import com.example.waldo.Models.LocationData
import com.example.waldo.R
import com.example.waldo.ui.HistoryAdapter.HistoryViewHolder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DataLocationAdapter (private var historyLocation: List<LocationData>) : RecyclerView.Adapter<DataLocationAdapter.HistoryLocationViewHolder>() {
    private lateinit var map : GoogleMap
    class HistoryLocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val latitude: TextView = view.findViewById(R.id.Dl_latitude)
        val longitude: TextView = view.findViewById(R.id.Dl_longitude)
        val batteryLevel: TextView = view.findViewById(R.id.Dl_battery_level)
        val buttonLocation : ImageButton = view.findViewById(R.id.DL_image)
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
        holder.buttonLocation.setOnClickListener{
            Log.e("Working Button", "onBindViewHolder: ${holder.latitude.text}-${holder.longitude.text}", )
            val moveLatLng = LatLng(location.latitude, location.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(moveLatLng, 20f))
            map.clear()
            val marker = map.addMarker(
                MarkerOptions()
                .position(moveLatLng)
                .title("Bateria: ${location.batteryLevel}%")
                .snippet("Fecha: ${location.created_at}"))

            marker?.showInfoWindow()
        }
    }
    fun setMap(map : GoogleMap){
        this.map = map
    }

}