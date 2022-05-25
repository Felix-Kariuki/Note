package com.flexcode.mynotes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flexcode.mynotes.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewNotesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_notes)
    }
}