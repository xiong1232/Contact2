package my.tarc.mycontact

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class ContactRepository(private val contactDao: ContactDao){
    //Room execute all queries on a separate thread
    val allContacts: LiveData<List<Contact>> = contactDao.getAllContact()

    @WorkerThread
    suspend fun deleteAll(){
        contactDao.deleteAll()
    }



    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun add(contact: Contact){
        contactDao.insert(contact)
    }

    @WorkerThread
    suspend fun delete(contact: Contact){
        contactDao.delete(contact)
    }

    @WorkerThread
    suspend fun update(contact: Contact){
        contactDao.update(contact)
    }
}