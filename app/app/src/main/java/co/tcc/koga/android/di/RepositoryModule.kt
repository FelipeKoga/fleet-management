package co.tcc.koga.android.di

import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.MessageRepository
import co.tcc.koga.android.data.repository.UserRepository
import co.tcc.koga.android.data.repository.impl.ChatsRepositoryImpl
import co.tcc.koga.android.data.repository.impl.ClientRepositoryImpl
import co.tcc.koga.android.data.repository.impl.MessageRepositoryImpl
import co.tcc.koga.android.data.repository.impl.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun providesClientRepository(repository: ClientRepositoryImpl): ClientRepository

    @Singleton
    @Binds
    abstract fun providesChatsRepository(repository: ChatsRepositoryImpl): ChatsRepository

    @Singleton
    @Binds
    abstract fun providesMessageRepository(repository: MessageRepositoryImpl): MessageRepository

    @Singleton
    @Binds
    abstract fun providesUserRepository(repository: UserRepositoryImpl): UserRepository
}