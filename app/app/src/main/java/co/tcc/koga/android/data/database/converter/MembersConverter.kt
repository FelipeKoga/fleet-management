package co.tcc.koga.android.data.database.converter

import androidx.room.TypeConverter
import co.tcc.koga.android.data.database.entity.UserEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MembersConverter {
    private var gson = Gson()

    @TypeConverter
    fun toList(data: String?): List<UserEntity> {
        if (data == "null") return listOf()
        val model: Type = object : TypeToken<List<UserEntity>>() {}.type
        return gson.fromJson(data, model)
    }


    @TypeConverter
    fun toString(data: List<UserEntity>?): String? {
        return gson.toJson(data)
    }
}