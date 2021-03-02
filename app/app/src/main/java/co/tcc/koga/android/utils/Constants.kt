package co.tcc.koga.android.utils


sealed class Constants {
    companion object {
        const val WebsocketURL = "https://87davwn2wl.execute-api.us-east-1.amazonaws.com/dev"
        const val ApiURL = "https://2p8b6trvua.execute-api.us-east-1.amazonaws.com/dev/"
        const val AvatarBaseURL = "https://ui-avatars.com/api/?rounded=true"

        fun getAvatarURL(name: String, color: String, size: Int = 42): String {
            return "${AvatarBaseURL}&name=${name}&background=${color}&size=$size"
        }
    }

    enum class AuthStatus {
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
}

