package co.tcc.koga.android.ui.chats.chat.viewholder

interface IAudioViewHolder {
    fun playAudio()
    fun pauseAudio()
    fun stopAudio()
    fun setProgress(progress: Int)
    fun setSeekBarMax(duration: Long)
    fun showLoading()
}