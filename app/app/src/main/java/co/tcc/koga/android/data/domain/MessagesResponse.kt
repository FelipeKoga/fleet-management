package co.tcc.koga.android.data.domain

import co.tcc.koga.android.data.database.entity.MessageEntity

class MessagesResponse(data: MutableList<MessageEntity?>, fromCache: Boolean) :
    BaseResponse<MutableList<MessageEntity?>>(data, fromCache)