package co.tcc.koga.android.ui

import com.tinder.scarlet.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.utils.AUTH_STATUS
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: ClientRepository,
    private val lifecycleRegistry: LifecycleRegistry
) :
    ViewModel() {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun observeAuthStatus() {
        compositeDisposable.add(repository.observeAuthStatus().subscribe { status ->
            if (status === AUTH_STATUS.LOGGED_OUT) {
                lifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason(ShutdownReason.GRACEFUL))
            }

            if (status === AUTH_STATUS.LOGGED_IN) {
                lifecycleRegistry.onNext(Lifecycle.State.Started)
            }
        })
    }

    override fun onCleared() {
        compositeDisposable.clear()
    }
}