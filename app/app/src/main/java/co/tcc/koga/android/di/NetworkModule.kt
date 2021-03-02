package co.tcc.koga.android.di

import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.network.retrofit.Service
import co.tcc.koga.android.data.network.socket.WebSocketService
import co.tcc.koga.android.utils.Constants
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {


    @Singleton
    @Provides
    fun providesRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(Constants.ApiURL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun providesRetrofitInstance(retrofit: Retrofit): Service {
        return retrofit.create(Service::class.java)
    }

    @Singleton
    @Provides
    fun providesWSLifecycle(): LifecycleRegistry  = LifecycleRegistry(0)

    @Singleton
    @Provides
    fun providesScarletInstance(
        okHttpClient: OkHttpClient,
        lifecycleRegistry: LifecycleRegistry
    ): WebSocketService {
        val scarletInstance = Scarlet.Builder()
            .webSocketFactory(
                okHttpClient.newWebSocketFactory(
                    "wss://87davwn2wl.execute-api.us-east-1.amazonaws.com/dev?username=${
                        Client.getInstance().username()
                    }"
                )
            )
            .lifecycle(lifecycleRegistry)
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory())
            .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
            .build()

        return scarletInstance.create()
    }

    @Singleton
    @Provides
    fun providesHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient
            .Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))

            .addInterceptor { chain ->
                val authorizer =
                    "Bearer ${Client.getInstance().getToken()}"
                val request: Request = chain.request()
                val builder = request.newBuilder()
                    .addHeader(
                        "Authorization",
                        authorizer
                    )
                chain.proceed(builder.build())
            }.retryOnConnectionFailure(true).build()
    }
}