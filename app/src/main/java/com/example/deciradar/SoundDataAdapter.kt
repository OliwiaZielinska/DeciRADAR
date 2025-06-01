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

/**
 * Interfejs służący do obsługi interakcji użytkownika z elementami listy pomiarów dźwięku.
 */
interface OnSoundDataInteractionListener {
    /**
     * Wywoływane, gdy użytkownik kliknie w element listy.
     * @param data Obiekt pomiaru dźwięku, który został kliknięty.
     */
    fun onItemClick(data: SoundData)

    /**
     * Wywoływane, gdy użytkownik przytrzyma element i wybierze opcję usunięcia.
     * @param data Obiekt pomiaru dźwięku do usunięcia.
     * @param position Pozycja elementu na liście.
     */
    fun onItemDelete(data: SoundData, position: Int)
}

/**
 * Adapter do wyświetlania listy obiektów [SoundData] w komponencie RecyclerView.
 * Odpowiada za tworzenie i bindowanie widoków pomiarów oraz obsługę kliknięć.
 *
 * @property soundDataList Lista pomiarów do wyświetlenia.
 * @property listener Interfejs słuchacza do obsługi kliknięć i usuwania.
 */
class SoundDataAdapter(
    private val soundDataList: MutableList<SoundData>,
    private val listener: OnSoundDataInteractionListener
) : RecyclerView.Adapter<SoundDataAdapter.SoundViewHolder>() {

    /**
     * ViewHolder przechowujący widoki reprezentujące pojedynczy pomiar dźwięku.
     * @param view Widok elementu listy.
     */
    class SoundViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.textDate)
        val hourTextView: TextView = view.findViewById(R.id.textHour)
        val soundIntensityTextView: TextView = view.findViewById(R.id.textSoundIntensity)
        val iconImageView: ImageView = view.findViewById(R.id.statusIcon)
        val cardView: CardView = view.findViewById(R.id.cardView)
    }

    /**
     * Tworzy nowy widok elementu listy (ViewHolder).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sound_data, parent, false)
        return SoundViewHolder(view)
    }

    /**
     * Łączy dane pomiaru z widokiem.
     * Koloruje kartę oraz ustawia odpowiednią ikonę w zależności od poziomu dźwięku.
     */
    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val data = soundDataList[position]
        holder.dateTextView.text = "Data: ${data.date}"
        holder.hourTextView.text = "Godzina: ${data.hour}"
        val formattedIntensity = formatSoundIntensity(data.soundIntensity)
        holder.soundIntensityTextView.text = "Natężenie dźwięku: $formattedIntensity dB"

        try {
            val intensityValue = formattedIntensity.toDouble()
            if (intensityValue < 85) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#C8E6C9")) // Zielony
                holder.iconImageView.setImageResource(R.drawable.good)
            } else {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFCDD2")) // Czerwony
                holder.iconImageView.setImageResource(R.drawable.bad)
            }
        } catch (e: Exception) {
            holder.cardView.setCardBackgroundColor(Color.LTGRAY)
            holder.iconImageView.setImageResource(R.drawable.bad)
        }

        // Kliknięcie elementu
        holder.view.setOnClickListener {
            listener.onItemClick(data)
        }

        // Długie kliknięcie – zapytanie o usunięcie
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

    /**
     * Zwraca liczbę elementów w liście.
     */
    override fun getItemCount(): Int = soundDataList.size

    /**
     * Formatuje natężenie dźwięku zamieniając przecinki na kropki.
     * Przydatne przy parsowaniu tekstu na liczby zmiennoprzecinkowe.
     *
     * @param soundIntensity Tekst z natężeniem dźwięku.
     * @return Sformatowany tekst.
     */
    private fun formatSoundIntensity(soundIntensity: String): String {
        return soundIntensity.replace(",", ".")
    }
}
