package com.example.redsocial.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.redsocial.data.Publicacion
import com.example.redsocial.viewmodel.PublicacionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(viewModel: PublicacionViewModel) {
    val publicaciones by viewModel.publicaciones.collectAsState()
    var pestañaSeleccionada by remember { mutableStateOf(0) } // 0: Muro, 1: Formulario

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Red Social Local") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestañaSeleccionada == 0,
                    onClick = { pestañaSeleccionada = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Muro") },
                    label = { Text("Publicaciones") }
                )
                NavigationBarItem(
                    selected = pestañaSeleccionada == 1,
                    onClick = { pestañaSeleccionada = 1 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Nueva") },
                    label = { Text("Crear") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (pestañaSeleccionada == 0) {
                VistaListadoPublicaciones(
                    publicaciones = publicaciones,
                    onEliminar = { viewModel.eliminarPublicacion(it) },
                    onIrACrear = { pestañaSeleccionada = 1 }
                )
            } else {
                FormularioAltaPublicacion(
                    onGuardar = { titulo, descripcion, fotoUri, fecha ->
                        viewModel.agregarPublicacion(titulo, descripcion, fotoUri, fecha)
                        pestañaSeleccionada = 0
                    }
                )
            }
        }
    }
}

@Composable
fun FormularioAltaPublicacion(
    onGuardar: (titulo: String, descripcion: String, fotoUri: String?, fecha: String) -> Unit
) {
    val context = LocalContext.current
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    var fecha by remember { mutableStateOf(obtenerFechaActual()) }

    // Launcher para seleccionar imagen de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoUri = uri
        }
    }

    // Launcher para tomar foto con la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito && tempCameraUri != null) {
            fotoUri = tempCameraUri
        }
    }

    // Launcher para solicitar permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val uri = crearArchivoTemporalUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nueva Publicación",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // Título
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Descripción
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        // Componente para visualizar la Fecha
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fecha: $fecha",
                    fontSize = 14.sp
                )
                TextButton(onClick = { fecha = obtenerFechaActual() }) {
                    Text("Actualizar")
                }
            }
        }

        // Vista previa de la fotografía
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEEEEEE))
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (fotoUri != null) {
                AsyncImage(
                    model = fotoUri,
                    contentDescription = "Foto seleccionada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "Sin fotografía seleccionada",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        // Botones de Hardware (Cámara y Galería)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val tienePermiso = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (tienePermiso) {
                        val uri = crearArchivoTemporalUri(context)
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cámara")
            }

            Button(
                onClick = {
                    galleryLauncher.launch("image/*")
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Galería")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón Guardar
        Button(
            onClick = {
                if (titulo.isBlank() || descripcion.isBlank()) {
                    Toast.makeText(context, "Por favor llena título y descripción", Toast.LENGTH_SHORT).show()
                } else {
                    onGuardar(titulo, descripcion, fotoUri?.toString(), fecha)
                    Toast.makeText(context, "Publicación guardada", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Publicación")
        }
    }
}

@Composable
fun VistaListadoPublicaciones(
    publicaciones: List<Publicacion>,
    onEliminar: (Publicacion) -> Unit,
    onIrACrear: () -> Unit
) {
    if (publicaciones.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No hay publicaciones aún",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onIrACrear) {
                    Text("Crear primera publicación")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(publicaciones, key = { it.id }) { item ->
                TarjetaPublicacion(publicacion = item, onEliminar = { onEliminar(item) })
            }
        }
    }
}

@Composable
fun TarjetaPublicacion(
    publicacion: Publicacion,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!publicacion.fotoUri.isNullOrEmpty()) {
                AsyncImage(
                    model = publicacion.fotoUri,
                    contentDescription = publicacion.titulo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = publicacion.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onEliminar) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Gray
                        )
                    }
                }

                Text(
                    text = publicacion.fecha,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = publicacion.descripcion,
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun crearArchivoTemporalUri(context: Context): Uri {
    val tempFile = File.createTempFile(
        "foto_${System.currentTimeMillis()}",
        ".jpg",
        context.cacheDir
    ).apply {
        createNewFile()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
