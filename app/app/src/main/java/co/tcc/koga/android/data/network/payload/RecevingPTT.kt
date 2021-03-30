package co.tcc.koga.android.data.network.payload

import co.tcc.koga.android.data.database.entity.UserEntity
import java.io.Serializable

data class RecevingPTT(
    var isReceiving: Boolean,
    var user: UserEntity?
): Serializable