package com.example.practica17.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.practica17.data.Tarea

// COMPONENTE CON ESTADO (Stateful)
@Composable
fun TareasScreen(
    viewModel: TareasViewModel = viewModel()
) {
    // Observamos el StateFlow
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TareasContenido(
        uiState = uiState,
        onTextoCambio = viewModel::onTextoCambio,
        onAgregarTarea = viewModel::agregarTarea,
        onAlternarCompletada = viewModel::alternarCompletada,
        onEliminarTarea = viewModel::eliminarTarea,
        onCambiarFiltro = viewModel::cambiarFiltro,
        onRecargar = { viewModel.cargarTareas(simularError = false) },
        onSimularError = { viewModel.cargarTareas(simularError = true) },
        onLimpiarCompletadas = viewModel::limpiarCompletadas
    )
}

// COMPONENTE SIN ESTADO (Stateless)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasContenido(
    uiState: TareasUiState,
    onTextoCambio: (String) -> Unit,
    onAgregarTarea: () -> Unit,
    onAlternarCompletada: (Int) -> Unit,
    onEliminarTarea: (Int) -> Unit,
    onCambiarFiltro: (FiltroTarea) -> Unit,
    onRecargar: () -> Unit,
    onSimularError: () -> Unit,
    onLimpiarCompletadas: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MVVM - Gestor de Tareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Botón para probar la transición al estado de Error
                    IconButton(onClick = onSimularError) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Simular error de carga"
                        )
                    }
                    // Botón para recargar los datos iniciales
                    IconButton(onClick = onRecargar) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar tareas"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val estado = uiState.estadoCarga) {
                is EstadoCarga.Cargando -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Cargando tareas...", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                is EstadoCarga.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = estado.mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = onRecargar) {
                            Text("Reintentar")
                        }
                    }
                }

                is EstadoCarga.Exito -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Reto (Entregable 1): Resumen de Estadísticas Derivado
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total: ${uiState.totalTareas}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Pendientes: ${uiState.totalPendientes}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Completadas: ${uiState.totalCompletadas}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Fila de Entrada de texto para nueva tarea
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.textoNuevaTarea,
                                onValueChange = onTextoCambio,
                                label = { Text("Nueva tarea") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            FilledIconButton(onClick = onAgregarTarea) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Agregar tarea"
                                )
                            }
                        }

                        // Fila de Filtros
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FiltroTarea.entries.forEach { filtro ->
                                FilterChip(
                                    selected = uiState.filtroActivo == filtro,
                                    onClick = { onCambiarFiltro(filtro) },
                                    label = { Text(filtro.etiqueta) }
                                )
                            }
                        }

                        // Reto (Entregable 2): Botón "Limpiar completadas"
                        if (uiState.totalCompletadas > 0) {
                            OutlinedButton(
                                onClick = onLimpiarCompletadas,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Limpiar tareas completadas (${uiState.totalCompletadas})")
                            }
                        }

                        // Lista o mensaje vacío
                        if (uiState.tareasFiltradas.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay tareas en esta categoría.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = uiState.tareasFiltradas,
                                    key = { it.id }
                                ) { tarea ->
                                    TareaItem(
                                        tarea = tarea,
                                        onAlternar = { onAlternarCompletada(tarea.id) },
                                        onEliminar = { onEliminarTarea(tarea.id) }
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

@Composable
fun TareaItem(
    tarea: Tarea,
    onAlternar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { onAlternar() }
            )
            Text(
                text = tarea.titulo,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textDecoration = if (tarea.completada) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar tarea",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
