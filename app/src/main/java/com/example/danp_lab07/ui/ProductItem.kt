package com.example.danp_lab07.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.danp_lab07.data.Product
import com.example.danp_lab07.ui.components.RemoteImage

@Composable
fun ProductItem(product: Product, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val firstImage = product.imageUris.firstOrNull()
            if (firstImage != null) {
                RemoteImage(
                    path = firstImage,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                // No images attached — keep visual weight balanced with a
                // placeholder slot so the row still looks like a row.
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleLarge)
                Text(text = "S/. ${product.price}", color = Color.Gray)
                Text(text = "Categoría: ${product.category}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}
