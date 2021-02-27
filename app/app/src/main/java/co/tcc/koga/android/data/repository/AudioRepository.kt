package co.tcc.koga.android.data.repository

import co.tcc.koga.android.data.network.payload.UploadResponse
import io.reactivex.Observable
import okhttp3.ResponseBody
import java.io.File

interface AudioRepository {
    fun uploadUrl(key: String): Observable<UploadResponse>

    fun uploadAudio(file: File, url: String): Observable<ResponseBody>

}