package com.flexcode.flexnote.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flexcode.flexnote.*
import com.flexcode.flexnote.adapters.NoteAdapter
import com.flexcode.flexnote.adapters.NoteClickDeleteInterface
import com.flexcode.flexnote.adapters.NoteClickInterface
import com.flexcode.flexnote.database.Note
import com.flexcode.flexnote.databinding.ActivityMainBinding
import com.flexcode.flexnote.viewmodels.NoteViewModel

class MainActivity : AppCompatActivity(), NoteClickInterface, NoteClickDeleteInterface {

    lateinit var binding: ActivityMainBinding
    lateinit var rvNotes:RecyclerView
    lateinit var viewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        rvNotes = binding.rvNotes
        rvNotes.layoutManager = LinearLayoutManager(this)

        val noteRvAdapter = NoteAdapter(this,this,this)
        rvNotes.adapter = noteRvAdapter
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)
        viewModel.allNotes.observe(this, Observer { list ->
            list?.let {
                noteRvAdapter.updateList(it)
            }
        })
        binding.fabAddNotes.setOnClickListener {
            val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }

    override fun onNoteClick(note: Note) {
        val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java)
        intent.putExtra("noteType","Edit")
        intent.putExtra("noteTitle",note.noteTitle)
        intent.putExtra("noteDescription",note.noteDescription)
        intent.putExtra("noteID",note.id)
        startActivity(intent)
        this.finish()
    }

    override fun onDeleteIconClick(note: Note) {
        viewModel.deleteNote(note)
        Toast.makeText(this,
            "${note.noteTitle} Deleted",Toast.LENGTH_SHORT
        ).show()
    }
}