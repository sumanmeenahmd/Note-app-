package com.example.data

import androidx.room.TypeConverter
import com.example.model.ChecklistItem
import com.example.model.NoteCategory
import com.example.model.NoteColor
import org.json.JSONArray
import org.json.JSONObject

class NoteConverters {
    @TypeConverter
    fun fromChecklistItems(items: List<ChecklistItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("isChecked", item.isChecked)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toChecklistItems(raw: String): List<ChecklistItem> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        ChecklistItem(
                            id = obj.optString("id"),
                            text = obj.optString("text"),
                            isChecked = obj.optBoolean("isChecked", false)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromTags(tags: List<String>): String {
        val array = JSONArray()
        tags.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toTags(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val tag = array.optString(i).trim()
                    if (tag.isNotEmpty()) add(tag)
                }
            }
        }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromCategory(category: NoteCategory): String = category.name

    @TypeConverter
    fun toCategory(name: String): NoteCategory =
        NoteCategory.entries.firstOrNull { it.name == name } ?: NoteCategory.PERSONAL

    @TypeConverter
    fun fromColor(color: NoteColor): String = color.name

    @TypeConverter
    fun toColor(name: String): NoteColor =
        NoteColor.entries.firstOrNull { it.name == name } ?: NoteColor.DEFAULT
}
