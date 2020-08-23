package co.tcc.koga.android.data.network

import co.tcc.koga.android.domain.Contact
import retrofit2.http.GET

interface ContactsService {
    @GET("/user")
    suspend fun getContacts(): MutableList<Contact>
}

val contactsService: ContactsService =
    NetworkUtils.getRetrofitInstance().create(ContactsService::class.java)