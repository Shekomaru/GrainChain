package com.grainchain.interview.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.grainchain.interview.data.Coord
import com.grainchain.interview.data.Route

@Database(entities = [Route::class, Coord::class], version = 1, exportSchema = false)
@TypeConverters(RoutesConverters::class)
abstract class RoutesDatabase : RoomDatabase() {
    abstract fun RoutesDao(): RoutesDao

    companion object {
        @Volatile
        private var INSTANCE: RoutesDatabase? = null

        fun getDatabase(context: Context): RoutesDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        RoutesDatabase::class.java,
                        "routes_database"
                    )
                    .allowMainThreadQueries()
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}