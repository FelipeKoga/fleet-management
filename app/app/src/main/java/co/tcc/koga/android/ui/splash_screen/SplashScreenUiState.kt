package co.tcc.koga.android.ui.splash_screen

sealed class SplashScreenUiState {
    object LoggedIn : SplashScreenUiState()
    object LoggedOut : SplashScreenUiState()
    object Error : SplashScreenUiState()
    object Pending : SplashScreenUiState()
}
