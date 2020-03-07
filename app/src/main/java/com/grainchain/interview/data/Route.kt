package com.grainchain.interview.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Entity(tableName = "route")
@Parcelize
data class Route(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String = "Not found",
    @ColumnInfo(name = "points") val points: List<Pair<Double, Double>> = listOf(),
    @ColumnInfo(name = "start_time") val startTime: Date = Date(),
    @ColumnInfo(name = "end_time") val endTime: Date = Date()
) : Parcelable