package co.tcc.koga.android.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import co.tcc.koga.android.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target


class Avatar {
    companion object {
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
    }
}
