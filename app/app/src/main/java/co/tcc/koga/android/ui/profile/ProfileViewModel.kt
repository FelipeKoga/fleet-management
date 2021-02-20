package co.tcc.koga.android.ui.profile


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.UserRepository
import co.tcc.koga.android.ui.auth.login.LoginViewModel
import co.tcc.koga.android.utils.UserRole
import co.tcc.koga.android.utils.getUserAvatar
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    clientRepository: ClientRepository,
    private val userRepository: UserRepository,
    val context: Context
) : ViewModel() {
    var formFields: ProfileForm = ProfileForm()
    val formErrors = MutableLiveData<MutableMap<String, String>>(mutableMapOf())
    private val _isLoading = MutableLiveData(false)
    private val _isLoadingUpload = MutableLiveData(false)

    private var currentUser: UserEntity = clientRepository.user()

    val isLoading: LiveData<Boolean> get() = _isLoading
    val isLoadingUpload: LiveData<Boolean> get() = _isLoadingUpload

    init {
        formFields.init(currentUser)
    }


    fun getAvatar(): String {
        return currentUser.avatarUrl ?: getUserAvatar(
            currentUser
        )
    }

    fun getRole(): String {
        return when(currentUser.role) {
            UserRole.EMPLOYEE.name -> "Funcionário"
            UserRole.ADMIN.name -> "Administrador"
            UserRole.EMPLOYEE.name -> "Operador"
            else -> "Funcionário"
        }
    }

    private fun isFormValid(): Boolean {
        val errors = mutableMapOf<String, String>()
        formErrors.postValue(errors)

        if (formFields.name.isEmpty()) {
            errors["name"] = context.getString(R.string.required)
        }

        if (formFields.email.isEmpty()) {
            errors["email"] = context.getString(R.string.required)
        }

        if (formFields.phone.isEmpty()) {
            errors["phone"] = context.getString(R.string.required)
        }

        formErrors.postValue(errors)

        return errors.isEmpty()
    }

    fun save() = viewModelScope.launch {
        if (isFormValid()) {
            val newCurrentUser = currentUser
            newCurrentUser.apply {
                name = formFields.name
                phone = formFields.phone
                email = formFields.email
                customName = formFields.customName
            }
            _isLoading.postValue(true)
            withContext(Dispatchers.IO) {
                userRepository.updateUser(newCurrentUser).subscribe {
                    _isLoading.postValue(false)
                }
            }
        }
    }

    fun uploadPhoto(file: File) = viewModelScope.launch {
        _isLoadingUpload.postValue(true)
        val key = "company/${currentUser.companyId}/user/${currentUser.username}/avatar.jpeg"

        withContext(Dispatchers.IO) {
            userRepository.getUploadPhotoUrl(key).subscribe {
                uploadToS3(file, key, it.putURL)
            }
        }
    }

    private fun uploadToS3(file: File, key: String, url: String)  = viewModelScope.launch{
        withContext(Dispatchers.IO) {
            userRepository.uploadPhoto(file, url).subscribe(object : Observer<ResponseBody> {
                override fun onComplete() {
                    updateUserAvatar(key)
                }
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(i: ResponseBody) {}
                override fun onError(e: Throwable) {
                    println(e)
                }

            })
        }

    }

    private fun updateUserAvatar(key: String) = viewModelScope.launch {
        val newUser = currentUser
        newUser.avatar = key
        withContext(Dispatchers.IO) {
            userRepository.updateUser(newUser).subscribe {
                currentUser = it
                updateLocalUser(it)
                _isLoadingUpload.postValue(false)
            }
        }
    }

    private fun updateLocalUser(user: UserEntity) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            userRepository.updateLocalUser(user)
        }
    }

}