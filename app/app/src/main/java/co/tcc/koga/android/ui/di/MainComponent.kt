package co.tcc.koga.android.ui.di

import co.tcc.koga.android.service.LocationService
import co.tcc.koga.android.service.PushToTalkService
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.ui.auth.confirm_forgot_password.ConfirmForgotPasswordFragment
import co.tcc.koga.android.ui.auth.forgot_password.ForgotPasswordFragment
import co.tcc.koga.android.ui.chats.chat.ChatFragment
import co.tcc.koga.android.ui.chats.ChatsFragment
import co.tcc.koga.android.ui.chats.new_group.NewGroupFragment
import co.tcc.koga.android.ui.auth.login.LoginFragment
import co.tcc.koga.android.ui.chats.new_chat.NewChatFragment
import co.tcc.koga.android.ui.details.group.GroupDetailsFragment
import co.tcc.koga.android.ui.details.user.UserDetailsFragment
import co.tcc.koga.android.ui.profile.ProfileFragment
import co.tcc.koga.android.ui.settings.SettingsFragment
import co.tcc.koga.android.ui.splash_screen.SplashScreenFragment
import dagger.Subcomponent


@Subcomponent(modules = [MainModule::class])
interface MainComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainComponent
    }

    fun inject(activity: MainActivity)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: ForgotPasswordFragment)
    fun inject(fragment: ConfirmForgotPasswordFragment)
    fun inject(fragment: ChatsFragment)
    fun inject(fragment: ChatFragment)
    fun inject(fragment: SettingsFragment)
    fun inject(fragment: NewChatFragment)
    fun inject(fragment: NewGroupFragment)
    fun inject(fragment: SplashScreenFragment)
    fun inject(fragment: ProfileFragment)
    fun inject(fragment: UserDetailsFragment)
    fun inject(fragment: GroupDetailsFragment)

    fun inject(service: LocationService)
    fun inject(service: PushToTalkService)

}