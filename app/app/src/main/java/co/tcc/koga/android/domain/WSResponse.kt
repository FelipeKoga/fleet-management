package co.tcc.koga.android.domain


import org.json.JSONObject
import java.io.Serializable

data class WSResponse(
    var data: Any,
    var action: String
): Serializable