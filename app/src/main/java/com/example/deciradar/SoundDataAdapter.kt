package com.example.deciradar

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

interface OnSoundDataInteractionListener {
    fun onItemClick(data: SoundData)
    fun onItemDelete(data: SoundData, position: Int)
}

class SoundDataAdapter(
    private val soundDataList: MutableList<SoundData>,
    private val listener: OnSoundDataInteractionListener
) : RecyclerView.Adapter<SoundDataAdapter.SoundViewHolder>() {

    class SoundViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.textDate)
        val hourTextView: TextView = view.findViewById(R.id.textHour)
        val soundIntensityTextView: TextView = view.findViewById(R.id.textSoundIntensity)
        val iconImageView: ImageView = view.findViewById(R.id.statusIcon)
        val cardView: CardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sound_data, parent, false)
        return SoundViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val data = soundDataList[position]
        holder.dateTextView.text = "Data: ${data.date}"
        holder.hourTextView.text = "Godzina: ${data.hour}"
        val formattedIntensity = formatSoundIntensity(data.soundIntensity)
        holder.soundIntensityTextView.text = "Natężenie dźwięku: $formattedIntensity dB"

        try {
            val intensityValue = formattedIntensity.toDouble()
            if (intensityValue < 85) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#C8E6C9")) // Zielone
                holder.iconImageView.setImageResource(R.drawable.good)
            } else {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFCDD2")) // Czerwone
                holder.iconImageView.setImageResource(R.drawable.bad)
            }
        } catch (e: Exception) {
            holder.cardView.setCardBackgroundColor(Color.LTGRAY)
            holder.iconImageView.setImageResource(R.drawable.bad)
        }

        holder.view.setOnClickListener {
            listener.onItemClick(data)
        }

        holder.view.setOnLongClickListener {
            AlertDialog.Builder(holder.view.context)
                .setTitle("Usuń pomiar")
                .setMessage("Czy chcesz usunąć ten pomiar?")
                .setPositiveButton("Tak") { _, _ ->
                    listener.onItemDelete(data, position)
                }
                .setNegativeButton("Nie", null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = soundDataList.size

    private fun formatSoundIntensity(soundIntensity: String): String {
        return soundIntensity.replace(",", ".")
    }
}
