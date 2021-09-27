package com.flexcode.flexnote.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flexcode.flexnote.*
import com.flexcode.flexnote.adapters.NoteAdapter
import com.flexcode.flexnote.adapters.NoteClickDeleteInterface
import com.flexcode.flexnote.adapters.NoteClickInterface
import com.flexcode.flexnote.database.Note
import com.flexcode.flexnote.databinding.ActivityMainBinding
import com.flexcode.flexnote.viewmodels.NoteViewModel
import com.google.android.material.snackbar.Snackbar
import android.view.ViewGroup

import android.R.string.no
import android.widget.Button
import java.lang.RuntimeException


class MainActivity : AppCompatActivity(), NoteClickInterface, NoteClickDeleteInterface {

    lateinit var binding: ActivityMainBinding
    lateinit var rvNotes:RecyclerView
    lateinit var viewModel: NoteViewModel
    lateinit var noteAdapter: NoteAdapter
    private val allNotes = ArrayList<Note>()


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

        //swipe to delete
        val itemTouchHelperCallBack = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note  = allNotes[position]
                viewModel.deleteNote(note)
                Snackbar.make(findViewById(android.R.id.content),
                    "Delete successful",Snackbar.LENGTH_LONG).apply {
                    setAction("Undo"){
                        viewModel.addNote(note)
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallBack).apply {
            attachToRecyclerView(rvNotes)
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