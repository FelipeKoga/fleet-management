package co.tcc.koga.android.data.database.converter

import androidx.room.TypeConverter
import co.tcc.koga.android.data.database.entity.MessageEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MessagesConverter {
    private var gson = Gson()

    @TypeConverter
    fun toList(data: String?): MutableList<MessageEntity?> {
        if (data == null) return mutableListOf()
        val model: Type = object : TypeToken<MutableList<MessageEntity?>>() {}.type
        return gson.fromJson(data, model)
    }


    @TypeConverter
    fun toString(data: MutableList<MessageEntity?>): String {
        return gson.toJson(data)
    }
}