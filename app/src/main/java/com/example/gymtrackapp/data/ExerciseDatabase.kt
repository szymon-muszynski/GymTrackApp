package com.example.gymtrackapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gymtrackapp.data.converters.Converters
import com.example.gymtrackapp.data.dao.ExerciseDao
import com.example.gymtrackapp.data.dao.TemplateDao
import com.example.gymtrackapp.data.dao.TrainingDao
import com.example.gymtrackapp.data.entity.Exercise
import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.TrainingSession
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TemplateExercise
import com.example.gymtrackapp.data.entity.WorkoutTemplate

@Database(
    entities = [
        Exercise::class,
        TrainingSession::class,
        SessionSetDetails::class,
        SessionExercise::class,
        WorkoutTemplate::class,
        TemplateExercise::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun trainingDao(): TrainingDao
    abstract fun templateDao(): TemplateDao

    companion object {
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        fun getDatabase(context: Context): ExerciseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    "exercise_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
