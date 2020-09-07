package co.tcc.koga.android.di


import android.content.Context
import androidx.room.Room
import co.tcc.koga.android.data.database.AppDatabase
import co.tcc.koga.android.data.database.dao.ChatDAO
import co.tcc.koga.android.data.database.dao.ContactDAO
import co.tcc.koga.android.data.database.dao.MessageDAO
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun providesDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tcc_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideChatDao(database: AppDatabase): ChatDAO = database.chatDAO

    @Singleton
    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDAO = database.messageDAO

    @Singleton
    @Provides
    fun provideContactDao(database: AppDatabase): ContactDAO = database.contactDAO
}