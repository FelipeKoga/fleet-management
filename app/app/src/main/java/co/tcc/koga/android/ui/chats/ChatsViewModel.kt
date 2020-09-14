package co.tcc.koga.android.ui.chats

import androidx.lifecycle.*
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.repository.ChatsRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatsViewModel @Inject constructor(
    chatsRepository: ChatsRepository,
) : ViewModel() {

    val chats = chatsRepository.getChats().map {
        when (it.status) {
            Resource.Status.LOADING -> {
                Resource.loading(null)
            }
            Resource.Status.SUCCESS -> {
                Resource.success(it.data)
            }
            Resource.Status.LOCAL -> {
                println("LOCAL")
                Resource.localData(it.data)
            }
            Resource.Status.ERROR -> {
                Resource.error(it.message!!, null)
            }
        }
    }.asLiveData(viewModelScope.coroutineContext)
}