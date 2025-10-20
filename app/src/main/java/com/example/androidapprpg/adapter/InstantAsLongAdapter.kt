package com.example.androidapprpg.adapter

import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant

/** Converte ISO-8601 (Instant) <-> millis (Long) para o campo ts. */
class InstantAsLongAdapter : JsonDeserializer<Long>, JsonSerializer<Long> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): Long? {
        return when {
            json.isJsonNull -> null
            json.asJsonPrimitive.isNumber -> json.asLong
            else -> runCatching { Instant.parse(json.asString).toEpochMilli() }.getOrNull()
        }
    }
    override fun serialize(src: Long?, typeOfSrc: Type, ctx: JsonSerializationContext): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(src)
    }
}