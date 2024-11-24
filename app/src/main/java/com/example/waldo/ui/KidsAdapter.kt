package com.example.waldo.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waldo.HistoryActivity
import com.example.waldo.HistoryDataLocationActivity
import com.example.waldo.MainActivity
import com.example.waldo.Models.KidDisplayModel
import com.example.waldo.R
import com.example.waldo.Repository.EnrollmentRepository
import com.squareup.picasso.Picasso

class KidsAdapter(
    private var enrollmentRepository: EnrollmentRepository,
    private var kids: MutableList<KidDisplayModel>,
    private val onItemClick: (KidDisplayModel) -> Unit // Listener para clics
) : RecyclerView.Adapter<KidsAdapter.KidViewHolder>() {

    class KidViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kidName: TextView = view.findViewById(R.id.kid_name)
        val kidStatus: TextView = view.findViewById(R.id.kid_status)
        val kidImage: ImageView = view.findViewById(R.id.kid_image)
        val kidUnlinkButton : Button = view.findViewById(R.id.unlink_button)
        val kidHistoryLocation : Button = view.findViewById(R.id.ButtonHistoryKid)
    }
    private lateinit var context: Context;

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
        holder.kidUnlinkButton.setOnClickListener {
            Log.e("Button", "Unlink User ${kid.name}", )

            val dialog = AlertDialog.Builder(enrollmentRepository.getContext())
                .setTitle("Advertencia")
                .setMessage("Esta seguro de desvincularse de ${kid.name}??")
                .setPositiveButton("Aceptar") { dialog, _ ->
                    enrollmentRepository.unLinkEnrollment(kid.id_Enrollment)
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    //cancelar la solicutd de desvinculacion
                }
                .create()
            dialog.show()
        }
        holder.kidHistoryLocation.setOnClickListener {
            val intent = Intent(context, HistoryDataLocationActivity::class.java)
                .putExtra("id_Kid", kid.id_User)
            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = kids.size

    fun setContext(context: Context){
        this.context = context
    }

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