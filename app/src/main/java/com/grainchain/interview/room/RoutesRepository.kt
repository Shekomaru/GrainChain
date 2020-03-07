package com.grainchain.interview.room

import android.content.Context
import androidx.lifecycle.LiveData
import com.grainchain.interview.data.Coord
import com.grainchain.interview.data.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class RoutesRepository(context: Context) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var routesDao: RoutesDao

    init {
        val db = RoutesDatabase.getDatabase(context)
        routesDao = db.RoutesDao()
    }

    fun getAllRoutes(): LiveData<List<Route>> = routesDao.getAllRoutes()

    fun getRoute(routeId: Int) = routesDao.getRoute(routeId)

    fun addRoute(route: Route) {
        launch { addRouteInBG(route) }
    }

    private suspend fun addRouteInBG(route: Route) {
        withContext(Dispatchers.IO) {
            routesDao.insertCompleteRoute(route)
        }
    }

    fun deleteRoute(route: Route) {
        launch { deleteRouteInBG(route) }
    }

    private suspend fun deleteRouteInBG(route: Route) {
        withContext(Dispatchers.IO) {
            routesDao.deleteRoute(route)
        }
    }

    fun getCoordsOfRoute(routeId: Int) {
        routesDao.getCoordsOfRoute(routeId)
    }

    fun insertCoords(vararg coords: Coord) {
        launch { insertCoordsInBG(*coords) }
    }

    private suspend fun insertCoordsInBG(vararg coords: Coord) {
        withContext(Dispatchers.IO) {
            routesDao.insertCoords(*coords)
        }
    }

    fun deleteCoords(routeId: Int) {
        launch { deleteCoordsInBG(routeId) }
    }

    private suspend fun deleteCoordsInBG(routeId: Int) {
        withContext(Dispatchers.IO) {
            routesDao.deletePointsFromRoute(routeId)
        }
    }
}