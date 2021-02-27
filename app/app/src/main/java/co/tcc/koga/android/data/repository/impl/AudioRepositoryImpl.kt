package co.tcc.koga.android.data.repository.impl

import android.content.Context
import co.tcc.koga.android.data.network.payload.UploadResponse
import co.tcc.koga.android.data.network.payload.UploadUrlPayload
import co.tcc.koga.android.data.network.retrofit.Service
import co.tcc.koga.android.data.repository.AudioRepository
import co.tcc.koga.android.utils.CONSTANTS
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AudioRepositoryImpl @Inject constructor(val service: Service, private val context: Context) :
    AudioRepository {

    override fun uploadUrl(key: String): Observable<UploadResponse> {
        val payload = UploadUrlPayload(key)
        return service.uploadUrl(payload)
    }

    override fun uploadAudio(file: File, url: String): Observable<ResponseBody> {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val retrofit = Retrofit.Builder()
            .client(
                OkHttpClient
                    .Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .retryOnConnectionFailure(true).build()
            )
            .baseUrl(CONSTANTS.API_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(Service::class.java)

        val avatarBody = RequestBody.create(MediaType.parse("audio/wav"), file)
        return service.uploadFile(url, avatarBody)
    }
}