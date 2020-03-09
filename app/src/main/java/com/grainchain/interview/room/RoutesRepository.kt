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

    fun addRoute(route: Route) {
        launch { addRouteInBG(route) }
    }

    private suspend fun addRouteInBG(route: Route) {
        withContext(Dispatchers.IO) {
            routesDao.insertCompleteRoute(route)
        }
    }

    fun deleteRouteById(routeId: Long){
        launch { deleteRouteByIdInBG(routeId)}
    }

    private suspend fun deleteRouteByIdInBG(routeId: Long){
        withContext(Dispatchers.IO){
            routesDao.deleteCompleteRouteById(routeId)
        }
    }

    fun getCoordsOfRoute(routeId: Long): List<Coord> {
        return routesDao.getCoordsOfRoute(routeId)
    }

}