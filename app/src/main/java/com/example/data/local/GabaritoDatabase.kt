package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Exam
import com.example.data.model.ExamSubmission
import com.example.data.model.SchoolClass

@Database(
    entities = [SchoolClass::class, Exam::class, ExamSubmission::class],
    version = 1,
    exportSchema = false
)
abstract class GabaritoDatabase : RoomDatabase() {
    abstract fun gabaritoDao(): GabaritoDao

    companion object {
        @Volatile
        private var INSTANCE: GabaritoDatabase? = null

        fun getDatabase(context: Context): GabaritoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GabaritoDatabase::class.java,
                    "gabarito_master.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
