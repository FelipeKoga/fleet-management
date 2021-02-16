package co.tcc.koga.android.data.repository.impl

import android.content.Context
import co.tcc.koga.android.data.network.payload.UploadResponse
import co.tcc.koga.android.data.network.payload.UploadUrlPayload
import co.tcc.koga.android.data.network.retrofit.Service
import co.tcc.koga.android.data.repository.AudioRepository
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject


class AudioRepositoryImpl @Inject constructor(val service: Service, private val context: Context) :
    AudioRepository {

    override fun uploadUrl(key: String): Observable<UploadResponse> {
        val payload = UploadUrlPayload(key)
        return service.uploadUrl(payload)
    }

    override fun uploadAudio(file: File, putURL: String): Observable<Boolean> {
        println(file)
        println(putURL)
        val body: RequestBody =
            RequestBody.create(MediaType.parse("audio/wav"), file)
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context.applicationContext,
            "us-east-1:37c2e9f4-7fac-4274-bd9e-747803cbbedb",
            Regions.US_EAST_1
        )
        val s3Client = AmazonS3Client(credentialsProvider)
        val transferUtility = TransferUtility.builder().s3Client(s3Client).context(context).build()


        println(putURL)
        val transferObserver = transferUtility.upload(
            "tcc-projects-assets",
            putURL,
            file,
            CannedAccessControlList.PublicRead
        )

        var obs = Observable.just(false)

        transferObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                println(state)
                obs = Observable.just(true)
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                println(bytesCurrent)
            }

            override fun onError(id: Int, ex: Exception?) {
                println(ex)
            }

        })
        return obs

    }
}