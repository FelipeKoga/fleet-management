package co.tcc.koga.android.data.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkUtils {
    companion object {
        fun getRetrofitInstance() : Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://qv7ica8p35.execute-api.us-east-1.amazonaws.com/dev")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
    }
}
