package co.tcc.koga.android.data.network.socket

enum class WebSocketActions {
    SEND_MESSAGE,
    OPEN_MESSAGES,
    SEND_LOCATION,
    PUSH_TO_TALK,

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

enum class PushToTalkActions {
    START_PUSH_TO_TALK,
    STOP_PUSH_TO_TALK,
    SEND_PUSH_TO_TALK,
    STARTED_PUSH_TO_TALK,
    STOPPED_PUSH_TO_TALK,
    RECEIVED_PUSH_TO_TALK
}