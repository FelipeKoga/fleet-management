package co.tcc.koga.android.data.network.api

import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.ContactEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.domain.Contact
import retrofit2.http.*

interface Service {
    @GET("company/{companyId}/user")
    suspend fun getContacts(
        @Path("companyId") companyId: String
    ): MutableList<ContactEntity>

    @GET("user")
    suspend fun getCurrentUser(@Query("email") email: String): ContactEntity

    @GET("company/{companyId}/users/{userId}/chats")
    suspend fun getChats(
        @Path("companyId") companyId: String,
        @Path("userId") userId: String
    ): List<ChatEntity>

    @GET("company/{companyId}/users/{userId}/chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("companyId") companyId: String,
        @Path("userId") userId: String,
        @Path("chatId") chatId: String
    ): List<MessageEntity>

}