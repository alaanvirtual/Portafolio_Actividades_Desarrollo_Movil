package com.example.practica16

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class RespaldoWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Recuperamos los datos enviados desde la interfaz (Input Data)
        val nombreArchivo = inputData.getString("NOMBRE_ARCHIVO") ?: "respaldo_general.db"
        Log.d("WORK_MANAGER", "Iniciando respaldo de: $nombreArchivo")

        // 2. Simulamos una carga
        for (paso in 1..5) {
            delay(1000L)
            val porcentaje = paso * 20

            // Enviamos el avance para que Jetpack Compose actualice la barra de progreso
            setProgress(workDataOf("PROGRESO" to porcentaje))
            Log.d("WORK_MANAGER", "Progreso de $nombreArchivo: $porcentaje%")
        }

        // 3. Mostramos una notificación
        mostrarNotificacionFinal(
            titulo = "Respaldo Completado",
            mensaje = "El archivo $nombreArchivo se guardó en segundo plano."
        )

        // 4. Devolvemos el resultado a (Output Data)
        val datosSalida = workDataOf(
            "RESULTADO" to "Respaldo de '$nombreArchivo' finalizado al 100%."
        )
        return Result.success(datosSalida)
    }

    private fun mostrarNotificacionFinal(titulo: String, mensaje: String) {
        val idCanal = "canal_workmanager_16"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                idCanal,
                "Tareas en Segundo Plano",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }

        // Verificamos el permisos de Android
        val tienePermiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (tienePermiso) {
            val notificacion = NotificationCompat.Builder(applicationContext, idCanal)
                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(applicationContext).notify(201, notificacion)
        }
    }
}
