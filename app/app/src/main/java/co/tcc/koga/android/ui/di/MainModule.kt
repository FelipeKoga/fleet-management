package co.tcc.koga.android.ui.di

import androidx.lifecycle.ViewModel
import co.tcc.koga.android.di.ViewModelKey
import co.tcc.koga.android.ui.chat.ChatViewModel
import co.tcc.koga.android.ui.chats.ChatsViewModel
import co.tcc.koga.android.ui.login.LoginViewModel
import co.tcc.koga.android.ui.new_chat.NewChatViewModel
import co.tcc.koga.android.ui.new_group.NewGroupViewModel
import co.tcc.koga.android.ui.settings.SettingsViewModel
import co.tcc.koga.android.ui.splash_screen.SplashScreenViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface MainModule {

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    fun bindLoginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatsViewModel::class)
    fun bindChatsViewModel(viewModel: ChatsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    fun bindChatViewModel(viewModel: ChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashScreenViewModel::class)
    fun bindSplashScreenViewModel(viewModel: SplashScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewChatViewModel::class)
    fun bindNewChatViewModel(viewModel: NewChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewGroupViewModel::class)
    fun bindNewGroupViewModel(viewModel: NewGroupViewModel): ViewModel

}
