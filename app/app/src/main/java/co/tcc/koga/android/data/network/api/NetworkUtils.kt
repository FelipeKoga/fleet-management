package co.tcc.koga.android.data.network.api

import com.amazonaws.mobile.client.AWSMobileClient
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class NetworkUtils {
    companion object {
        private const val baseUrl = "https://2p8b6trvua.execute-api.us-east-1.amazonaws.com/dev/"
        private lateinit var retrofit: Retrofit
        private lateinit var okHttpClient: OkHttpClient

        private fun getRetrofitInstance(): Retrofit {
            if (!Companion::retrofit.isInitialized) {
                println("INIT RETROFIT")
                initHttpClient()
                retrofit = Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(baseUrl)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
            }

            return retrofit
        }

        private fun initHttpClient() {
            okHttpClient = OkHttpClient.Builder().addInterceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader(
                        "Authorization",
                        "Bearer ${AWSMobileClient.getInstance().tokens.idToken.tokenString}"
                    )
                    .build()
                chain.proceed(newRequest)
            }.retryOnConnectionFailure(true).build()
        }

        val api: Service by lazy {
            getRetrofitInstance().create(Service::class.java)
        }
    }


}
