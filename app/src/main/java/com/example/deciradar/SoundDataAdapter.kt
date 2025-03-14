package com.example.deciradar

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
    class SoundViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        holder.soundIntensityTextView.text = "Natężenie dźwięku: ${formatSoundIntensity(data.soundIntensity)}"
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