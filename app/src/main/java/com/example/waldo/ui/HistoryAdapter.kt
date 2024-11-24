package com.example.waldo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waldo.Models.HistoryKid
import com.example.waldo.Models.KidDisplayModel
import com.example.waldo.R
import com.example.waldo.ui.KidsAdapter.KidViewHolder
import com.squareup.picasso.Picasso
import java.util.Date

class HistoryAdapter(private var history: List<HistoryKid>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kidName: TextView = view.findViewById(R.id.kid_name_history)
        val kidStatus: TextView = view.findViewById(R.id.kid_history_status)
        val kidCreated: TextView = view.findViewById(R.id.kid_date_history)
        val kidImage: ImageView = view.findViewById(R.id.kid_history_image)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return history.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val kid = history[position]
        holder.kidName.text = kid.familyName
        holder.kidStatus.text = if (kid.isActive == "1") "Vinculacion activa" else "Vinculacion no activa"
        holder.kidCreated.text = kid.created_at.toString()
        Picasso.get()
            .load(kid.photo)
            .placeholder(R.drawable.teddy_bear)
            .error(R.drawable.error)
            .into(holder.kidImage)
    }
}
