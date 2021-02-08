package co.tcc.koga.android.utils

import android.content.Context
import android.widget.ImageView
import co.tcc.koga.android.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


fun loadImage(
    context: Context,
    imageView: ImageView,
    url: String?,
    placeholder: Int,
) {
    Glide
        .with(context)
        .load(url)
        .centerInside()
        .apply(RequestOptions.circleCropTransform())
        .placeholder(placeholder)
        .into(imageView)
}