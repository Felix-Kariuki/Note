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
import android.view.View
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
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity(), NoteClickInterface, NoteClickDeleteInterface {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rvNotes: RecyclerView
    lateinit var viewModel: NoteViewModel
    lateinit var noteAdapter: NoteAdapter
    private val allNotes = ArrayList<Note>()

    lateinit var executor: Executor
    lateinit var biometricPrompt: BiometricPrompt
    private lateinit var callBack: BiometricPrompt.AuthenticationCallback
    private var keyguardManager: KeyguardManager? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        checkDeviceCanAuthenticateWithBiometrics()

        //THEME
        checkTheme()




        //title, and description
//        promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("Login")
//            .setSubtitle("Login to your Notepad")
//            .setDescription("Please Authenticate Using Biometrics")
//            .setNegativeButtonText("Cancel")
//            .build()
//
        //auth btn
//        binding.btnAuthenticate.setOnClickListener {
//            authenticateWithBiometrics()
//        }

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

    override fun onStart() {
        super.onStart()
        authenticateWithBiometrics()
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
        when (MyPreferences(this).darkMode) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                delegate.applyDayNight()
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                delegate.applyDayNight()
            }
        }
    }

    private fun init() {
        //biometric
        executor = ContextCompat.getMainExecutor(this)
        callBack = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@MainActivity, getErrorMessage(errorCode), Toast.LENGTH_LONG).show()
                finish()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@MainActivity, "SUCCESS", Toast.LENGTH_LONG).show()

            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

            }
        }
        biometricPrompt = BiometricPrompt(this, executor, callBack)

    }

    private fun checkDeviceCanAuthenticateWithBiometrics() {

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
                    or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {

            BiometricManager.BIOMETRIC_SUCCESS -> {}
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {}
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {}
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {}
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {}
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {}
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {}
        }
    }

    private fun authenticateWithBiometrics() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle("Login to My Notepad")
            setDescription("Please Authenticate Using Biometrics")
            setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        }.build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N
        ){
            keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager!!.let { manager ->
                if (manager.isKeyguardSecure){
                    biometricPrompt.authenticate(promptInfo)
                }else {
                    startActivityForResult(setUpDeviceLockInAPIBelow23(), RC_DEVICE_CREDENTIAL_ENROLL)
                }
            }
        }else {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun setUpDeviceLockInAPIBelow23(): Intent {
        return Intent(Settings.ACTION_SECURITY_SETTINGS)
    }
    private fun checkAPILevelAndProceed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            startActivityForResult(setUpDeviceLockInAPIBelow23(), RC_DEVICE_CREDENTIAL_ENROLL)
        } else {
            startActivityForResult(biometricsEnrollIntent(), RC_BIOMETRICS_ENROLL)
        }
    }

    private fun biometricsEnrollIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
            }
        } else {
            TODO("VERSION.SDK_INT < R")
        }
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                getString(R.string.message_user_app_authentication)
            }
            BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                getString(R.string.error_hw_unavailable)
            }
            BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> {
                getString(R.string.error_unable_to_process)
            }
            BiometricPrompt.ERROR_TIMEOUT -> {
                getString(R.string.error_time_out)
            }
            BiometricPrompt.ERROR_NO_SPACE -> {
                getString(R.string.error_no_space)
            }
            BiometricPrompt.ERROR_CANCELED -> {
                getString(R.string.error_canceled)
            }
            BiometricPrompt.ERROR_LOCKOUT -> {
                getString(R.string.error_lockout)
            }
            BiometricPrompt.ERROR_VENDOR -> {
                getString(R.string.error_vendor)
            }
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                getString(R.string.error_lockout_permanent)
            }
            BiometricPrompt.ERROR_USER_CANCELED -> {
                getString(R.string.error_user_canceled)
            }
            BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                checkAPILevelAndProceed()
                getString(R.string.error_no_biometrics)
            }
            BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                getString(R.string.error_hw_not_present)
            }
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                startActivityForResult(biometricsEnrollIntent(), RC_BIOMETRICS_ENROLL)
                getString(R.string.error_no_device_credentials)
            }
            BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED -> {
                getString(R.string.error_security_update_required)
            }
            else -> {
                getString(R.string.error_unknown)
            }
        }
    }
    companion object {
        const val RC_DEVICE_CREDENTIAL_ENROLL = 10
        const val RC_BIOMETRICS_ENROLL = 18
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