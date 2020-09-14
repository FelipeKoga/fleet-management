package co.tcc.koga.android.di

import co.tcc.koga.android.data.network.Service
import co.tcc.koga.android.data.utils.CallAdapterFactory
import co.tcc.koga.android.utils.CONSTANTS
import com.amazonaws.mobile.client.AWSMobileClient
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Singleton
    @Provides
    fun providesRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(CONSTANTS.API_URL)
            .addCallAdapterFactory(CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
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
        return OkHttpClient
            .Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val authorizer =
                    "Bearer ${AWSMobileClient.getInstance().tokens.idToken.tokenString}"
                chain.proceed(request.newBuilder().addHeader(
                    "Authorization",
                    authorizer
                ).build())
            }.retryOnConnectionFailure(true).build()
    }
}