package com.ligo.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.rxjava3.core.Single

@Dao
internal interface RouteDao {

    @Query("SELECT * FROM routes")
    fun getRouteList(): List<RouteEntity>

    @Query("SELECT * FROM routes WHERE startLat = :startLat AND startLng = :startLng AND endLat = :endLat AND endLng = :endLng")
    fun getRouteByLocation(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
    ): RouteEntity

    @Query("SELECT * FROM routes WHERE startLat = :startLat AND startLng = :startLng AND endLat = :endLat AND endLng = :endLng")
    fun getRouteByLocationObservable(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
    ): Single<RouteEntity>

    @Insert
    fun insertRoute(entity: RouteEntity)

    @Update
    fun updateRoute(entity: RouteEntity)
}