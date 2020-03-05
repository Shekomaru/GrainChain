package com.grainchain.interview.main

import com.google.android.gms.location.LocationResult
import com.grainchain.interview.data.Route
import java.util.Date

class MainPresenterImpl(view: MainView) : MainPresenter {

    var routes: List<Route> = listOf()

    override fun saveRoute(
        name: String,
        points: List<LocationResult>,
        startTime: Date,
        endTime: Date
    ) {
        val route = Route(
            name, points, startTime, endTime
        )

        routes = routes + route
    }

}

interface MainPresenter {
    fun saveRoute(name: String, points: List<LocationResult>, startTime: Date, endTime: Date)
}