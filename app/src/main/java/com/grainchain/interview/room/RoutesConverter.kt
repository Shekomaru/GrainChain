package com.grainchain.interview.room

import androidx.room.TypeConverter
import java.util.Date

class RoutesConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun fromPointToString(point: Pair<Double, Double>?): String? {
        return if (point != null) "${point.first}\$${point.second}" else null
    }

    @TypeConverter
    fun fromStringToPoint(string: String?): Pair<Double, Double>? {
        if (string == null) {
            return null
        }

        val stringParts = string.split("\$")
        if (stringParts.size != 2) {
            return null
        }

        return Pair(stringParts[0].toDouble(), stringParts[1].toDouble())
    }
}