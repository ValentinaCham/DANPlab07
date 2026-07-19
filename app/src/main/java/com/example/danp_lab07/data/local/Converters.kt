package com.example.danp_lab07.data.local

import androidx.room.TypeConverter

/**
 * Room TypeConverters shared by all local entities. Stored as a `|` separated
 * string so we don't pull in a JSON library; values are Storage *paths* (or
 * local content URIs while the upload is pending).
 *
 * Empty list ↔ empty string.
 */
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String =
        if (value.isEmpty()) "" else value.joinToString(separator = "\u001F")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split("\u001F")
}
