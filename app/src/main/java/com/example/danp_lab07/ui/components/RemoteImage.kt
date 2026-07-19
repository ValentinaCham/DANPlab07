package com.example.danp_lab07.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.danp_lab07.data.remote.StorageUrlResolver

/**
 * CompositionLocal that exposes the [StorageUrlResolver] to any composable in
 * the tree. Defaults to `null` so previews and tests can render without it —
 * in that case [RemoteImage] falls back to whatever [placeholder] is passed
 * (which itself defaults to `null`, so callers can opt into the placeholder
 * wrapper [RemoteImageOrPlaceholder]).
 *
 * Wiring is done in `MainActivity` via:
 *
 *     CompositionLocalProvider(LocalStorageUrlResolver provides resolver) {
 *         ProductAppNavigation()
 *     }
 */
val LocalStorageUrlResolver = staticCompositionLocalOf<StorageUrlResolver?> { null }

/**
 * Loads a Firebase Storage image by its *path* (not URL) and renders it with
 * Coil. The download URL is resolved lazily via the injected
 * [StorageUrlResolver]; while that promise is pending, or if it fails, the
 * provided [placeholder] is shown (pass `null` to suppress it).
 */
@Composable
fun RemoteImage(
    path: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    placeholder: Painter? = null,
) {
    val resolver = LocalStorageUrlResolver.current
    val url by produceState<String?>(initialValue = null, key1 = path) {
        value = resolver?.resolve(path)
    }
    AsyncImage(
        model = url,
        modifier = modifier,
        contentScale = contentScale,
        contentDescription = contentDescription,
        placeholder = placeholder,
        error = placeholder,
    )
}

/**
 * Convenience wrapper that always renders a broken-image placeholder
 * underneath the resolved thumbnail — useful for list rows where you want a
 * stable visual footprint even before the network resolves the URL.
 */
@Composable
fun RemoteImageOrPlaceholder(
    path: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.BrokenImage,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RemoteImage(
            path = path,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            contentDescription = contentDescription,
        )
    }
}
