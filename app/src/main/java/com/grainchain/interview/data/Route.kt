package com.grainchain.interview.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class Route(
    val name: String = "Not found",
    val points: List<Pair<Double, Double>> = listOf(),
    val startTime: Date = Date(),
    val endTime: Date = Date()
) : Parcelable