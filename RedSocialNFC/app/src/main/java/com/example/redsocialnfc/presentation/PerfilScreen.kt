package com.example.redsocialnfc.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocialnfc.data.Comentario
import com.example.redsocialnfc.data.Publicacion
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.presentation.components.AvatarUsuario
import com.example.redsocialnfc.presentation.components.ReproductorVideoDialog
import com.example.redsocialnfc.presentation.components.VisorImagenDialog
import com.example.redsocialnfc.presentation.components.rememberCameraPhotoLauncher
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.DarkOutline
import com.example.redsocialnfc.ui.theme.DarkSurface
import com.example.redsocialnfc.ui.theme.DarkSurfaceVariant
import com.example.redsocialnfc.ui.theme.ErrorColor
import com.example.redsocialnfc.ui.theme.MutedAccent
import com.example.redsocialnfc.ui.theme.MutedGray
import com.example.redsocialnfc.ui.theme.MutedPurple
import com.example.redsocialnfc.ui.theme.MutedPurpleLight
import com.example.redsocialnfc.ui.theme.SubtleGrayPurple
import com.example.redsocialnfc.ui.theme.TextPrimary
import com.example.redsocialnfc.ui.theme.TextSecondary
import com.example.redsocialnfc.util.MediaStorageHelper
import com.example.redsocialnfc.util.PollHelper
import com.example.redsocialnfc.util.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val userId = sessionManager.getUserId()
    val usuarioDb by db.usuarioDao().observarPorId(userId).collectAsState(initial = null)
    val totalPosts by db.publicacionDao().observarConteoPorAutor(userId).collectAsState(initial = 0)
    val totalLikes by db.publicacionDao().observarLikesRecibidos(userId).collectAsState(initial = 0)
    val totalAmigos by db.amigoDao().observarConteo().collectAsState(initial = 0)
    val totalGuardados by db.publicacionDao().observarConteoGuardados().collectAsState(initial = 0)
    val todasPublicaciones by db.publicacionDao().obtenerTodas().collectAsState(initial = emptyList())
    val todosComentarios by db.comentarioDao().observarTodos().collectAsState(initial = emptyList())

    val email = usuarioDb?.email ?: sessionManager.getEmail()
    val realHandle = usuarioDb?.realHandle ?: sessionManager.getRealHandle()
    val displayName = (usuarioDb?.displayName ?: sessionManager.getDisplayName())
        .ifBlank { realHandle.removePrefix("@") }
    val bio = usuarioDb?.bio ?: sessionManager.getBio()
    val avatarUri = usuarioDb?.avatarUri ?: sessionManager.getAvatarUri()
    val anonHandle = usuarioDb?.anonHandle ?: sessionManager.getAnonHandle()
    val nfcUuid = usuarioDb?.nfcUuid ?: sessionManager.obtenerOGenerarDeviceNfcUuid()

    var mostrarEditarPerfil by remember { mutableStateOf(false) }
    var mostrarOpcionesAvatar by remember { mutableStateOf(false) }
    var perfilTabIndex by remember { mutableIntStateOf(0) }
    var postParaComentarPerfil by remember { mutableStateOf<Publicacion?>(null) }
    var postParaBorrarPerfil by remember { mutableStateOf<Publicacion?>(null) }
    var imagenAmpliadaPerfil by remember { mutableStateOf<String?>(null) }
    var videoReproducirPerfil by remember { mutableStateOf<String?>(null) }

    val misPublicaciones = remember(todasPublicaciones, userId, realHandle, anonHandle) {
        todasPublicaciones.filter {
            it.autorId == userId || it.autorHandle == realHandle || it.autorAnonimo == anonHandle
        }
    }
    val misReposts = remember(todasPublicaciones) {
        todasPublicaciones.filter { it.repostedByMe }
    }
    val misLikes = remember(todasPublicaciones) {
        todasPublicaciones.filter { it.likedByMe }
    }
    val comentariosPorPost = remember(todosComentarios) {
        todosComentarios.groupBy { it.publicacionId }
    }

    val actualizarAvatarLocal: (String) -> Unit = { localPath ->
        scope.launch {
            db.usuarioDao().actualizarPerfil(userId, displayName, bio, localPath)
            db.publicacionDao().actualizarAutorEnPublicaciones(userId, displayName, localPath)
            sessionManager.actualizarDatosPerfil(displayName, bio, localPath)
            Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = MediaStorageHelper.copiarUriAAlmacenamientoLocal(context, uri, prefijo = "avatar")
                if (res != null) {
                    actualizarAvatarLocal(res.localPath)
                }
            }
        }
    }

    val lanzarCamaraAvatar = rememberCameraPhotoLauncher(prefijo = "avatar_cam") { res ->
        actualizarAvatarLocal(res.localPath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            displayName,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "$totalPosts publicaciones",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarEditarPerfil = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar perfil",
                            tint = MutedPurpleLight
                        )
                    }
                    IconButton(onClick = {
                        sessionManager.cerrarSesion()
                        onCerrarSesion()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MutedPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Cabecera tipo Twitter: Banner + Avatar superpuesto + botón Editar perfil
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF2B2238),
                                    MutedPurple.copy(alpha = 0.55f),
                                    Color(0xFF1F1C26)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(DarkBackground)
                            .padding(3.dp)
                            .clickable {
                                mostrarOpcionesAvatar = true
                            }
                    ) {
                        AvatarUsuario(
                            nombreVisible = displayName,
                            avatarUri = avatarUri,
                            esAnonimo = false,
                            size = 78.dp
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(MutedPurple)
                                .border(2.dp, DarkBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { mostrarEditarPerfil = true },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MutedPurpleLight.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        modifier = Modifier.offset(y = (-4).dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Editar perfil", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Identidad verificada",
                        tint = MutedPurpleLight,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "$realHandle · $email",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                if (bio.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Estadísticas tipo Twitter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatInline(numero = totalAmigos, etiqueta = "Amigos NFC")
                    StatInline(numero = totalPosts, etiqueta = "Posts")
                    StatInline(numero = totalLikes, etiqueta = "Me gusta")
                    StatInline(numero = totalGuardados, etiqueta = "Guardados")
                }

                Spacer(Modifier.height(14.dp))

                PrimaryTabRow(
                    selectedTabIndex = perfilTabIndex,
                    containerColor = DarkBackground,
                    contentColor = TextPrimary,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(perfilTabIndex),
                            color = MutedPurpleLight,
                            height = 3.dp
                        )
                    }
                ) {
                    val tabsPerfil = listOf(
                        "Identidad",
                        "Posts (${misPublicaciones.size})",
                        "Reposts (${misReposts.size})",
                        "Likes (${misLikes.size})"
                    )
                    tabsPerfil.forEachIndexed { idx, titulo ->
                        Tab(
                            selected = perfilTabIndex == idx,
                            onClick = { perfilTabIndex = idx }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = titulo,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (perfilTabIndex == idx) FontWeight.Bold else FontWeight.Medium,
                                    color = if (perfilTabIndex == idx) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (perfilTabIndex == 0) {
                    Text(
                        text = "Identidad y Privacidad Local",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MutedPurpleLight
                    )
                    Spacer(Modifier.height(10.dp))

                // Tarjeta 1: Identidad Real (1er grado)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MutedPurple, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "IDENTIDAD REAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MutedPurpleLight
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MutedPurple.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "1er Grado · Contactos",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MutedPurpleLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = realHandle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Visible en chats directos y publicaciones con identidad abierta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Tarjeta 2: Pseudónimo Anónimo (2do grado)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MutedGray, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SubtleGrayPurple)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = MutedAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "PSEUDÓNIMO ANÓNIMO",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MutedAccent
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Text(
                                    text = "FIJO · 2do Grado",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = anonHandle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MutedAccent
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Identidad autogenerada inalterable para interactuar anónimamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Tarjeta 3: Identificador NFC del dispositivo con copiar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkOutline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = null,
                                    tint = MutedAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "IDENTIFICADOR DE HARDWARE NFC",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("NFC UUID", nfcUuid))
                                    Toast.makeText(context, "ID NFC copiado", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar UUID",
                                    tint = MutedPurpleLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = nfcUuid,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Código criptográfico único asignado a tu dispositivo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                } else {
                    val (listaMostrada, mensajeVacio) = when (perfilTabIndex) {
                        1 -> misPublicaciones to "Aún no tienes publicaciones.\nTodo lo que publiques (visible o anónimo) aparecerá aquí."
                        2 -> misReposts to "Aún no has hecho repost.\nLos posts en los que marques Repost aparecerán aquí."
                        else -> misLikes to "Aún no diste Me gusta.\nLas publicaciones con corazón aparecerán aquí."
                    }

                    ListaPostsPerfil(
                        posts = listaMostrada,
                        mensajeVacio = mensajeVacio,
                        mostrarBadgeRepost = perfilTabIndex == 2,
                        userId = userId,
                        realHandle = realHandle,
                        anonHandle = anonHandle,
                        comentariosPorPost = comentariosPorPost,
                        onLike = { post ->
                            scope.launch {
                                val nuevo = !post.likedByMe
                                val conteo = (post.likesCount + if (nuevo) 1 else -1).coerceAtLeast(0)
                                db.publicacionDao().actualizar(post.copy(likedByMe = nuevo, likesCount = conteo))
                            }
                        },
                        onRepost = { post ->
                            scope.launch {
                                val nuevo = !post.repostedByMe
                                val conteo = (post.repostsCount + if (nuevo) 1 else -1).coerceAtLeast(0)
                                db.publicacionDao().actualizar(post.copy(repostedByMe = nuevo, repostsCount = conteo))
                            }
                        },
                        onGuardar = { post ->
                            scope.launch {
                                db.publicacionDao().actualizar(post.copy(guardado = !post.guardado))
                            }
                        },
                        onComentar = { postParaComentarPerfil = it },
                        onVotarEncuesta = { post, indiceVoto ->
                            scope.launch {
                                val opc = PollHelper.parsearEncuesta(post.encuestaOpciones, post.encuestaVotos)
                                    ?: return@launch
                                val (nuevosVotos, nuevoVotoUser) = PollHelper.registrarVoto(
                                    votosJson = post.encuestaVotos,
                                    totalOpciones = opc.opciones.size,
                                    votoAnterior = post.encuestaVotoUsuario,
                                    nuevoVoto = indiceVoto
                                )
                                db.publicacionDao().actualizar(
                                    post.copy(encuestaVotos = nuevosVotos, encuestaVotoUsuario = nuevoVotoUser)
                                )
                            }
                        },
                        onCompartir = { post ->
                            val autor = if (post.esAmigoDirecto) post.autorHandle else post.autorAnonimo
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "$autor en Local:\n${post.contenido}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir publicación"))
                        },
                        onAlternarFijado = { post ->
                            scope.launch {
                                val nuevoFijado = !post.fijado
                                if (nuevoFijado) {
                                    db.publicacionDao().desfijarOtrasDeAutor(post.autorId, post.id)
                                }
                                db.publicacionDao().actualizar(post.copy(fijado = nuevoFijado))
                            }
                        },
                        onAlternarPrivacidad = { post ->
                            scope.launch {
                                db.publicacionDao().actualizar(post.copy(esAmigoDirecto = !post.esAmigoDirecto))
                            }
                        },
                        onEliminar = { postParaBorrarPerfil = it },
                        onAbrirImagen = { imagenAmpliadaPerfil = it },
                        onAbrirVideo = { videoReproducirPerfil = it }
                    )
                }

                Spacer(Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        sessionManager.cerrarSesion()
                        onCerrarSesion()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedPurpleLight),
                    border = BorderStroke(1.dp, MutedPurple)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    postParaComentarPerfil?.let { postActual ->
        val comentariosPost = comentariosPorPost[postActual.id] ?: emptyList()
        ModalBottomSheet(
            onDismissRequest = { postParaComentarPerfil = null },
            containerColor = DarkSurface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ComentariosSheetContent(
                post = postActual,
                comentarios = comentariosPost,
                miUserId = userId,
                miHandle = realHandle,
                miAnonHandle = anonHandle,
                defaultAnon = sessionManager.getSetting(SessionManager.PREF_DEFAULT_ANON_POST, false),
                onEnviarComentario = { texto, esAnon ->
                    scope.launch {
                        db.comentarioDao().insertar(
                            Comentario(
                                publicacionId = postActual.id,
                                autorId = userId,
                                autorNombre = displayName,
                                autorHandle = realHandle,
                                autorAnonimo = anonHandle,
                                esAnonimo = esAnon,
                                contenido = texto
                            )
                        )
                    }
                },
                onEliminarComentario = { c ->
                    scope.launch { db.comentarioDao().eliminar(c) }
                }
            )
        }
    }

    postParaBorrarPerfil?.let { post ->
        AlertDialog(
            onDismissRequest = { postParaBorrarPerfil = null },
            containerColor = DarkSurface,
            title = { Text("¿Eliminar publicación?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Esta acción eliminará la publicación y sus respuestas de forma permanente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            db.comentarioDao().eliminarPorPublicacion(post.id)
                            db.publicacionDao().eliminar(post)
                            postParaBorrarPerfil = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { postParaBorrarPerfil = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    imagenAmpliadaPerfil?.let { path ->
        VisorImagenDialog(path = path, onDismiss = { imagenAmpliadaPerfil = null })
    }

    videoReproducirPerfil?.let { path ->
        ReproductorVideoDialog(path = path, onDismiss = { videoReproducirPerfil = null })
    }

    if (mostrarOpcionesAvatar) {
        AlertDialog(
            onDismissRequest = { mostrarOpcionesAvatar = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    "Foto de Perfil",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            mostrarOpcionesAvatar = false
                            lanzarCamaraAvatar()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MutedPurple),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MutedPurpleLight)
                        Spacer(Modifier.width(8.dp))
                        Text("Tomar foto con cámara")
                    }
                    OutlinedButton(
                        onClick = {
                            mostrarOpcionesAvatar = false
                            pickAvatarLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, DarkOutline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = MutedPurpleLight)
                        Spacer(Modifier.width(8.dp))
                        Text("Elegir de la galería")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarOpcionesAvatar = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    if (mostrarEditarPerfil) {
        EditarPerfilDialog(
            nombreInicial = displayName,
            bioInicial = bio,
            onDismiss = { mostrarEditarPerfil = false },
            onGuardar = { nuevoNombre, nuevaBio ->
                scope.launch {
                    val nombreLimpio = nuevoNombre.trim().ifBlank { realHandle.removePrefix("@") }
                    val bioLimpia = nuevaBio.trim()
                    db.usuarioDao().actualizarPerfil(userId, nombreLimpio, bioLimpia, avatarUri)
                    db.publicacionDao().actualizarAutorEnPublicaciones(userId, nombreLimpio, avatarUri)
                    sessionManager.actualizarDatosPerfil(nombreLimpio, bioLimpia, avatarUri)
                    mostrarEditarPerfil = false
                    Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
private fun ListaPostsPerfil(
    posts: List<Publicacion>,
    mensajeVacio: String,
    mostrarBadgeRepost: Boolean,
    userId: Int,
    realHandle: String,
    anonHandle: String,
    comentariosPorPost: Map<Int, List<Comentario>>,
    onLike: (Publicacion) -> Unit,
    onRepost: (Publicacion) -> Unit,
    onGuardar: (Publicacion) -> Unit,
    onComentar: (Publicacion) -> Unit,
    onVotarEncuesta: (Publicacion, Int) -> Unit,
    onCompartir: (Publicacion) -> Unit,
    onAlternarFijado: (Publicacion) -> Unit,
    onAlternarPrivacidad: (Publicacion) -> Unit,
    onEliminar: (Publicacion) -> Unit,
    onAbrirImagen: (String) -> Unit,
    onAbrirVideo: (String) -> Unit
) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mensajeVacio,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            posts.forEach { post ->
                val esMio = post.autorId == userId || post.autorHandle == realHandle || post.autorAnonimo == anonHandle
                val replies = comentariosPorPost[post.id]?.size ?: 0

                if (mostrarBadgeRepost) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp, bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Reposteaste",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF81C784)
                        )
                    }
                }

                TarjetaPostTwitter(
                    post = post,
                    esMio = esMio,
                    compacto = true,
                    comentariosCount = replies,
                    onLike = { onLike(post) },
                    onRepost = { onRepost(post) },
                    onGuardar = { onGuardar(post) },
                    onComentar = { onComentar(post) },
                    onVotarEncuesta = { onVotarEncuesta(post, it) },
                    onCompartir = { onCompartir(post) },
                    onAlternarFijado = { onAlternarFijado(post) },
                    onAlternarPrivacidad = { onAlternarPrivacidad(post) },
                    onEliminar = { onEliminar(post) },
                    onAbrirImagen = onAbrirImagen,
                    onAbrirVideo = onAbrirVideo
                )
                HorizontalDivider(color = DarkOutline.copy(alpha = 0.35f))
            }
        }
    }
}

@Composable
private fun StatInline(numero: Int, etiqueta: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$numero",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun EditarPerfilDialog(
    nombreInicial: String,
    bioInicial: String,
    onDismiss: () -> Unit,
    onGuardar: (String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(nombreInicial) }
    var bio by remember { mutableStateOf(bioInicial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                "Editar Perfil",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { if (it.length <= 30) nombre = it },
                    label = { Text("Nombre para mostrar") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedPurple,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 160) bio = it },
                    label = { Text("Biografía (${bio.length}/160)") },
                    minLines = 3,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedPurple,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, bio) },
                colors = ButtonDefaults.buttonColors(containerColor = MutedPurple)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}
