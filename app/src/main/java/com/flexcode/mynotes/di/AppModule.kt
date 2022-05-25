package com.flexcode.mynotes.di

import android.app.Application
import androidx.room.Room
import com.flexcode.mynotes.database.NoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesJobsDatabase(app: Application): NoteDatabase {
        return Room.databaseBuilder(
            app,
            NoteDatabase::class.java,
            "notesTable"
        ).build()
    }

    @Provides
    @Singleton
    fun providesNotesDao(db:NoteDatabase)  = db.getNotesDao()
}