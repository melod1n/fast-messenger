package dev.meloda.fast.database.typeconverters

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun intListToString(list: List<Int>): String = list.joinToString()

    @TypeConverter
    fun stringToIntList(string: String): List<Int> =
        string
            .split(", ")
            .mapNotNull(String::toIntOrNull)

    @TypeConverter
    fun longListToString(list: List<Long>): String = list.joinToString()

    @TypeConverter
    fun stringToLongList(string: String): List<Long> =
        string
            .split(", ")
            .mapNotNull(String::toLongOrNull)

    @TypeConverter
    fun stringListToString(list: List<String>): String = list.joinToString()

    @TypeConverter
    fun stringToStringList(string: String): List<String> = string.split(", ")
}
