package co.tcc.koga.android.di


import android.content.Context
import androidx.room.Room
import co.tcc.koga.android.data.database.AppDatabase
import co.tcc.koga.android.data.database.dao.ChatDAO
import co.tcc.koga.android.data.database.dao.MessageDAO
import co.tcc.koga.android.data.database.dao.UserDAO
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
    fun providesChatDao(database: AppDatabase): ChatDAO = database.chatDAO

    @Singleton
    @Provides
    fun providesMessageDao(database: AppDatabase): MessageDAO = database.messageDAO

    @Singleton
    @Provides
    fun providesUserDao(database: AppDatabase): UserDAO = database.userDao
}