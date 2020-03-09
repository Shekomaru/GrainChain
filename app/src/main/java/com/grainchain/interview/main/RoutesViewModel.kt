package com.grainchain.interview.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationResult
import com.grainchain.interview.data.Coord
import com.grainchain.interview.data.Route
import com.grainchain.interview.room.RoutesRepository
import java.util.Date

open class RoutesViewModel(application: Application) : AndroidViewModel(application) {
    private val routesRepository = RoutesRepository(application)
    val routes: LiveData<List<Route>> = routesRepository.getAllRoutes()

    fun saveRoute(
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

        routesRepository.addRoute(route)
    }

    fun getCoordsOfRoute(routeId: Long): List<Coord> {
        return routesRepository.getCoordsOfRoute(routeId)
    }

    fun deleteRouteById(routeId: Long) {
        routesRepository.deleteRouteById(routeId)
    }

}
