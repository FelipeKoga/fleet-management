package co.tcc.koga.android.utils

import java.io.Serializable

data class FormField(
    var value: String = "",
    var errorMessage: String? = null,
    var hasError: Boolean = false
): Serializable