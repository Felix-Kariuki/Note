package com.flexcode.mynotes.database.repository

import androidx.lifecycle.LiveData
import com.flexcode.mynotes.database.Note
import com.flexcode.mynotes.database.dao.NoteDao
import javax.inject.Inject

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
    ) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    //
    val allTimeStamp: LiveData<List<Note>> = noteDao.getAllNotesTime()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.


    suspend fun insert(note: Note){
        noteDao.insert(note)
    }

    suspend fun delete(note: Note){
        noteDao.delete(note)
    }
    suspend fun update(note: Note){
        noteDao.update(note)
    }
}