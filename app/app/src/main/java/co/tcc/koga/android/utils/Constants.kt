package co.tcc.koga.android.utils

object CONSTANTS {
    const val WEBSOCKET_URL = "https://87davwn2wl.execute-api.us-east-1.amazonaws.com/dev"
    const val API_URL = "https://2p8b6trvua.execute-api.us-east-1.amazonaws.com/dev/"
    const val AVATAR_BASE_URL = "https://ui-avatars.com/api/?rounded=true"
}
enum class AUTH_STATUS {
    LOGGED_IN,
    ERROR,
    UNAUTHORIZED,
    PENDING,
    BLOCKED,
    LOGGED_OUT
}

enum class UserRole {
    EMPLOYEE,
    ADMIN,
    OPERATOR
}