package com.flexcode.mynotes.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flexcode.mynotes.R
import com.flexcode.mynotes.database.Note

class NoteAdapter(
    private val context: Context,
    private val noteClickDeleteInterface: NoteClickDeleteInterface,
    private val noteClickInterface: NoteClickInterface
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val allNotes = ArrayList<Note>()

    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvNoteTitle: TextView = itemView.findViewById(R.id.tvNoteTitle)
        val tvTimeStamp: TextView = itemView.findViewById(R.id.tvNoteTimeStamp)
        val tvNoteDescription: TextView = itemView.findViewById(R.id.tvNoteDescription)
        val ivOptions: ImageView = itemView.findViewById(R.id.ivOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item,parent,false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.tvNoteTitle.text = allNotes[position].noteTitle
        holder.tvNoteDescription.text = allNotes[position].noteDescription
        holder.tvTimeStamp.text = "Updated : " + allNotes[position].timestamp

        holder.ivOptions.setOnClickListener {
            val options = arrayOf(
                "Delete"
            )
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
            builder.setItems(options) { dialog, i ->
                if (i == 0){
                    noteClickDeleteInterface.onDeleteIconClick(allNotes[position])
                }


            }
            builder.show()
        }

        holder.itemView.setOnClickListener {
            noteClickInterface.onNoteClick(allNotes[position])
        }
    }

    override fun getItemCount(): Int {
        return allNotes.size
    }

    fun updateList(newList: List<Note>){
        allNotes.clear()
        allNotes.addAll(newList)
        notifyDataSetChanged()
    }
}

interface NoteClickDeleteInterface {
    fun onDeleteIconClick(note: Note)
}

interface NoteClickInterface {
    fun onNoteClick(note: Note)
}