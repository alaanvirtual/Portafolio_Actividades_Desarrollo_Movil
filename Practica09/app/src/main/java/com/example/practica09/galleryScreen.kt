package com.example.practica09

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun GaleriaScreen() {
    // Lista de URLs de imágenes de prueba
    val imagenes = listOf(
        "https://picsum.photos/id/1018/400/400",
        "https://picsum.photos/id/1021/400/400",
        "https://picsum.photos/id/1022/400/400",
        "https://picsum.photos/id/1023/400/400",
        "https://picsum.photos/id/1024/400/400",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQusvRy3TUSUeRU9XvVEqUq3FrlDi-qjqsiNLyRBKNKIosd1WtO0yfJ33jG&s=10",
        "https://m.media-amazon.com/images/I/51Z9wKv9cgL._UXNaN_FMjpg_QL85_.jpg",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTqknttjjLUW_vfuctBGLSy-o3HB-2ZMSykA-GK_0iMEw&s",
        "https://img.itch.zone/aW1nLzI1NTUzNDg1LmpwZWc=/original/ohOfEx.jpeg",
        "https://i.scdn.co/image/ab67616d00001e026ed39fc4ac85aa4ce6ea1793",
        "https://www.shutterstock.com/image-vector/number-67-hands-gesture-vector-600w-2731415297.jpg"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .padding(top = 32.dp)
    ) {
        Text(
            text = "Mi Galería",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )

        // LazyVerticalGrid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Indicamos que queremos 2 columnas
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(imagenes) { url ->
                ImagenItem(url = url)
            }
        }
    }
}

@Composable
fun ImagenItem(url: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        // AsyncImage de Coil maneja la carga de red y el ciclo de vida
        AsyncImage(
            model = url,
            contentDescription = "Imagen de la galería",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
