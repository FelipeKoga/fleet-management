package co.tcc.koga.android.data.database.converter

import androidx.room.TypeConverter
import co.tcc.koga.android.data.database.entity.UserEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class UserConverter {
    private var gson = Gson()

    @TypeConverter
    fun toList(data: String?): UserEntity? {
        if (data === "null") return null
        val model: Type = object : TypeToken<UserEntity>() {}.type
        return gson.fromJson(data, model)
    }

    @TypeConverter
    fun toString(data: UserEntity?): String? {
        return gson.toJson(data)
    }
}