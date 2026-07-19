package com.example.danp_lab07.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseImageStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage,
) : ImageStorageDataSource {

    override suspend fun uploadImage(localUri: Uri, productId: Int, imageIndex: Int): String {
        val path = pathFor(productId, imageIndex)
        val ref: StorageReference = storage.reference.child(path)
        ref.putFile(localUri).await()
        return path
    }

    override suspend fun deleteImage(storagePath: String): Boolean {
        if (storagePath.isBlank()) return false
        return try {
            storage.reference.child(storagePath).delete().await()
            true
        } catch (e: Exception) {
            // File not found → treat as success; otherwise rethrow to caller.
            if (e.message?.contains("not found", ignoreCase = true) == true) false
            else throw e
        }
    }

    override suspend fun deleteAllProductImages(productId: Int): Boolean {
        val prefix = "products/$productId/"
        val refs = storage.reference.listAll().await().items
        val ours = refs.filter { it.path.startsWith(prefix) }
        if (ours.isEmpty()) return false
        ours.forEach { it.delete().await() }
        return true
    }

    private fun pathFor(productId: Int, imageIndex: Int): String =
        "products/$productId/${System.currentTimeMillis()}_${imageIndex}_${UUID.randomUUID().toString().take(6)}.jpg"

    companion object {
        /** Default Storage bucket suffix appended by the Firebase SDK when the
         *  client omits a `gs://` URL. Kept here for documentation purposes. */
        const val DEFAULT_BUCKET_SUFFIX = ".appspot.com"
    }
}
