package co.tcc.koga.android.di

import co.tcc.koga.android.data.repository.*
import co.tcc.koga.android.data.repository.impl.*
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

    @Singleton
    @Binds
    abstract fun providesAudioRepository(repository: AudioRepositoryImpl): AudioRepository

    @Singleton
    @Binds
    abstract fun providesPushToTalkRepository(repository: PushToTalkRepositoryImpl): PushToTalkRepository
}