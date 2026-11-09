package com.satse.jarj2me

import android.app.Application
import androidx.room.Room
import com.satse.jarj2me.data.GameDatabase

class J2MEApplication : Application() {

    companion object {
        lateinit var instance: J2MEApplication
        lateinit var database: GameDatabase
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(
            this,
            GameDatabase::class.java,
            "j2me_games.db"
        ).fallbackToDestructiveMigration().build()
    }
}