package allgoritm.com.centrifuge.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(
    private val context: Context,
    val logs: MutableList<String>
) : RecyclerView.Adapter<LogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val li = LayoutInflater.from(context)
        return LogViewHolder(li, parent)
    }

    override fun getItemCount(): Int = logs.size

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.setText(logs[position])
    }
}

