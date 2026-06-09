package com.viv3k.filehive.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FolderCacheEntity::class, LockedFolderEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
    abstract fun folderCacheDao(): FolderCacheDao
    abstract fun lockedFolderDao(): LockedFolderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "filehive_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}