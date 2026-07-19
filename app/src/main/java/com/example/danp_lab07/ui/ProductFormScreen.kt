package com.example.danp_lab07.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.danp_lab07.data.Product
import com.example.danp_lab07.domain.ProductValidator
import com.example.danp_lab07.ui.components.RemoteImage
import com.example.danp_lab07.viewmodel.ImageUploadState
import com.example.danp_lab07.viewmodel.ProductUiState
import com.example.danp_lab07.viewmodel.ProductViewModel

/**
 * Local-only UI state for the image picker. We keep this separate from
 * anything that gets persisted; the final mapping to `PendingImage` happens
 * at save time.
 */
private sealed interface ImageSlot {
    data class Stored(val storagePath: String) : ImageSlot
    data class Picked(val uri: Uri) : ImageSlot
    object Empty : ImageSlot
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: Int?,
    viewModel: ProductViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }

    // Each slot is either a previously uploaded Storage path or a freshly
    // picked local Uri. Empty tail slots are ignored.
    var imageSlots by remember { mutableStateOf<List<ImageSlot>>(emptyList()) }
    val initialLoadDone = remember(productId) { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    // Load existing product data once.
    LaunchedEffect(productId, uiState) {
        if (!initialLoadDone.value && productId != null) {
            val product = (uiState as? ProductUiState.Success)
                ?.products?.find { it.id == productId }
            if (product != null) {
                name = product.name
                price = product.price.toString()
                description = product.description
                category = product.category
                imageSlots = product.imageUris.map { ImageSlot.Stored(it) }
                initialLoadDone.value = true
            }
        }
    }

    // Connectivity gate: images can only be picked while online. We resolve
    // this on entry and on lifecycle resume so the user can re-enable the
    // button as soon as the network comes back.
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(checkConnectivity(context)) }
    DisposableEffect(Unit) {
        val receiver = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                isOnline = true
            }
            override fun onLost(network: android.net.Network) {
                isOnline = false
            }
        }

        // Best-effort: we don't fail if the platform refuses the callback
        // (it never does on a real device); we still rely on the initial
        // snapshot above.
        runCatching {
            val cm = context.getSystemService(android.net.ConnectivityManager::class.java)
            cm?.registerDefaultNetworkCallback(receiver)
        }

        onDispose {
            runCatching {
                context.getSystemService(android.net.ConnectivityManager::class.java)
                    ?.unregisterNetworkCallback(receiver)
            }
        }
    }

    // Gallery picker — only fires while under MAX_IMAGES AND while online.
    val remainingSlots = Product.MAX_IMAGES - imageSlots.size
    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            if (uris.isEmpty()) return@rememberLauncherForActivityResult
            if (!isOnline) return@rememberLauncherForActivityResult
            val accepted = uris.take(remainingSlots.coerceAtLeast(0))
            imageSlots = imageSlots + accepted.map { ImageSlot.Picked(it) }
        },
    )

    // Validation summary shown only after the user attempts to save.
    var attemptedSave by rememberSaveable { mutableStateOf(false) }
    val validation = ProductValidator.validate(
        name = name,
        price = price,
        description = description,
        category = category,
        imageCount = imageSlots.size,
    )
    val showError = attemptedSave && !validation.isValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == null) "Añadir Producto" else "Editar Producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                isError = showError && validation.nameError != null,
                supportingText = {
                    if (showError) validation.nameError?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio") },
                isError = showError && validation.priceError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                isError = showError && validation.descriptionError != null,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría") },
                isError = showError && validation.categoryError != null,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ---------- Image picker section ----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Imágenes (${imageSlots.size}/${Product.MAX_IMAGES})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { pickImages.launch("image/*") },
                    enabled = isOnline && imageSlots.size < Product.MAX_IMAGES,
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Añadir")
                }
            }
            if (!isOnline) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Sin conexión: no se pueden agregar imágenes nuevas. " +
                            "El resto del producto sí puede guardarse; " +
                            "las imágenes se subirán cuando vuelva la red.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (showError && validation.imagesError != null) {
                Text(
                    validation.imagesError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (imageSlots.isEmpty()) {
                EmptyImagesPlaceholder(isOnline = isOnline)
            } else {
                ImagePreviewRow(
                    slots = imageSlots,
                    onRemove = { index ->
                        imageSlots = imageSlots.toMutableList().also {
                            it.removeAt(index)
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button — observes upload state to disable while in flight.
            val isUploading = uploadState is ImageUploadState.Uploading
            // Block save if there are pending local URIs to upload but no
            // network — Storage would hang/fail and confuse the user.
            val hasPendingUploads = imageSlots.any { it is ImageSlot.Picked }
            val saveBlockedByOffline = hasPendingUploads && !isOnline
            Button(
                onClick = {
                    attemptedSave = true
                    if (!validation.isValid) return@Button
                    if (saveBlockedByOffline) return@Button
                    val storedPaths = imageSlots.filterIsInstance<ImageSlot.Stored>()
                        .map { it.storagePath }
                    val pickedUris = imageSlots.filterIsInstance<ImageSlot.Picked>()
                        .map { it.uri }
                    // Anything that *was* stored but is no longer in slots counts as removed.
                    val originalStored = (uiState as? ProductUiState.Success)
                        ?.products?.find { it.id == productId }?.imageUris.orEmpty()
                    val removedPaths = originalStored - storedPaths.toSet()
                    viewModel.saveProduct(
                        product = Product(
                            id = productId ?: 0,
                            name = name,
                            price = validation.parsedPrice ?: 0.0,
                            description = description,
                            category = category,
                            imageUris = storedPaths,
                        ),
                        existingStoragePaths = storedPaths,
                        pendingUris = pickedUris,
                        removedPaths = removedPaths,
                    )
                    onNavigateBack()
                },
                enabled = !isUploading && !saveBlockedByOffline,
                modifier = Modifier.fillMaxWidth(),
            ) {
                when (val s = uploadState) {
                    is ImageUploadState.Uploading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Subiendo ${s.remaining}…")
                    }
                    else -> Text(if (productId == null) "Guardar" else "Actualizar")
                }
            }
            if (saveBlockedByOffline) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tienes imágenes locales pendientes. Conéctate a la red para subirlas antes de guardar.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // Surface upload errors non-blockingly.
            if (uploadState is ImageUploadState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(
                    (uploadState as ImageUploadState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                )
                // Reset so we don't keep showing the same message forever.
                LaunchedEffect(Unit) { viewModel.consumeUploadState() }
            }
        }
    }
}

@Composable
private fun ImagePreviewRow(slots: List<ImageSlot>, onRemove: (Int) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        itemsIndexed(items = slots, key = { idx, _ -> idx }) { idx, slot ->
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                when (slot) {
                    is ImageSlot.Picked -> AsyncImage(
                        model = slot.uri,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    is ImageSlot.Stored -> RemoteImage(
                        path = slot.storagePath,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    ImageSlot.Empty -> Unit
                }
                IconButton(
                    onClick = { onRemove(idx) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Quitar imagen",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyImagesPlaceholder(isOnline: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (isOnline) {
                "Sin imágenes. Pulsa \"Añadir\" para elegir hasta ${Product.MAX_IMAGES}."
            } else {
                "Sin conexión: no puedes agregar imágenes ahora."
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * One-shot connectivity snapshot used as the initial value of [isOnline].
 * Returns `true` if we cannot determine (so we err on the side of letting the
 * user try — `pickImages` will fail gracefully through Coil if anything is
 * actually offline).
 */
private fun checkConnectivity(context: android.content.Context): Boolean {
    val cm = context.getSystemService(android.net.ConnectivityManager::class.java)
        ?: return true
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
