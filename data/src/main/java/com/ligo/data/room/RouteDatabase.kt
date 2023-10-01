package com.ligo.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RouteEntity::class], version = 1)
internal abstract class RouteDatabase : RoomDatabase() {

    companion object {
        const val NAME = "db.routesd"
    }

    abstract fun routeDao(): RouteDao
}