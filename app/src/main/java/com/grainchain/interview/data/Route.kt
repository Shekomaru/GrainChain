package com.grainchain.interview.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Entity(tableName = "routes_table")
@Parcelize
data class Route(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "name") var name: String = "Not found",
    @Ignore var points: List<Pair<Double, Double>> = listOf(),
    @ColumnInfo(name = "start_time") var startTime: Date = Date(),
    @ColumnInfo(name = "end_time") var endTime: Date = Date()
) : Parcelable

@Entity(tableName = "coords_table")
data class Coord(
    @PrimaryKey(autoGenerate = true) val coordId: Int,
    @ColumnInfo(name = "route_id") val routeId: Int,
    val latitude: Double,
    val longitude: Double
)
