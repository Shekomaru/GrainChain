package com.grainchain.interview.room

import androidx.room.TypeConverter
import java.util.Date

/**
 * Class to hold converters for Room
 */
class RoutesConverters {

    // Long to Date converter
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // Date to Long converter
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}
