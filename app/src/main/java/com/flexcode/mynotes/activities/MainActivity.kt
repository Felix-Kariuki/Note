package com.flexcode.mynotes.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
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
import com.flexcode.mynotes.util.Constants.UPDATE_CODE
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NoteClickInterface, NoteClickDeleteInterface {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rvNotes: RecyclerView
    private val viewModel: NoteViewModel by viewModels()
    private val allNotes = ArrayList<Note>()

    private lateinit var appUpdate: AppUpdateManager

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        //update
        appUpdate = AppUpdateManagerFactory.create(this)
        checkUpdate()

        //THEME

        rvNotes = binding.rvNotes
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.layoutManager = GridLayoutManager(this, 2)

        val noteRvAdapter = NoteAdapter(this, this, this)
        rvNotes.adapter = noteRvAdapter

        /*viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[NoteViewModel::class.java]*/
        viewModel.allNotes.observe(this) { list ->
            list?.let {
                noteRvAdapter.updateList(it)
            }
        }
        binding.fabAddNotes.setOnClickListener {
            val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java)
            startActivity(intent)
            //this.finish()
        }

        //swipe to delete
        val itemTouchHelperCallBack = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = allNotes[position + 1]
                viewModel.deleteNote(note)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Delete successful", Snackbar.LENGTH_LONG
                ).apply {
                    setAction("Undo") {
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

    override fun onResume() {
        super.onResume()
        inProgressUpdate()
    }

    private fun checkUpdate() {
        appUpdate.appUpdateInfo.addOnSuccessListener { updateInfo->
            if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                appUpdate.startUpdateFlowForResult(updateInfo, AppUpdateType.IMMEDIATE,this,
                    UPDATE_CODE)
            }
        }
    }

    private fun inProgressUpdate() {
        appUpdate.appUpdateInfo.addOnSuccessListener { updateInfo ->

            if (updateInfo.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                appUpdate.startUpdateFlowForResult(updateInfo,AppUpdateType.IMMEDIATE,this,
                    UPDATE_CODE)
            }
        }
    }

    override fun onNoteClick(note: Note) {
        val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java)
        intent.putExtra("noteType", "Edit")
        intent.putExtra("noteTitle", note.noteTitle)
        intent.putExtra("noteDescription", note.noteDescription)
        intent.putExtra("noteID", note.id)
        startActivity(intent)
        //this.finish()
    }

    override fun onDeleteIconClick(note: Note) {
        viewModel.deleteNote(note)
        Toast.makeText(
            this,
            "${note.noteTitle} Deleted", Toast.LENGTH_SHORT
        ).show()
    }

    //options menu
    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.appearance -> {

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }






}
