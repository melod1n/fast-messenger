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
    fun stringListToString(list: List<String>): String = list.joinToString()

    @TypeConverter
    fun stringToStringList(string: String): List<String> = string.split(", ")
}
