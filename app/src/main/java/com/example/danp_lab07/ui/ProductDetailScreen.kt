package com.example.danp_lab07.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.danp_lab07.data.Product
import com.example.danp_lab07.ui.components.RemoteImage
import com.example.danp_lab07.viewmodel.ProductUiState
import com.example.danp_lab07.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    viewModel: ProductViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val product = (uiState as? ProductUiState.Success)
        ?.products?.find { it.id == productId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEdit(productId) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
        }
    ) { paddingValues ->
        if (product != null) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                ProductImagesCarousel(imageUris = product.imageUris)

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = product.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "S/. ${product.price}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Categoría: ${product.category}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = product.description, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Text(text = "Producto no encontrado", modifier = Modifier.padding(paddingValues))
        }
    }
}

/**
 * Horizontal LazyRow rendering up to [Product.MAX_IMAGES] thumbnails. Falls
 * back to a single "Sin imágenes" placeholder when the list is empty.
 */
@Composable
private fun ProductImagesCarousel(imageUris: List<String>) {
    val maxImages = Product.MAX_IMAGES
    val visible = imageUris.take(maxImages)

    if (visible.isEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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
                "Sin imágenes",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        itemsIndexed(items = visible, key = { idx, _ -> idx }) { _, path ->
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                RemoteImage(
                    path = path,
                    contentDescription = "Imagen del producto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
