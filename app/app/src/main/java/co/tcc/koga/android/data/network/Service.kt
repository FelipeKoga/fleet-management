package co.tcc.koga.android.data.network

import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.dto.UserDTO
import co.tcc.koga.android.data.network.payload.NewChatPayload
import co.tcc.koga.android.data.network.payload.WebSocketPayload
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import retrofit2.http.*

interface Service {
    @GET("company/{companyId}/users")
    fun getUsers(
        @Path("companyId") companyId: String
    ): Flow<ApiResponse<List<UserEntity>>>

    @GET("user/{username}")
    fun getCurrentUser(
        @Path("username") username: String
    ): Observable<UserEntity>

    @GET("company/{companyId}/users/{username}/chats")
    fun getChats(
        @Path("companyId") companyId: String,
        @Path("username") username: String
    ): Observable<List<ChatEntity>>

    @POST("company/{companyId}/users/{username}/chats ")
    suspend fun createChat(
        @Path("username") username: String,
        @Path("companyId") companyId: String,
        @Body newChatPayload: NewChatPayload,
    ): ChatEntity

    @GET("company/{companyId}/users/{username}/chats/{chatId}/messages")
    fun getMessages(
        @Path("companyId") companyId: String,
        @Path("username") username: String,
        @Path("chatId") chatId: String
    ): Observable<List<MessageEntity>>

}