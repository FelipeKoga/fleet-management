package co.tcc.koga.android.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.tcc.koga.android.data.database.converter.MembersConverter
import co.tcc.koga.android.data.database.converter.MessageConverter
import co.tcc.koga.android.data.database.converter.UserConverter
import co.tcc.koga.android.data.database.dao.ChatDAO
import co.tcc.koga.android.data.database.dao.MessageDAO
import co.tcc.koga.android.data.database.dao.UserDAO
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity

@Database(entities = [ChatEntity::class, UserEntity::class, MessageEntity::class], version = 30)
@TypeConverters(MembersConverter::class, UserConverter::class, MessageConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val messageDAO: MessageDAO
    abstract val chatDAO: ChatDAO
    abstract val userDao: UserDAO
}