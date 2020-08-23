package co.tcc.koga.android.domain

import java.io.Serializable

data class Message(
    var message: String,
    var sender: Contact,
    var recipient: Contact,
    var hasAudio: Boolean = false,
    var received: Boolean,
    var date: String
): Serializable