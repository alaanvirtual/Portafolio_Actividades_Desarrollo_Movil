package com.example.redsocialnfc.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocialnfc.data.Publicacion
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.DarkOutline
import com.example.redsocialnfc.ui.theme.DarkSurface
import com.example.redsocialnfc.ui.theme.DarkSurfaceVariant
import com.example.redsocialnfc.ui.theme.MutedAccent
import com.example.redsocialnfc.ui.theme.MutedGray
import com.example.redsocialnfc.ui.theme.MutedPurple
import com.example.redsocialnfc.ui.theme.SubtleGrayPurple
import com.example.redsocialnfc.ui.theme.TextPrimary
import com.example.redsocialnfc.ui.theme.TextSecondary
import com.example.redsocialnfc.util.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FiltroFeed(val etiqueta: String) {
    TODAS("Todas"),
    AMIGOS("Contactos"),
    ANONIMAS("Anónimas")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = remember { db.publicacionDao() }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val publicaciones by dao.obtenerTodas().collectAsState(initial = emptyList())
    var filtroActual by remember { mutableStateOf(FiltroFeed.TODAS) }
    var mostrarSheetCrear by remember { mutableStateOf(false) }

    // Cargar publicaciones de muestra si la base de datos está vacía para ver la funcionalidad
    LaunchedEffect(Unit) {
        if (dao.contarTotal() == 0) {
            dao.insertar(
                Publicacion(
                    autorId = 99,
                    autorHandle = "@carlos_dev",
                    autorAnonimo = "@Anonimo_45A",
                    contenido = "¡Sincronizado por sensor de proximidad! Excelente diseño del sistema.",
                    esAmigoDirecto = true
                )
            )
            dao.insertar(
                Publicacion(
                    autorId = 98,
                    autorHandle = "@valeria_m",
                    autorAnonimo = "@Anonimo_72K",
                    contenido = "Publicando desde el círculo de confianza extendido. La identidad se mantiene oculta.",
                    esAmigoDirecto = false
                )
            )
        }
    }

    val listaFiltrada = remember(publicaciones, filtroActual) {
        when (filtroActual) {
            FiltroFeed.TODAS -> publicaciones
            FiltroFeed.AMIGOS -> publicaciones.filter { it.esAmigoDirecto }
            FiltroFeed.ANONIMAS -> publicaciones.filter { !it.esAmigoDirecto }
        }
    }

    val totalAmigos = publicaciones.count { it.esAmigoDirecto }
    val totalAnonimas = publicaciones.count { !it.esAmigoDirecto }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Local",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            "Muro de Proximidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarSheetCrear = true },
                containerColor = MutedPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Publicar")
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Dashboard de métricas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaMetrica(
                    titulo = "Contactos",
                    valor = "$totalAmigos",
                    icono = Icons.Default.Person,
                    color = MutedPurple,
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Anónimas",
                    valor = "$totalAnonimas",
                    icono = Icons.Default.VisibilityOff,
                    color = MutedGray,
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Total Posts",
                    valor = "${publicaciones.size}",
                    icono = Icons.Default.DynamicFeed,
                    color = MutedAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            // Barra horizontal de filtros
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FiltroFeed.entries.toTypedArray()) { f ->
                    FilterChip(
                        selected = filtroActual == f,
                        onClick = { filtroActual = f },
                        label = { Text(f.etiqueta, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MutedPurple,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filtroActual == f,
                            borderColor = if (filtroActual == f) MutedPurple else DarkOutline
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Lista de Publicaciones
            if (listaFiltrada.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DynamicFeed,
                            contentDescription = null,
                            tint = DarkOutline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "No hay publicaciones en este filtro",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listaFiltrada, key = { it.id }) { post ->
                        TarjetaPublicacion(
                            post = post,
                            esMio = post.autorId == sessionManager.getUserId(),
                            onEliminar = {
                                scope.launch { dao.eliminar(post) }
                            }
                        )
                    }
                }
            }
        }

        // Hoja modal para redactar post (280 caracteres con contador en vivo)
        if (mostrarSheetCrear) {
            ModalBottomSheet(
                onDismissRequest = { mostrarSheetCrear = false },
                containerColor = DarkSurface,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                CrearPostSheetContent(
                    onPublicar = { contenido, publicarAnonimo ->
                        scope.launch {
                            dao.insertar(
                                Publicacion(
                                    autorId = sessionManager.getUserId(),
                                    autorHandle = sessionManager.getRealHandle(),
                                    autorAnonimo = sessionManager.getAnonHandle(),
                                    contenido = contenido,
                                    esAmigoDirecto = !publicarAnonimo
                                )
                            )
                            mostrarSheetCrear = false
                        }
                    },
                    onCancelar = { mostrarSheetCrear = false }
                )
            }
        }
    }
}

@Composable
private fun TarjetaMetrica(
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun TarjetaPublicacion(
    post: Publicacion,
    esMio: Boolean,
    onEliminar: () -> Unit
) {
    val formatoFecha = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val fechaLegible = remember(post.timestamp) { formatoFecha.format(Date(post.timestamp)) }

    val colorBorde = if (post.esAmigoDirecto) MutedPurple else MutedGray
    val colorFondo = if (post.esAmigoDirecto) DarkSurface else SubtleGrayPurple

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorBorde, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (post.esAmigoDirecto) MutedPurple else MutedGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (post.esAmigoDirecto) Icons.Default.Person else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (post.esAmigoDirecto) post.autorHandle else post.autorAnonimo,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = if (!post.esAmigoDirecto) FontFamily.Monospace else FontFamily.Default
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = if (post.esAmigoDirecto) "Contacto directo" else "Círculo anónimo",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (post.esAmigoDirecto) MutedPurple else MutedGray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = fechaLegible,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    if (esMio) {
                        IconButton(onClick = onEliminar) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = post.contenido,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun CrearPostSheetContent(
    onPublicar: (String, Boolean) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf("") }
    var modoAnonimo by remember { mutableStateOf(false) }
    val maxCaracteres = 280

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(
            text = "Nueva Publicación",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = texto,
            onValueChange = {
                if (it.length <= maxCaracteres) {
                    texto = it
                }
            },
            placeholder = { Text("¿Qué está sucediendo?", color = TextSecondary) },
            minLines = 4,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MutedPurple,
                unfocusedBorderColor = DarkOutline,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${texto.length}/$maxCaracteres",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (texto.length >= maxCaracteres) MaterialTheme.colorScheme.error else TextSecondary
                    )
                }
            }
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Publicar con seudónimo anónimo",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = if (modoAnonimo) "Tu usuario quedará oculto" else "Visible para tus contactos",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Switch(
                checked = modoAnonimo,
                onCheckedChange = { modoAnonimo = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = MutedPurple
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onCancelar,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
            ) {
                Text("Cancelar", color = TextSecondary)
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = {
                    if (texto.isNotBlank()) {
                        onPublicar(texto.trim(), modoAnonimo)
                    }
                },
                enabled = texto.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MutedPurple,
                    contentColor = Color.White
                )
            ) {
                Text("Publicar", fontWeight = FontWeight.Bold)
            }
        }
    }
}
