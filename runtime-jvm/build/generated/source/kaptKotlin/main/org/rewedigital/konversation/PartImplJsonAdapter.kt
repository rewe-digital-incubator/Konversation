// Code generated by moshi-kotlin-codegen. Do not edit.
package org.rewedigital.konversation

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.NullPointerException
import kotlin.String
import kotlin.collections.MutableList

internal class PartImplJsonAdapter(moshi: Moshi) : JsonAdapter<PartImpl>() {
    private val options: JsonReader.Options = JsonReader.Options.of("variants", "type")

    private val mutableListOfStringAdapter: JsonAdapter<MutableList<String>> =
            moshi.adapter<MutableList<String>>(Types.newParameterizedType(MutableList::class.java, String::class.java), kotlin.collections.emptySet(), "variants")

    private val partTypeAdapter: JsonAdapter<PartType> =
            moshi.adapter<PartType>(PartType::class.java, kotlin.collections.emptySet(), "type")

    override fun toString(): String = "GeneratedJsonAdapter(PartImpl)"

    override fun fromJson(reader: JsonReader): PartImpl {
        var variants: MutableList<String>? = null
        var type: PartType? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> variants = mutableListOfStringAdapter.fromJson(reader) ?: throw JsonDataException("Non-null value 'variants' was null at ${reader.path}")
                1 -> type = partTypeAdapter.fromJson(reader) ?: throw JsonDataException("Non-null value 'type' was null at ${reader.path}")
                -1 -> {
                    // Unknown name, skip it.
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        var result = PartImpl(
                variants = variants ?: throw JsonDataException("Required property 'variants' missing at ${reader.path}"),
                type = type ?: throw JsonDataException("Required property 'type' missing at ${reader.path}"))
        return result
    }

    override fun toJson(writer: JsonWriter, value: PartImpl?) {
        if (value == null) {
            throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("variants")
        mutableListOfStringAdapter.toJson(writer, value.variants)
        writer.name("type")
        partTypeAdapter.toJson(writer, value.type)
        writer.endObject()
    }
}