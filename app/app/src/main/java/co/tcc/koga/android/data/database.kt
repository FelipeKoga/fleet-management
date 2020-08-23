package co.tcc.koga.android.data

import co.tcc.koga.android.domain.Contact
import co.tcc.koga.android.domain.ContactStatus
import co.tcc.koga.android.domain.Message

object Database {
    fun getContacts(): List<Contact> {
        return listOf(
            Contact("Usuário 1", ContactStatus.ONLINE, photo = "https://s2.glbimg.com/p9m8kLcKAts7ooSJ72r9wu9uuBE=/940x523/e.glbimg.com/og/ed/f/original/2018/08/08/real_grumpy_cat.jpg"),
            Contact("Usuário 2", ContactStatus.ONLINE, photo = "https://www.osprofanos.com/wp-content/uploads/2013/03/cat8.jpg"),
            Contact("Usuário 3", ContactStatus.OCCUPIED, photo = "https://petgusto.com/wp-content/uploads/2017/04/10-perfis-de-gatos-para-seguir-no-instagram-3.jpg"),
            Contact("Usuário 4", ContactStatus.OFFLINE, photo = "https://lh3.googleusercontent.com/proxy/mbzPNa2AyxCD593NPN5Wmt25HcwVcs8XdJ5WpfSraDoNg5tpfGCiobBNeFiL30EAsRjjx9RosFj0HPquz3dcp6SzKcPZK-4gLwg5837rpF3hZR4lLLtHbw"),
            Contact("Usuário 5", ContactStatus.OFFLINE, photo = "https://www.osprofanos.com/wp-content/uploads/2013/03/cat37.jpg"),
            Contact("Grupo 1", ContactStatus.NONE, isGroup = true),
            Contact("Grupo 2", ContactStatus.NONE, isGroup = true),
            Contact("Grupo 3", ContactStatus.NONE, isGroup = true),
            Contact("Grupo 4", ContactStatus.NONE, isGroup = true),
            Contact("Grupo 5", ContactStatus.NONE, isGroup = true)
        ).shuffled()
    }

    fun getContactsOnly(): List<Contact> {
        return listOf(
            Contact("Usuário 1", ContactStatus.ONLINE, photo = "https://s2.glbimg.com/p9m8kLcKAts7ooSJ72r9wu9uuBE=/940x523/e.glbimg.com/og/ed/f/original/2018/08/08/real_grumpy_cat.jpg"),
            Contact("Usuário 2", ContactStatus.ONLINE, photo = "https://www.osprofanos.com/wp-content/uploads/2013/03/cat8.jpg"),
            Contact("Usuário 3", ContactStatus.OCCUPIED, photo = "https://petgusto.com/wp-content/uploads/2017/04/10-perfis-de-gatos-para-seguir-no-instagram-3.jpg"),
            Contact("Usuário 4", ContactStatus.OFFLINE, photo = "https://lh3.googleusercontent.com/proxy/mbzPNa2AyxCD593NPN5Wmt25HcwVcs8XdJ5WpfSraDoNg5tpfGCiobBNeFiL30EAsRjjx9RosFj0HPquz3dcp6SzKcPZK-4gLwg5837rpF3hZR4lLLtHbw"),
            Contact("Usuário 5", ContactStatus.OFFLINE, photo = "https://www.osprofanos.com/wp-content/uploads/2013/03/cat37.jpg")
        )
    }

