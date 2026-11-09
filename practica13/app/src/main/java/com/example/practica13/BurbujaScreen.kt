package com.example.practica13

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurbujaScreen(sensorManager: SensorManager) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Guardamos las lecturas crudas actuales para permitir la calibración
    var currentRawX by remember { mutableFloatStateOf(0f) }
    var currentRawY by remember { mutableFloatStateOf(0f) }

    // Offsets de calibración a cero
    var zeroX by remember { mutableFloatStateOf(0f) }
    var zeroY by remember { mutableFloatStateOf(0f) }

    // Factor de sensibilidad (35f para movimiento ágil y rápido)
    val sensibilidad = 35f

    // Limite real para mantenerse dentro del contenedor (125dp radio - 20dp burbuja = 105dp)
    val radioMaximo = 105f

    // Factor de suavizado (0.35f = respuesta rápida e instantánea)
    val alpha = 0.35f

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            private var filteredX = 0f
            private var filteredY = 0f

            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val sensorX = -it.values[0]
                    val sensorY = it.values[1]

                    currentRawX = sensorX
                    currentRawY = sensorY

                    // 1. Calculamos la aceleración restando la calibración a cero
                    val targetX = (sensorX - zeroX) * sensibilidad
                    val targetY = (sensorY - zeroY) * sensibilidad

                    // 2. Filtro de respuesta rápida (0.35f) sin retardo
                    filteredX += alpha * (targetX - filteredX)
                    filteredY += alpha * (targetY - filteredY)

                    // 3. Calculamos la distancia desde el centro usando Pitágoras
                    val distancia = sqrt((filteredX * filteredX) + (filteredY * filteredY))

                    // 4. Limitamos para que la burbuja no se salga del círculo
                    if (distancia > radioMaximo) {
                        val proporcion = radioMaximo / distancia
                        offsetX = filteredX * proporcion
                        offsetY = filteredY * proporcion
                    } else {
                        offsetX = filteredX
                        offsetY = filteredY
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(listener, acelerometro, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nivel de Burbuja") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .background(Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(50.dp).border(2.dp, Color.Red, CircleShape))

                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .size(40.dp)
                        .background(Color(0xFF00FF00), CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        zeroX = currentRawX
                        zeroY = currentRawY
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Calibrar Centro")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Eje X: ${"%.2f".format(offsetX / sensibilidad)}",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    "Eje Y: ${"%.2f".format(offsetY / sensibilidad)}",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}
