package com.flexcode.mynotes.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flexcode.mynotes.*
import com.flexcode.mynotes.adapters.NoteAdapter
import com.flexcode.mynotes.adapters.NoteClickDeleteInterface
import com.flexcode.mynotes.adapters.NoteClickInterface
import com.flexcode.mynotes.database.Note
import com.flexcode.mynotes.databinding.ActivityMainBinding
import com.flexcode.mynotes.viewmodels.NoteViewModel
import com.google.android.material.snackbar.Snackbar

import androidx.recyclerview.widget.GridLayoutManager


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

        rvNotes = binding.rvNotes
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.layoutManager = GridLayoutManager(this,2)

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
            //this.finish()
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
                val note  = allNotes[position+1]
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
        //this.finish()
    }

    override fun onDeleteIconClick(note: Note) {
        viewModel.deleteNote(note)
        Toast.makeText(this,
            "${note.noteTitle} Deleted",Toast.LENGTH_SHORT
        ).show()
    }
}