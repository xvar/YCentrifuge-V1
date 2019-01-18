package allgoritm.com.centrifuge.ui.main

import allgoritm.com.centrifuge.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogViewHolder(li: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder(li.inflate(R.layout.item_log, parent, false)) {
    private val item = itemView.findViewById<TextView>(R.id.item_log)
    fun setText(text: String) {
        item.text = text
    }

}