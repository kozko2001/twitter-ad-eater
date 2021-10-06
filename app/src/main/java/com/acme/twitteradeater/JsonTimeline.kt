package com.acme.twitteradeater

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

fun JsonObject.getObject(member: String): JsonObject? {
    if(this.has(member)) {
        return this.getAsJsonObject(member)
    }

    return null
}

fun JsonObject.getArray(member: String): JsonArray? {
    if(this.has(member)) {
        return this.getAsJsonArray(member)
    }
    return null
}


class JsonTimeline (private val original: String) {
    val gson = Gson()

    fun removePromote(): String {
        val json = gson.fromJson(original, JsonObject::class.java)

        val instructions = json.getObject("timeline")
            ?.getArray("instructions")

        val addEntries = instructions?.map { it.asJsonObject?.getObject("addEntries") }

        val entries = addEntries?.map { it?.asJsonObject?.getArray("entries") }

        entries?.onEach {
            it?.removeAll {
                it.asJsonObject.get("entryId")?.asString?.let { it.contains("promoted") } ?: false
            }
        }


        val output = gson.toJson(json)
        return output
    }

}