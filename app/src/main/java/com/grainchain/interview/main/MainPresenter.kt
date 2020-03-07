package com.grainchain.interview.main

import android.content.Context
import com.google.android.gms.location.LocationResult
import com.grainchain.interview.data.Coord
import com.grainchain.interview.data.Route
import com.grainchain.interview.helpers.RoutesManager
import com.grainchain.interview.helpers.RoutesManagerListener
import com.grainchain.interview.room.RoutesRepository
import java.util.Date

class MainPresenterImpl(private val view: MainView, context: Context) :
    MainPresenter, RoutesManagerListener {
    private val routesManager = RoutesManager(this)
    val routesRepository = RoutesRepository(context)

    override fun saveRoute(
        name: String,
        points: List<LocationResult>,
        startTime: Date,
        endTime: Date
    ) {
        val route = Route(
            0,
            name,
            points.map {
                it.lastLocation.run {
                    Coord(latitude = latitude, longitude = longitude)
                }
            },
            startTime,
            endTime
        )

        routesManager.addRoute(route)

        routesRepository.addRoute(route)
    }

    override fun onOnResume() {
        view.updateRoutesList(routesRepository.getAllRoutes())
    }

    override fun onRoutesChanged(routes: List<Route>) {
        view.updateRoutesList(routes)
    }

}

interface MainPresenter {
    fun saveRoute(name: String, points: List<LocationResult>, startTime: Date, endTime: Date)
    fun onOnResume()
}