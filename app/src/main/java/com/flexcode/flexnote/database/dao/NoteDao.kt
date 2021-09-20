package com.flexcode.flexnote.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.flexcode.flexnote.database.Note

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)//ignore conflicting ids
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notesTable ORDER BY id ASC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notesTable ORDER BY timestamp ASC")
    fun getAllNotesTime(): LiveData<List<Note>>
}