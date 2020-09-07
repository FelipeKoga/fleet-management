package co.tcc.koga.android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import co.tcc.koga.android.data.database.dao.ChatDAO
import co.tcc.koga.android.data.database.dao.ContactDAO
import co.tcc.koga.android.data.database.dao.MessageDAO
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.ContactEntity
import co.tcc.koga.android.data.database.entity.MessageEntity

@Database(entities = [MessageEntity::class, ChatEntity::class, ContactEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract val messageDAO: MessageDAO
    abstract val chatDAO: ChatDAO
    abstract val contactDAO: ContactDAO
}