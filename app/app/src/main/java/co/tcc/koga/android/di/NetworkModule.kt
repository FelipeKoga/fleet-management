package co.tcc.koga.android.di

import android.content.Context
import co.tcc.koga.android.data.network.Service
import co.tcc.koga.android.data.utils.CallAdapterFactory
import co.tcc.koga.android.utils.CONSTANTS
import com.amazonaws.mobile.client.AWSMobileClient
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {


    @Singleton
    @Provides
    fun providesRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(CONSTANTS.API_URL)
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
    fun providesHttpClient(): OkHttpClient {
    println("PROVIDES")

        return OkHttpClient
            .Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                println("SHOW DE BOLA")
                val authorizer =
                    "Bearer ${AWSMobileClient.getInstance().tokens.idToken.tokenString}"
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