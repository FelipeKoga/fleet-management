package co.tcc.koga.android.data.network.socket

enum class WebSocketActions {
    NEW_MESSAGE,
    MESSAGE_SENT,
    SEND_MESSAGE,
    OPEN_MESSAGES,
    CHAT_UPDATED
}

enum class ChatActions {
    CHAT_UPDATED
}

enum class MessageActions {
    NEW_MESSAGE,
    MESSAGE_SENT,
    OPEN_MESSAGES
}

enum class UserActions {
    USER_CONNECTED,
    USER_DISCONNECTED
}