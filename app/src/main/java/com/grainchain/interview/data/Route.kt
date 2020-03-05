package com.grainchain.interview.data

import com.google.android.gms.location.LocationResult
import java.util.Date

data class Route(
    val name: String,
    val points: List<LocationResult>,
    val startTime: Date,
    val endTime: Date
)