package co.tcc.koga.android.data.domain

import co.tcc.koga.android.data.database.entity.ChatEntity
import java.io.Serializable

class ChatsResponse(data: List<ChatEntity>, fromCache: Boolean) :
    BaseResponse<List<ChatEntity>>(data, fromCache)