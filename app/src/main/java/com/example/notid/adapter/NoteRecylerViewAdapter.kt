package com.example.notid.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.notid.R
import com.example.notid.Util
import com.example.notid.activity.EditActivity
import com.example.notid.model.Note
import kotlinx.android.synthetic.main.list_layout.view.*
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class NoteRecylerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items:List<Note> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is NoteViewHolder->{
                holder.bind(items.get(position))
            }
        }
    }

    fun submitList(items:List<Note>){
        this.items = items
    }

    class NoteViewHolder(itemCardView:View):RecyclerView.ViewHolder(itemCardView){
        val cardTitle = itemCardView.card_title
        val cardDate = itemCardView.card_date
        val cardPreview = itemCardView.card_preview

        fun bind(note: Note){
            cardTitle.text = note.name
            cardDate.text = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(note.date)
            cardPreview.text = note.noteCode
            itemView.setOnClickListener {
                Toast.makeText(itemView.context,"ID = "+note.noteCode, Toast.LENGTH_SHORT).show()
                val intent = Intent(itemView.context, EditActivity::class.java).apply {
                    putExtra("com.example.notid.NOTECODE", note.noteCode)
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}