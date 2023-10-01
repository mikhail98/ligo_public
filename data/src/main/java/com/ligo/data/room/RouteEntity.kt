package com.ligo.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
internal class RouteEntity(
    @ColumnInfo(name = "startLat")
    val startLat: Double,

    @ColumnInfo(name = "startLng")
    val startLng: Double,

    @ColumnInfo(name = "endLat")
    val endLat: Double,

    @ColumnInfo(name = "endLng")
    val endLng: Double,

    @ColumnInfo(name = "duration")
    val duration: Long,

    @ColumnInfo(name = "distance")
    val distance: Float,

    @PrimaryKey
    @ColumnInfo(name = "points")
    val points: String,
)