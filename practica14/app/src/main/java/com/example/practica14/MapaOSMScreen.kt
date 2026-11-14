package com.example.practica14

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.practica14.data.AppDatabase
import com.example.practica14.data.MarcadorEntity
import com.example.practica14.util.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaOSMScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val marcadorDao = remember { database.marcadorDao() }

    // Flujo reactivo (Flow<List<MarcadorEntity>>) desde Room a Compose
    val marcadores by marcadorDao.obtenerTodos().collectAsState(initial = emptyList())

    var permissionGranted by remember { mutableStateOf(false) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var locationOverlayInstance by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    // Estado del diálogo de nuevo marcador tras Long Press
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var markerTitleInput by remember { mutableStateOf("") }

    // Estado del modal sheet con la lista de marcadores guardados
    var showListSheet by remember { mutableStateOf(false) }

    // Fuente de mapa actual (OpenTopo / MAPNIK)
    var useOpenTopo by remember { mutableStateOf(true) }

    // Solicitar permiso de GPS
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            Toast.makeText(context, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_LONG).show()
        }
    }

    // Verificar el permiso al iniciar la aplicación
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted = true
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón para alternar capa del mapa (OpenTopo / MAPNIK)
                FloatingActionButton(
                    onClick = {
                        useOpenTopo = !useOpenTopo
                        mapViewInstance?.let { map ->
                            val source = if (useOpenTopo) TileSourceFactory.OpenTopo else TileSourceFactory.MAPNIK
                            map.setTileSource(source)
                            map.tileProvider.clearTileCache()
                            map.invalidate()
                        }
                        val name = if (useOpenTopo) "OpenTopo" else "Mapnik (OSM)"
                        Toast.makeText(context, "Mapa: $name", Toast.LENGTH_SHORT).show()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Filled.Layers, contentDescription = "Cambiar fuente de mapa")
                }

                // Botón para ver lista de marcadores guardados
                BadgedBox(
                    badge = {
                        if (marcadores.isNotEmpty()) {
                            Badge { Text("${marcadores.size}") }
                        }
                    }
                ) {
                    FloatingActionButton(
                        onClick = { showListSheet = true },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Icon(Icons.Filled.List, contentDescription = "Lista de marcadores")
                    }
                }

                // Botón centrar en ubicación GPS
                FloatingActionButton(
                    onClick = {
                        val location = locationOverlayInstance?.myLocation
                        if (location != null && mapViewInstance != null) {
                            mapViewInstance?.controller?.animateTo(location)
                            mapViewInstance?.controller?.setZoom(17.5)
                        } else {
                            Toast.makeText(context, "Esperando señal de GPS...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "Mi ubicación")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // AndroidView que aloja el MapView de OSMdroid
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(if (useOpenTopo) TileSourceFactory.OpenTopo else TileSourceFactory.MAPNIK)
                        tileProvider.clearTileCache()
                        setMultiTouchControls(true)
                        controller.setZoom(17.0)
                        controller.setCenter(GeoPoint(25.7905, -108.9875)) // Los Mochis, Sinaloa

                        // --------------------------------------------------------------------
                        // RETO FASE 2: DETECCIÓN DE TOQUE PROLONGADO (LONG PRESS)
                        // --------------------------------------------------------------------
                        val receiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false

                            override fun longPressHelper(p: GeoPoint?): Boolean {
                                p?.let { point ->
                                    pendingPoint = point
                                    markerTitleInput = "Marcador #${marcadores.size + 1}"
                                    showAddDialog = true
                                }
                                return true
                            }
                        }
                        overlays.add(0, MapEventsOverlay(receiver))

                        mapViewInstance = this
                    }
                },
                update = { mapView ->
                    mapViewInstance = mapView

                    // --------------------------------------------------------------------
                    // CONFIGURACIÓN DE GPS Y RETO FASE 1: PERSONALIZACIÓN DEL ÍCONO GPS EN ROJO
                    // --------------------------------------------------------------------
                    if (permissionGranted && mapView.overlays.none { it is MyLocationNewOverlay }) {
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
                        locationOverlay.enableMyLocation()
                        locationOverlay.enableFollowLocation()

                        // Pista técnica Reto Fase 1:
                        // Reemplazar ícono blanco por recurso vectorial rojo convertido a Bitmap
                        try {
                            val personRedBitmap = BitmapUtils.vectorToBitmap(
                                context,
                                R.drawable.ic_person_red,
                                widthPx = 72,
                                heightPx = 72
                            )
                            val arrowRedBitmap = BitmapUtils.vectorToBitmap(
                                context,
                                R.drawable.ic_direction_arrow_red,
                                widthPx = 72,
                                heightPx = 72
                            )
                            locationOverlay.setPersonIcon(personRedBitmap)
                            locationOverlay.setDirectionArrow(personRedBitmap, arrowRedBitmap)
                            locationOverlay.setPersonAnchor(0.5f, 0.5f)
                            locationOverlay.setDirectionAnchor(0.5f, 0.5f)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // Obliga a la cámara a viajar a la ubicación en el primer impacto de señal
                        locationOverlay.runOnFirstFix {
                            mapView.post {
                                val currentLocation = locationOverlay.myLocation
                                if (currentLocation != null) {
                                    mapView.controller.setZoom(17.0)
                                    mapView.controller.setCenter(currentLocation)
                                }
                            }
                        }

                        locationOverlayInstance = locationOverlay
                        mapView.overlays.add(locationOverlay)
                        mapView.invalidate()
                    }

                    // --------------------------------------------------------------------
                    // RETO FASE 2: RENDERIZADO REACTIVO DE MARCADORES (ROOM -> COMPOSE -> MAPA)
                    // --------------------------------------------------------------------
                    // Eliminar marcadores anteriores para sincronizar con la lista reactiva de Room
                    val existingMarkers = mapView.overlays.filterIsInstance<Marker>()
                    mapView.overlays.removeAll(existingMarkers)

                    // Agregar marcadores persistidos desde Room
                    marcadores.forEach { marcador ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(marcador.latitud, marcador.longitud)
                            title = marcador.titulo
                            snippet = "Lat: %.5f, Lng: %.5f".format(marcador.latitud, marcador.longitud)
                            icon = ContextCompat.getDrawable(context, R.drawable.ic_marker_red)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { m, _ ->
                                m.showInfoWindow()
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }

                    mapView.invalidate()
                }
            )

            // Banner superior de ayuda informativa
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Place,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Práctica 14 - GPS & OSMdroid",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Mantén presionado (Long Press) para agregar marcador en Room",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // DIÁLOGO PARA CAPTURAR Y GUARDAR EL MARCADOR EN ROOM TRAS LONG PRESS
    // ------------------------------------------------------------------------
    if (showAddDialog && pendingPoint != null) {
        val pt = pendingPoint!!
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                pendingPoint = null
            },
            icon = {
                Icon(Icons.Filled.Place, contentDescription = null, tint = Color.Red)
            },
            title = {
                Text("Guardar Marcador en Room")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Coordenadas capturadas:\nLat: ${pt.latitude.format(6)}\nLng: ${pt.longitude.format(6)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = markerTitleInput,
                        onValueChange = { markerTitleInput = it },
                        label = { Text("Título descriptivo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = if (markerTitleInput.isNotBlank()) {
                            markerTitleInput.trim()
                        } else {
                            "Punto (${pt.latitude.format(4)}, ${pt.longitude.format(4)})"
                        }

                        coroutineScope.launch {
                            val entity = MarcadorEntity(
                                latitud = pt.latitude,
                                longitud = pt.longitude,
                                titulo = title
                            )
                            withContext(Dispatchers.IO) {
                                marcadorDao.insertar(entity)
                            }
                            Toast.makeText(
                                context,
                                "Marcador \"$title\" persistido en SQLite",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        showAddDialog = false
                        pendingPoint = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        pendingPoint = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ------------------------------------------------------------------------
    // MODAL BOTTOM SHEET: LISTA Y GESTIÓN DE MARCADORES GUARDADOS EN ROOM
    // ------------------------------------------------------------------------
    if (showListSheet) {
        ModalBottomSheet(
            onDismissRequest = { showListSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Marcadores en Room (${marcadores.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (marcadores.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    marcadorDao.eliminarTodos()
                                }
                                Toast.makeText(context, "Marcadores eliminados", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Borrar todos", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (marcadores.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay marcadores aún.\nMantén presionado cualquier punto del mapa.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(marcadores, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                onClick = {
                                    // Viajar al marcador seleccionado
                                    mapViewInstance?.controller?.animateTo(GeoPoint(item.latitud, item.longitud))
                                    mapViewInstance?.controller?.setZoom(18.0)
                                    showListSheet = false
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.titulo,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Lat: ${item.latitud.format(5)}, Lng: ${item.longitud.format(5)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                marcadorDao.eliminar(item)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extensión para formatear números decimales
private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
