package com.example.practica16

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.*
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkManagerScreen() {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }

    // Guardamos el ID de la tarea actual para monitorear su estado
    var workId by remember { mutableStateOf<UUID?>(null) }
    var requiereInternet by remember { mutableStateOf(false) }
    // Reto (Entregable): Segunda restricción controlable desde la interfaz
    var requiereCarga by remember { mutableStateOf(false) }

    // Solicitamos permiso de notificaciones
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val estado = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            )
            if (estado != PackageManager.PERMISSION_GRANTED) {
                permisoLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Observamos el estado y progreso de la tarea en tiempo real mediante Flow
    val workInfo by remember(workId) {
        if (workId != null) {
            workManager.getWorkInfoByIdFlow(workId!!)
        } else {
            flowOf(null)
        }
    }.collectAsState(initial = null)

    // Calculamos el progreso
    val estadoTexto = when (workInfo?.state) {
        WorkInfo.State.ENQUEUED -> "EN ESPERA (EN COLA)"
        WorkInfo.State.RUNNING -> "EN EJECUCIÓN... "
        WorkInfo.State.SUCCEEDED -> "COMPLETADO CON ÉXITO"
        WorkInfo.State.FAILED -> "FALLÓ LA TAREA"
        WorkInfo.State.BLOCKED -> "BLOQUEADO"
        WorkInfo.State.CANCELLED -> "CANCELADO"
        null -> "SIN TAREA ACTIVA"
    }

    val progreso = if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
        100
    } else {
        workInfo?.progress?.getInt("PROGRESO", 0) ?: 0
    }

    val mensajeResultado = workInfo?.outputData?.getString("RESULTADO")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WorkManager - Respaldo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Código para el monitoreo en tiempo real
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Estado de la tarea: $estadoTexto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    LinearProgressIndicator(
                        progress = { progreso / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Progreso: $progreso%",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (mensajeResultado != null) {
                        Text(
                            text = mensajeResultado,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Switch para configurar la restricción (Constraint) de red
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Requerir conexión a Internet",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = requiereInternet,
                    onCheckedChange = { requiereInternet = it }
                )
            }

            // Reto (Entregable): Switch para restricción de carga eléctrica
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Solo mientras está cargando",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = requiereCarga,
                    onCheckedChange = { requiereCarga = it }
                )
            }

            HorizontalDivider()

            // Botón 1: Tarea única (OneTimeWorkRequest)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val restricciones = Constraints.Builder().apply {
                        if (requiereInternet) {
                            setRequiredNetworkType(NetworkType.CONNECTED)
                        } else {
                            setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        }
                        setRequiresCharging(requiereCarga)
                    }.build()

                    val datosEntrada = workDataOf("NOMBRE_ARCHIVO" to "fotos_practica16.zip")

                    val solicitudUnica = OneTimeWorkRequestBuilder<RespaldoWorker>()
                        .setConstraints(restricciones)
                        .setInputData(datosEntrada)
                        .addTag("respaldo_unico")
                        .build()

                    workManager.enqueue(solicitudUnica)
                    workId = solicitudUnica.id
                }
            ) {
                Text("Iniciar Respaldo Único (OneTime)")
            }

            // Botón 2: Tarea periódica cada 15 minutos (PeriodicWorkRequest)
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val restricciones = Constraints.Builder().apply {
                        if (requiereInternet) {
                            setRequiredNetworkType(NetworkType.CONNECTED)
                        } else {
                            setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        }
                        setRequiresCharging(requiereCarga)
                    }.build()

                    val datosEntrada = workDataOf("NOMBRE_ARCHIVO" to "respaldo_automatico.db")

                    val solicitudPeriodica = PeriodicWorkRequestBuilder<RespaldoWorker>(
                        15, TimeUnit.MINUTES
                    )
                        .setConstraints(restricciones)
                        .setInputData(datosEntrada)
                        .build()

                    // Evitamos tareas duplicadas con el mismo nombre
                    workManager.enqueueUniquePeriodicWork(
                        "trabajo_respaldo_periodico",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        solicitudPeriodica
                    )

                    workId = solicitudPeriodica.id
                }
            ) {
                Text("Programar Respaldo Periódico (15 min)")
            }

            // Botón 3: Cancelar todas las tareas activas o en cola
            TextButton(
                onClick = {
                    workManager.cancelAllWork()
                }
            ) {
                Text(
                    text = "Cancelar Todas las Tareas",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
