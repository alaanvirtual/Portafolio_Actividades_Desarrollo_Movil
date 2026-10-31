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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
    var pestañaSeleccionada by remember { mutableStateOf(0) } // 0: Publicaciones, 1: Formulario
    var publicacionAEditar by remember { mutableStateOf<Publicacion?>(null) }

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
                    onClick = {
                        pestañaSeleccionada = 0
                        publicacionAEditar = null
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = "Publicaciones") },
                    label = { Text("Publicaciones") }
                )
                NavigationBarItem(
                    selected = pestañaSeleccionada == 1,
                    onClick = {
                        pestañaSeleccionada = 1
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
                    label = { Text(if (publicacionAEditar != null) "Editar" else "Crear") }
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
                    onEditar = { pub ->
                        publicacionAEditar = pub
                        pestañaSeleccionada = 1
                    },
                    onEliminar = { viewModel.eliminarPublicacion(it) },
                    onIrACrear = {
                        publicacionAEditar = null
                        pestañaSeleccionada = 1
                    }
                )
            } else {
                FormularioAltaPublicacion(
                    publicacionAEditar = publicacionAEditar,
                    onGuardar = { titulo, descripcion, fotoUri, fecha ->
                        val actual = publicacionAEditar
                        if (actual != null) {
                            // Modificar publicación existente
                            viewModel.actualizarPublicacion(
                                actual.copy(
                                    titulo = titulo,
                                    descripcion = descripcion,
                                    fotoUri = fotoUri,
                                    fecha = fecha
                                )
                            )
                        } else {
                            // Crear nueva publicación
                            viewModel.agregarPublicacion(titulo, descripcion, fotoUri, fecha)
                        }
                        publicacionAEditar = null
                        pestañaSeleccionada = 0
                    },
                    onCancelar = {
                        publicacionAEditar = null
                        pestañaSeleccionada = 0
                    }
                )
            }
        }
    }
}

@Composable
fun FormularioAltaPublicacion(
    publicacionAEditar: Publicacion?,
    onGuardar: (titulo: String, descripcion: String, fotoUri: String?, fecha: String) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current

    fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    var titulo by remember(publicacionAEditar) { mutableStateOf(publicacionAEditar?.titulo ?: "") }
    var descripcion by remember(publicacionAEditar) { mutableStateOf(publicacionAEditar?.descripcion ?: "") }
    var fecha by remember(publicacionAEditar) { mutableStateOf(publicacionAEditar?.fecha ?: obtenerFechaActual()) }
    var fotoUri by remember(publicacionAEditar) {
        mutableStateOf(publicacionAEditar?.fotoUri?.let { Uri.parse(it) })
    }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

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

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (publicacionAEditar != null) "Modificar Publicación" else "Nueva Publicación",
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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fecha: $fecha",
                    fontSize = 13.sp
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
                .height(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEEEEEE))
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (fotoUri != null) {
                AsyncImage(
                    model = fotoUri,
                    contentDescription = "Foto de la publicación",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "Sin fotografía seleccionada",
                    color = Color.Gray,
                    fontSize = 13.sp
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

        // Botón Guardar / Modificar
        Button(
            onClick = {
                if (titulo.isBlank() || descripcion.isBlank()) {
                    Toast.makeText(context, "Por favor llena título y descripción", Toast.LENGTH_SHORT).show()
                } else {
                    onGuardar(titulo, descripcion, fotoUri?.toString(), fecha)
                    val mensaje = if (publicacionAEditar != null) "Publicación modificada" else "Publicación guardada"
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (publicacionAEditar != null) "Guardar Cambios" else "Guardar Publicación")
        }

        // Botón Cancelar (si está editando)
        if (publicacionAEditar != null) {
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar Edición")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun VistaListadoPublicaciones(
    publicaciones: List<Publicacion>,
    onEditar: (Publicacion) -> Unit,
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
                TarjetaPublicacion(
                    publicacion = item,
                    onEditar = { onEditar(item) },
                    onEliminar = { onEliminar(item) }
                )
            }
        }
    }
}

@Composable
fun TarjetaPublicacion(
    publicacion: Publicacion,
    onEditar: () -> Unit,
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
                        .height(190.dp),
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
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Row {
                        IconButton(onClick = onEditar) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modificar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onEliminar) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
