package co.tcc.koga.android.data.network.socket

enum class WebSocketActions {
    SEND_MESSAGE,
    OPEN_MESSAGES,
    SEND_LOCATION
}

enum class ChatActions {
    CHAT_UPDATED,
    CHAT_CREATED
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