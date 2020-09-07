package co.tcc.koga.android.di

import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.MessageRepository
import co.tcc.koga.android.data.repository.implementations.ChatsRepositoryImpl
import co.tcc.koga.android.data.repository.implementations.ClientRepositoryImpl
import co.tcc.koga.android.data.repository.implementations.MessageRepositoryImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class DataModule {

    @Singleton
    @Binds
    abstract fun provideClientRepository(repository: ClientRepositoryImpl): ClientRepository

    @Singleton
    @Binds
    abstract fun provideChatsRepository(repository: ChatsRepositoryImpl): ChatsRepository

    @Singleton
    @Binds
    abstract fun provideMessageRepository(repository: MessageRepositoryImpl): MessageRepository
}