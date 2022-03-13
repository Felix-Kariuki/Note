package com.flexcode.mynotes.activities

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
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
import com.flexcode.mynotes.util.Constants.RC_BIOMETRICS_ENROLL
import com.flexcode.mynotes.util.Constants.RC_DEVICE_CREDENTIAL_ENROLL
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity(), NoteClickInterface, NoteClickDeleteInterface {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rvNotes: RecyclerView
    lateinit var viewModel: NoteViewModel
    lateinit var noteAdapter: NoteAdapter
    private val allNotes = ArrayList<Note>()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        //THEME
        checkTheme()

        rvNotes = binding.rvNotes
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.layoutManager = GridLayoutManager(this, 2)

        val noteRvAdapter = NoteAdapter(this, this, this)
        rvNotes.adapter = noteRvAdapter

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[NoteViewModel::class.java]
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
                chooseThemeDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun chooseThemeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Theme")
        val styles = arrayOf("Light", "Dark")
        val checkedItem = MyPreferences(this).darkMode

        builder.setSingleChoiceItems(styles, checkedItem) { dialog, i ->

            when (i) {
                0 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    MyPreferences(this).darkMode = 0
                    dialog.dismiss()
                }
                1 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    MyPreferences(this).darkMode = 1
                    dialog.dismiss()
                }
            }

        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun checkTheme() {
        /*when (MyPreferences(this).darkMode) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                delegate.applyDayNight()
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                delegate.applyDayNight()
            }
        }*/
    }



}

class MyPreferences(context: Context?) {

    companion object {
        private const val DARK_STATUS = "DARK_MODE"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var darkMode = preferences.getInt(DARK_STATUS, 0)
        set(value) = preferences.edit().putInt(DARK_STATUS, value).apply()
}