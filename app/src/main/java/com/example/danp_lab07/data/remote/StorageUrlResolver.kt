package com.example.danp_lab07.data.remote

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves Firebase Storage *paths* (e.g. `products/42/abc.jpg`) into the
 * publicly downloadable HTTPS URLs that Coil / AsyncImage expect.
 *
 * Why this exists as a separate class:
 *  - `imageUris` in our domain model is a list of Storage paths, NOT URLs.
 *  - Storing raw download URLs would break the moment the Firebase SDK
 *    rotates the underlying token; the canonical source of truth is the path.
 *  - `AsyncImage` cannot dereference a path on its own — Coil would try to
 *    fetch `https://products/42/abc.jpg` which is meaningless.
 *
 * All resolutions are suspend functions and swallow errors individually so the
 * UI can render a placeholder for the broken entries instead of crashing the
 * whole list.
 */
@Singleton
class StorageUrlResolver @Inject constructor(
    private val storage: FirebaseStorage,
) {
    /**
     * Resolves a single Storage path to a download URL. Returns `null` if the
     * path is blank, the object does not exist, or the network call fails.
     */
    suspend fun resolve(storagePath: String): String? {
        if (storagePath.isBlank()) return null
        return runCatching {
            storage.getReference(storagePath).downloadUrl.await().toString()
        }.getOrNull()
    }

    /**
     * Best-effort batch resolution. Paths that fail to resolve are silently
     * dropped from the resulting map; the UI is expected to render a
     * placeholder for any path that is not present in the returned map.
     */
    suspend fun resolveAll(paths: List<String>): Map<String, String> {
        if (paths.isEmpty()) return emptyMap()
        val out = LinkedHashMap<String, String>(paths.size)
        for (path in paths) {
            resolve(path)?.let { out[path] = it }
        }
        return out
    }
}
