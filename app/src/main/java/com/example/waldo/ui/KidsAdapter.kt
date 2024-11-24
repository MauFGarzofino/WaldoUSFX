package com.example.waldo.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waldo.Models.KidDisplayModel
import com.example.waldo.R
import com.squareup.picasso.Picasso

class KidsAdapter(
    private var kids: MutableList<KidDisplayModel>,
    private val onItemClick: (KidDisplayModel) -> Unit // Listener para clics
) : RecyclerView.Adapter<KidsAdapter.KidViewHolder>() {

    class KidViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kidName: TextView = view.findViewById(R.id.kid_name)
        val kidStatus: TextView = view.findViewById(R.id.kid_status)
        val kidImage: ImageView = view.findViewById(R.id.kid_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KidViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.kid_item_layout, parent, false)
        return KidViewHolder(view)
    }

    override fun onBindViewHolder(holder: KidViewHolder, position: Int) {
        val kid = kids[position]
        holder.kidName.text = kid.name
        holder.kidStatus.text = kid.connectionStatus

        Picasso.get()
            .load(kid.photo)
            .placeholder(R.drawable.teddy_bear)
            .error(R.drawable.error)
            .into(holder.kidImage)

        // Configura el listener de clic
        holder.itemView.setOnClickListener {
            onItemClick(kid) // Dispara el listener con el elemento clicado
        }
    }

    override fun getItemCount(): Int = kids.size

    fun updateKid(index: Int, updatedKid: KidDisplayModel) {
        kids[index] = updatedKid
        notifyItemChanged(index)
    }

    fun updateKidsList(newKids: List<KidDisplayModel>) {
        kids.clear()
        kids.addAll(newKids)
        notifyDataSetChanged()
    }
}