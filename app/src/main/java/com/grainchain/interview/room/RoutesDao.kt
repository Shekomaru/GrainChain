package com.grainchain.interview.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.grainchain.interview.data.Coord
import com.grainchain.interview.data.Route
import com.grainchain.interview.data.RouteWithCoords

@Dao
interface RoutesDao {
    /*Routes methods*/
    @Query("SELECT * FROM routes_table")
    fun getAllRoutes(): LiveData<List<Route>>

    @Transaction
    @Query("SELECT * FROM routes_table WHERE id = :routeId LIMIT 1")
    fun getRoute(routeId: Int): RouteWithCoords

    @Insert
    suspend fun insertRoute(route: Route)

    @Transaction
    suspend fun insertCompleteRoute(route: Route) {
        insertRoute(route)
        insertCoords(*route.points.toTypedArray())
    }

    @Delete
    suspend fun deleteRoute(route: Route)

    @Transaction
    suspend fun deleteCompleteRoute(route: Route) {
        deleteRoute(route)
        deletePointsFromRoute(route.id)
    }

    /*Coords methods*/
    @Query("SELECT * FROM coords_table WHERE route_id = :routeId")
    fun getCoordsOfRoute(routeId: Int): List<Coord>

    @Insert
    suspend fun insertCoords(vararg coords: Coord)

    @Query("DELETE FROM coords_table WHERE route_id = :routeId")
    suspend fun deletePointsFromRoute(routeId: Int)
}
