package com.grainchain.interview.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.grainchain.interview.data.Coord
import com.grainchain.interview.data.Route

@Dao
interface RoutesDao {
    /*Routes methods*/
    @Query("SELECT * FROM routes_table")
    fun getAllRoutes(): List<Route>

    @Insert
    suspend fun insertRoute(route: Route)

    @Delete
    suspend fun deleteRoute(route: Route)

    /*Coords methods*/
    @Query("SELECT * FROM coords_table WHERE route_id = :routeId")
    fun getCoordsOfRoute(routeId: Int): List<Coord>

    @Insert
    suspend fun insertCoords(vararg coords: Coord)

    @Query("DELETE FROM coords_table WHERE route_id = :routeId")
    suspend fun deletePointsFromRoute(routeId: Int)
}
