package com.example.deciradar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
/**
 * Adapter dla RecyclerView, który wyświetla listę danych dotyczących pomiarów dźwięku.
 * @param soundDataList Lista obiektów SoundData do wyświetlenia.
 */
class SoundDataAdapter(private val soundDataList: List<SoundData>) :
    RecyclerView.Adapter<SoundDataAdapter.SoundViewHolder>() {

    /**
     * ViewHolder przechowujący widoki pojedynczego elementu listy.
     * @param view Widok pojedynczego elementu listy.
     */
    class SoundViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.textDate)
        val hourTextView: TextView = view.findViewById(R.id.textHour)
        val soundIntensityTextView: TextView = view.findViewById(R.id.textSoundIntensity)
    }

    /**
     * Tworzy nowe instancje SoundViewHolder na podstawie layoutu item_sound_data.
     * @param parent Grupa widoków, do której zostanie dodany nowy widok.
     * @param viewType Typ widoku.
     * @return Nowa instancja SoundViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sound_data, parent, false)
        return SoundViewHolder(view)
    }

    /**
     * Przypisuje dane do widoków w SoundViewHolder dla danego elementu listy.
     * @param holder SoundViewHolder do aktualizacji.
     * @param position Pozycja elementu w liście.
     */
    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val data = soundDataList[position]
        holder.dateTextView.text = "Data: ${data.date}"
        holder.hourTextView.text = "Godzina: ${data.hour}"
        val formattedIntensity = formatSoundIntensity(data.soundIntensity)
        holder.soundIntensityTextView.text = "Natężenie dźwięku: $formattedIntensity"

        // Ustawienie koloru tła na podstawie wartości natężenia dźwięku.
        try {
            val intensityValue = formattedIntensity.toDouble()
            if (intensityValue < 85) {
                // Zielony odcień dla wartości poniżej 85 dB
                holder.view.setBackgroundColor(Color.parseColor("#A5D6A7"))
            } else {
                // Czerwony odcień dla wartości 85 dB i powyżej
                holder.view.setBackgroundColor(Color.parseColor("#EF9A9A"))
            }
        } catch (e: Exception) {
            // W razie błędu parsowania ustaw domyślne tło
            holder.view.setBackgroundColor(Color.LTGRAY)
        }
    }

    /**
     * Zwraca liczbę elementów w liście.
     * @return Liczba elementów w soundDataList.
     */
    override fun getItemCount(): Int = soundDataList.size

    /**
     * Formatuje natężenie dźwięku, zamieniając przecinki na kropki.
     * @param soundIntensity Wartość natężenia dźwięku jako String.
     * @return Sformatowana wartość natężenia dźwięku.
     */
    private fun formatSoundIntensity(soundIntensity: String): String {
        return soundIntensity.replace(",", ".")
    }
}