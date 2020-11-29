package co.tcc.koga.android.data.database.converter

import androidx.room.TypeConverter
import co.tcc.koga.android.data.database.entity.MessageEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MessageConverter {
    private var gson = Gson()

    @TypeConverter
    fun toList(data: String?): MessageEntity? {
        println("toList: $data")
        if (data == null) return null
        val model: Type = object : TypeToken<MessageEntity?>() {}.type
        return gson.fromJson(data, model)
    }


    @TypeConverter
    fun toString(data: MessageEntity?): String? {
        println("toString: $data")
        if (data == null) return null
        return gson.toJson(data)
    }
}