package com.flexcode.mynotes.activities


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.flexcode.mynotes.database.Note
import com.flexcode.mynotes.viewmodels.NoteViewModel
import com.flexcode.mynotes.databinding.ActivityAddEditNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class AddEditNoteActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddEditNoteBinding
    lateinit var viewModel: NoteViewModel
    var noteID = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        val etEditNoteTitle = binding.etEditNoteTitle
        val etEditNoteDescription = binding.etEditNoteDescription
        val tvNoteTitle = binding.tvNoteTitle
        val tvNoteDescription = binding.tvNoteDescription
        val btnAddUpdate = binding.btnAddUpdate
        val fabUpdate = binding.fabEditNote

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)



        val noteType = intent.getStringExtra("noteType")
        if (noteType.equals("Edit")){
            val noteTitle = intent.getStringExtra("noteTitle")
            val noteDesc = intent.getStringExtra("noteDescription")
            noteID = intent.getIntExtra("noteID",-1)
            etEditNoteTitle.setText(noteTitle)
            etEditNoteDescription.setText(noteDesc)
            tvNoteDescription.text = noteDesc
            tvNoteTitle.text = noteTitle
            binding.tvNoteTitle.visibility = View.VISIBLE
            binding.tvNoteDescription.visibility = View.VISIBLE
            etEditNoteDescription.visibility = View.GONE
            etEditNoteTitle.visibility = View.GONE
            btnAddUpdate.visibility = View.GONE
        }else {
            btnAddUpdate.text ="Save Note"
            fabUpdate.visibility = View.GONE
        }
        fabUpdate.setOnClickListener {
            btnAddUpdate.text = "Update Note"
            btnAddUpdate.visibility = View.VISIBLE
            tvNoteTitle.visibility=View.GONE
            tvNoteDescription.visibility = View.GONE
            etEditNoteDescription.visibility = View.VISIBLE
            etEditNoteTitle.visibility = View.VISIBLE
            fabUpdate.visibility = View.GONE
        }

        btnAddUpdate.setOnClickListener {
            val noteTitle = etEditNoteTitle.text.toString()
            val noteDescription = etEditNoteDescription.text.toString()

            if (noteType.equals("Edit")){
                if (noteTitle.isNotEmpty() && noteDescription.isNotEmpty()){
                    val sdf = SimpleDateFormat("dd MMM , yyyy - HH:mm")
                    val currentDate:String = sdf.format(Date())
                    val updateNote = Note(noteTitle,noteDescription,currentDate)
                    updateNote.id = noteID
                    viewModel.updateNote(updateNote)
                    Toast.makeText(this,
                    "Note Updated Successfully",
                    Toast.LENGTH_SHORT).show()
                }
            }else {
                if (noteTitle.isNotEmpty() && noteDescription.isNotEmpty()) {
                    val sdf = SimpleDateFormat("dd MMM , yyyy - HH:mm")
                    val currentDate:String = sdf.format(Date())
                    viewModel.addNote(Note(noteTitle,noteDescription,currentDate))
                    Toast.makeText(this,
                        "Note Saved Successfully",
                        Toast.LENGTH_SHORT).show()
                }
            }
            onBackPressed()
            //startActivity(Intent(applicationContext, MainActivity::class.java))
            this.finish()
        }
    }
}