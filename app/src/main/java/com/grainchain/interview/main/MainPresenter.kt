package com.grainchain.interview.main

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationResult
import com.grainchain.interview.data.Coord
import com.grainchain.interview.data.Route
import com.grainchain.interview.data.RouteWithCoords
import com.grainchain.interview.helpers.RoutesManagerListener
import com.grainchain.interview.room.RoutesRepository
import java.util.Date

class MainPresenterImpl(private val view: MainView, context: Context) :
    MainPresenter, RoutesManagerListener {
    //    private val routesManager = RoutesManager(this)
    val routesRepository = RoutesRepository(context)
    val routes: LiveData<List<Route>> = routesRepository.getAllRoutes()

    override fun saveRoute(
        name: String,
        points: List<LocationResult>,
        startTime: Date,
        endTime: Date
    ) {
        val route = Route(
            startTime.time,
            name,
            points.map {
                it.lastLocation.run {
                    Coord(latitude = latitude, longitude = longitude, routeId = startTime.time)
                }
            },
            startTime,
            endTime
        )

//        routesManager.addRoute(route)

        routesRepository.addRoute(route)
    }

    override fun onRoutesChanged(routes: List<Route>) {
//        view.updateRoutesList(routes)
    }

    fun getCompleteRoute(routeId: Int): RouteWithCoords {
        return routesRepository.getRoute(routeId)
    }

}

interface MainPresenter {
    fun saveRoute(name: String, points: List<LocationResult>, startTime: Date, endTime: Date)
}