package co.tcc.koga.android.data.network.retrofit

import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.payload.NewChatPayload
import co.tcc.koga.android.data.network.payload.UploadResponse
import co.tcc.koga.android.data.network.payload.UploadUrlPayload
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface Service {
    @GET("company/{companyId}/users")
    fun getUsers(
        @Path("companyId") companyId: String
    ): Observable<List<UserEntity>>

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
    ): Observable<MutableList<MessageEntity?>>

    @PUT("company/{companyId}/users/{username}")
    fun updateUser(
        @Path("companyId") companyId: String,
        @Path("username") username: String,
        @Body body: UserEntity
    ): Observable<UserEntity>


    @POST("files")
    fun uploadUrl(
        @Body uploadUrlPayload: UploadUrlPayload
    ): Observable<UploadResponse>

    @PUT
    fun uploadAudio(
        @Url url: String,
        @Header("Content-type") contentType: String,
        @Body body: RequestBody
    ): Observable<Unit>

}