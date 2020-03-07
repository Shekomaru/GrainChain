package com.grainchain.interview.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Entity(tableName = "routes_table")
@Parcelize
data class Route(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "name") var name: String = "Not found",
    @Ignore var points: List<Coord> = listOf(),
    @ColumnInfo(name = "start_time") var startTime: Date = Date(),
    @ColumnInfo(name = "end_time") var endTime: Date = Date()
) : Parcelable

@Entity(tableName = "coords_table")
@Parcelize
data class Coord(
    @PrimaryKey(autoGenerate = true) val coordId: Int = 0,
    @ColumnInfo(name = "route_id") val routeId: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable

data class RouteWithCoords(
    @Embedded val route: Route,
    @Relation(
        parentColumn = "id",
        entityColumn = "route_id"
    ) val coords: List<Coord>
)