    fun getMessages(): List<Message> {
        return listOf(
            Message(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam a volutpat ipsum, non molestie ante. Ut tincidunt enim tempus, sagittis sem sit amet, ullamcorper nisi. Suspendisse eget dui non velit ullamcorper feugiat. Nulla facilisi. Phasellus fermentum nibh et dolor commodo placerat. Etiam at elit ac risus hendrerit viverra sit amet ut purus. Cras vitae nisl dignissim, sollicitudin tortor et, facilisis mauris. Ut rhoncus, nibh id dignissim ornare, eros quam vulputate leo, dignissim feugiat lorem lacus vitae urna.",
                Contact("Felipe", ContactStatus.ONLINE, photo = "https://scontent.fpgz1-1.fna.fbcdn.net/v/t31.0-8/23509456_1409309355854887_1466911775224434071_o.jpg?_nc_cat=108&_nc_sid=09cbfe&_nc_eui2=AeG623DjsuyKytHojdUetGk7pVh_hLQD8-ulWH-EtAPz68zvBVOrDuTeDTDkcwcMKguceUm-oHC3hNPbdhEQuVZK&_nc_ohc=inhZTXaPe04AX-lM6La&_nc_ht=scontent.fpgz1-1.fna&oh=3c51f2abbda394c3c8165b3abe21e0c3&oe=5F2B1497"),

                Contact("Usuário 5", ContactStatus.OFFLINE, photo = "https://www.osprofanos.com/wp-content/uploads/2013/03/cat37.jpg"),
                false,
                received = true,
                date = "17:40"
            ),
            Message(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam a volutpat ipsum, non molestie ante. Ut tincidunt enim tempus, sagittis sem sit amet, ullamcorper nisi. Suspendisse eget dui non velit ullamcorper feugiat. Nulla facilisi. Phasellus fermentum nibh et dolor commodo placerat. Etiam at elit ac risus hendrerit viverra sit amet ut purus. Cras vitae nisl dignissim, sollicitudin tortor et, facilisis mauris. Ut rhoncus, nibh id dignissim ornare, eros quam vulputate leo, dignissim feugiat lorem lacus vitae urna.",
                Contact("Felipe", ContactStatus.ONLINE, photo = "https://scontent.fpgz1-1.fna.fbcdn.net/v/t31.0-8/23509456_1409309355854887_1466911775224434071_o.jpg?_nc_cat=108&_nc_sid=09cbfe&_nc_eui2=AeG623DjsuyKytHojdUetGk7pVh_hLQD8-ulWH-EtAPz68zvBVOrDuTeDTDkcwcMKguceUm-oHC3hNPbdhEQuVZK&_nc_ohc=inhZTXaPe04AX-lM6La&_nc_ht=scontent.fpgz1-1.fna&oh=3c51f2abbda394c3c8165b3abe21e0c3&oe=5F2B1497"),
                Contact("Usuário 5", ContactStatus.OFFLINE, photo = "https://www.osprofanos.com/wp-content/uploads/2013/03/cat37.jpg"),
                false,
                received = false,
                date = "17:40"
            ),
            Message(
                "Olá 3!",
                Contact("Felipe", ContactStatus.ONLINE, photo = "https://scontent.fpgz1-1.fna.fbcdn.net/v/t31.0-8/23509456_1409309355854887_1466911775224434071_o.jpg?_nc_cat=108&_nc_sid=09cbfe&_nc_eui2=AeG623DjsuyKytHojdUetGk7pVh_hLQD8-ulWH-EtAPz68zvBVOrDuTeDTDkcwcMKguceUm-oHC3hNPbdhEQuVZK&_nc_ohc=inhZTXaPe04AX-lM6La&_nc_ht=scontent.fpgz1-1.fna&oh=3c51f2abbda394c3c8165b3abe21e0c3&oe=5F2B1497"),
                Contact("Usuário 5", ContactStatus.OFFLINE, photo = "https://www.osprofanos.com/wp-content/uploads/2013/03/cat37.jpg"),
                true,
                received = true,
                date = "17:40"
            ),
            Message(
                "Olá 4!",
                Contact("Felipe", ContactStatus.ONLINE, photo = "https://scontent.fpgz1-1.fna.fbcdn.net/v/t31.0-8/23509456_1409309355854887_1466911775224434071_o.jpg?_nc_cat=108&_nc_sid=09cbfe&_nc_eui2=AeG623DjsuyKytHojdUetGk7pVh_hLQD8-ulWH-EtAPz68zvBVOrDuTeDTDkcwcMKguceUm-oHC3hNPbdhEQuVZK&_nc_ohc=inhZTXaPe04AX-lM6La&_nc_ht=scontent.fpgz1-1.fna&oh=3c51f2abbda394c3c8165b3abe21e0c3&oe=5F2B1497"),
                Contact("Usuário 5", ContactStatus.OFFLINE, photo = "https://www.osprofanos.com/wp-content/uploads/2013/03/cat37.jpg"),
                true,
                received = false,
                date = "17:40"
            )
        )
    }


}