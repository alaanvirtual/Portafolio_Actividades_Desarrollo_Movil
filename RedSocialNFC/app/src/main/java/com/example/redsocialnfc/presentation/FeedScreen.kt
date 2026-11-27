package com.example.redsocialnfc.presentation

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocialnfc.data.Amigo
import com.example.redsocialnfc.data.Categoria
import com.example.redsocialnfc.data.Comentario
import com.example.redsocialnfc.data.MediaTipoPost
import com.example.redsocialnfc.data.Publicacion
import com.example.redsocialnfc.data.VisibilidadMuro
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.presentation.components.AvatarUsuario
import com.example.redsocialnfc.presentation.components.BadgeAudienciaPost
import com.example.redsocialnfc.presentation.components.FotoPreviewCard
import com.example.redsocialnfc.presentation.components.ReproductorVideoDialog
import com.example.redsocialnfc.presentation.components.SelectorAudienciaMuro
import com.example.redsocialnfc.presentation.components.VideoPreviewCard
import com.example.redsocialnfc.presentation.components.VisorImagenDialog
import com.example.redsocialnfc.presentation.components.iconoPorVisibilidad
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

enum class FiltroFeed(val etiqueta: String) {
    TODAS("Para ti"),
    AMIGOS("Contactos"),
    ANONIMAS("Anónimo"),
    GUARDADOS("Guardados")
}

private const val CATEGORIA_PRINCIPAL = "Principal"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = remember { db.publicacionDao() }
    val comentarioDao = remember { db.comentarioDao() }
    val categoriaDao = remember { db.categoriaDao() }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val miUserId = sessionManager.getUserId()
    val usuarioDb by db.usuarioDao().observarPorId(miUserId).collectAsState(initial = null)
    val miHandle = (usuarioDb?.realHandle ?: sessionManager.getRealHandle()).ifEmpty { "@usuario" }
    val miNombre = (usuarioDb?.displayName ?: sessionManager.getDisplayName()).ifBlank { miHandle.removePrefix("@") }
    val miAvatar = usuarioDb?.avatarUri ?: sessionManager.getAvatarUri()
    val miAnonHandle = (usuarioDb?.anonHandle ?: sessionManager.getAnonHandle()).ifEmpty { "@Anonimo" }

    val publicaciones by dao.obtenerTodas().collectAsState(initial = emptyList())
    val todosComentarios by comentarioDao.observarTodos().collectAsState(initial = emptyList())
    val categoriasDb by categoriaDao.observarTodas().collectAsState(initial = emptyList())
    val amigos by db.amigoDao().obtenerTodos().collectAsState(initial = emptyList())

    val comentariosPorPost = remember(todosComentarios) {
        todosComentarios.groupBy { it.publicacionId }
    }

    var filtroActual by remember { mutableStateOf(FiltroFeed.TODAS) }
    var mostrarBusqueda by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }
    var categoriaFiltro by remember { mutableStateOf(CATEGORIA_PRINCIPAL) }

    val categoriaSeleccionadaActual = if (categoriasDb.isEmpty()) CATEGORIA_PRINCIPAL else categoriaFiltro

    var mostrarSheetCrear by remember { mutableStateOf(false) }
    var postParaEditar by remember { mutableStateOf<Publicacion?>(null) }
    var postParaCitar by remember { mutableStateOf<Publicacion?>(null) }
    var postParaBorrar by remember { mutableStateOf<Publicacion?>(null) }
    var mostrarCrearCategoria by remember { mutableStateOf(false) }
    var categoriaParaBorrar by remember { mutableStateOf<Categoria?>(null) }
    var postParaComentar by remember { mutableStateOf<Publicacion?>(null) }
    var imagenAmpliadaPath by remember { mutableStateOf<String?>(null) }
    var videoReproducirPath by remember { mutableStateOf<String?>(null) }
    val feedListState = rememberLazyListState()

    val modoCompacto = sessionManager.getSetting(SessionManager.PREF_COMPACT_FEED, false)

    val listaFiltrada = remember(
        publicaciones,
        categoriasDb,
        amigos,
        filtroActual,
        textoBusqueda,
        categoriaFiltro,
        miUserId,
        miHandle,
        miAnonHandle
    ) {
        publicaciones.filter { post ->
            val cumpleTab = when (filtroActual) {
                FiltroFeed.TODAS -> true
                FiltroFeed.AMIGOS -> post.esAmigoDirecto
                FiltroFeed.ANONIMAS -> !post.esAmigoDirecto
                FiltroFeed.GUARDADOS -> post.guardado
            }
            val cumpleCategoria = when {
                categoriasDb.isEmpty() -> true
                categoriaFiltro == "Todas" -> true
                else -> post.categoria == categoriaFiltro
            }
            val esMioPost = post.autorId == miUserId ||
                post.autorHandle == miHandle ||
                post.autorAnonimo == miAnonHandle
            val cumpleAudiencia = if (esMioPost) {
                true
            } else {
                val esAmigoNfc = amigos.any { it.handle.equals(post.autorHandle, ignoreCase = true) }
                when (post.audiencia) {
                    VisibilidadMuro.PRIVADO -> false
                    VisibilidadMuro.AMIGOS -> esAmigoNfc
                    VisibilidadMuro.SELECCIONADOS -> {
                        val permitidos = VisibilidadMuro.parsearHandles(post.audienciaPermitidos)
                        esAmigoNfc && permitidos.any { it.equals(miHandle, ignoreCase = true) }
                    }
                    else -> true
                }
            }
            val q = textoBusqueda.trim().lowercase()
            val cumpleBusqueda = q.isEmpty() ||
                post.contenido.lowercase().contains(q) ||
                post.autorHandle.lowercase().contains(q) ||
                post.autorNombre.lowercase().contains(q) ||
                post.autorAnonimo.lowercase().contains(q) ||
                post.categoria.lowercase().contains(q)

            cumpleTab && cumpleCategoria && cumpleAudiencia && cumpleBusqueda
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(DarkSurface)) {
                TopAppBar(
                    navigationIcon = {
                        Box(modifier = Modifier.padding(start = 12.dp)) {
                            AvatarUsuario(
                                nombreVisible = miNombre,
                                avatarUri = miAvatar,
                                esAnonimo = false,
                                size = 34.dp
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                "Local",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                "Muro de Proximidad",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedPurpleLight
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            mostrarBusqueda = !mostrarBusqueda
                            if (!mostrarBusqueda) textoBusqueda = ""
                        }) {
                            Icon(
                                imageVector = if (mostrarBusqueda) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Buscar publicaciones",
                                tint = MutedPurpleLight
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
                )

                AnimatedVisibility(visible = mostrarBusqueda) {
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        placeholder = { Text("Buscar por texto, @usuario o tema...", color = TextSecondary) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MutedPurpleLight)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MutedPurple,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Tabs estilo Twitter
                PrimaryTabRow(
                    selectedTabIndex = filtroActual.ordinal,
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(filtroActual.ordinal),
                            color = MutedPurpleLight,
                            height = 3.dp
                        )
                    }
                ) {
                    FiltroFeed.entries.forEach { tab ->
                        Tab(
                            selected = filtroActual == tab,
                            onClick = { filtroActual = tab }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.etiqueta,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp,
                                    fontWeight = if (filtroActual == tab) FontWeight.Bold else FontWeight.Medium,
                                    color = if (filtroActual == tab) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarSheetCrear = true },
                containerColor = MutedPurple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva publicación")
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de categorías: solo Principal por defecto + creadas por usuarios + botón Crear
            BarraCategoriasChips(
                categorias = categoriasDb,
                categoriaSeleccionada = categoriaSeleccionadaActual,
                miUserId = miUserId,
                incluirTodas = categoriasDb.isNotEmpty(),
                onSeleccionar = { categoriaFiltro = it },
                onSolicitarCrear = { mostrarCrearCategoria = true },
                onSolicitarBorrar = { categoriaParaBorrar = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            HorizontalDivider(color = DarkOutline.copy(alpha = 0.35f))

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
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = if (publicaciones.isEmpty()) {
                                "Tu muro está vacío"
                            } else {
                                "Sin resultados en esta sección"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (publicaciones.isEmpty()) {
                                "Comparte un pensamiento, fotografía o video con identidad visible o anónima."
                            } else {
                                "Prueba cambiando el filtro o publica algo nuevo."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        if (publicaciones.isEmpty()) {
                            Spacer(Modifier.height(18.dp))
                            Button(
                                onClick = { mostrarSheetCrear = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MutedPurple),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Publicar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = feedListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 92.dp)
                ) {
                    items(listaFiltrada, key = { it.id }) { post ->
                        val esMio = post.autorId == miUserId ||
                            post.autorHandle == miHandle ||
                            post.autorAnonimo == miAnonHandle
                        val repliesCount = comentariosPorPost[post.id]?.size ?: 0

                        TarjetaPostTwitter(
                            post = post,
                            esMio = esMio,
                            compacto = modoCompacto,
                            comentariosCount = repliesCount,
                            onLike = {
                                scope.launch {
                                    val nuevoEstado = !post.likedByMe
                                    val nuevoConteo = (post.likesCount + if (nuevoEstado) 1 else -1).coerceAtLeast(0)
                                    dao.actualizar(post.copy(likedByMe = nuevoEstado, likesCount = nuevoConteo))
                                }
                            },
                            onRepost = {
                                scope.launch {
                                    val nuevoEstado = !post.repostedByMe
                                    val nuevoConteo = (post.repostsCount + if (nuevoEstado) 1 else -1).coerceAtLeast(0)
                                    dao.actualizar(post.copy(repostedByMe = nuevoEstado, repostsCount = nuevoConteo))
                                }
                            },
                            onGuardar = {
                                scope.launch {
                                    dao.actualizar(post.copy(guardado = !post.guardado))
                                }
                            },
                            onComentar = { postParaComentar = post },
                            onCitar = { postParaCitar = post },
                            onVotarEncuesta = { indiceVoto ->
                                scope.launch {
                                    val opc = PollHelper.parsearEncuesta(post.encuestaOpciones, post.encuestaVotos)
                                        ?: return@launch
                                    val (nuevosVotos, nuevoVotoUser) = PollHelper.registrarVoto(
                                        votosJson = post.encuestaVotos,
                                        totalOpciones = opc.opciones.size,
                                        votoAnterior = post.encuestaVotoUsuario,
                                        nuevoVoto = indiceVoto
                                    )
                                    dao.actualizar(
                                        post.copy(encuestaVotos = nuevosVotos, encuestaVotoUsuario = nuevoVotoUser)
                                    )
                                }
                            },
                            onCompartir = {
                                val autor = if (post.esAmigoDirecto) post.autorHandle else post.autorAnonimo
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "$autor en Local:\n${post.contenido}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Compartir publicación"))
                            },
                            onEditar = { postParaEditar = post },
                            onAlternarFijado = {
                                scope.launch {
                                    val nuevoFijado = !post.fijado
                                    if (nuevoFijado) {
                                        dao.desfijarOtrasDeAutor(post.autorId, post.id)
                                    }
                                    dao.actualizar(post.copy(fijado = nuevoFijado))
                                }
                            },
                            onAlternarPrivacidad = {
                                scope.launch {
                                    dao.actualizar(post.copy(esAmigoDirecto = !post.esAmigoDirecto))
                                }
                            },
                            onEliminar = { postParaBorrar = post },
                            onAbrirImagen = { imagenAmpliadaPath = it },
                            onAbrirVideo = { videoReproducirPath = it }
                        )
                        HorizontalDivider(color = DarkOutline.copy(alpha = 0.35f))
                    }
                }
            }
        }
    }

    val sheetActivo = mostrarSheetCrear || postParaEditar != null || postParaCitar != null
    if (sheetActivo) {
        val editando = postParaEditar
        val citando = postParaCitar
        val defaultAnon = sessionManager.getSetting(SessionManager.PREF_DEFAULT_ANON_POST, false)
        ModalBottomSheet(
            onDismissRequest = {
                mostrarSheetCrear = false
                postParaEditar = null
                postParaCitar = null
            },
            containerColor = DarkSurface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CrearPostTwitterSheet(
                miNombre = miNombre,
                miHandle = miHandle,
                miAnonHandle = miAnonHandle,
                miAvatarUri = miAvatar,
                miUserId = miUserId,
                categorias = categoriasDb,
                amigos = amigos,
                categoriaInicial = if (categoriaSeleccionadaActual == "Todas") CATEGORIA_PRINCIPAL else categoriaSeleccionadaActual,
                modoAnonimoInicial = if (editando != null) !editando.esAmigoDirecto else defaultAnon,
                postEdicion = editando,
                postCitado = citando,
                onSolicitarCrearCategoria = { mostrarCrearCategoria = true },
                onSolicitarBorrarCategoria = { categoriaParaBorrar = it },
                onPublicar = { contenido, publicarAnonimo, categoria, lugar, mediaTipo, mediaUri, encOpciones, encVotos, audiencia, audienciaPermitidos ->
                    scope.launch {
                        if (editando != null) {
                            dao.actualizar(
                                editando.copy(
                                    contenido = contenido,
                                    categoria = categoria,
                                    etiquetaLugar = lugar?.takeIf { it.isNotBlank() },
                                    esAmigoDirecto = !publicarAnonimo,
                                    mediaTipo = mediaTipo,
                                    mediaUri = mediaUri,
                                    encuestaOpciones = encOpciones,
                                    encuestaVotos = encVotos,
                                    encuestaVotoUsuario = if (encOpciones == editando.encuestaOpciones) {
                                        editando.encuestaVotoUsuario
                                    } else -1,
                                    audiencia = audiencia,
                                    audienciaPermitidos = audienciaPermitidos
                                )
                            )
                            Toast.makeText(context, "Publicación actualizada", Toast.LENGTH_SHORT).show()
                        } else {
                            val autorCitaLabel = citando?.let {
                                if (it.esAmigoDirecto) "${it.autorNombre.ifBlank { it.autorHandle.removePrefix("@") }} (${it.autorHandle})"
                                else "Círculo Anónimo (${it.autorAnonimo})"
                            }
                            dao.insertar(
                                Publicacion(
                                    autorId = miUserId,
                                    autorNombre = miNombre,
                                    autorHandle = miHandle,
                                    autorAvatarUri = miAvatar,
                                    autorAnonimo = miAnonHandle,
                                    contenido = contenido,
                                    categoria = categoria,
                                    etiquetaLugar = lugar?.takeIf { it.isNotBlank() },
                                    esAmigoDirecto = !publicarAnonimo,
                                    mediaTipo = mediaTipo,
                                    mediaUri = mediaUri,
                                    encuestaOpciones = encOpciones,
                                    encuestaVotos = encVotos,
                                    citadoAutor = autorCitaLabel,
                                    citadoContenido = citando?.contenido?.takeIf { it.isNotBlank() }
                                        ?: citando?.let { if (it.mediaTipo != MediaTipoPost.NINGUNO) "[Publicación con multimedia]" else null },
                                    audiencia = audiencia,
                                    audienciaPermitidos = audienciaPermitidos
                                )
                            )
                            if (listaFiltrada.isNotEmpty()) {
                                feedListState.animateScrollToItem(0)
                            }
                        }
                        mostrarSheetCrear = false
                        postParaEditar = null
                        postParaCitar = null
                    }
                },
                onCancelar = {
                    mostrarSheetCrear = false
                    postParaEditar = null
                    postParaCitar = null
                }
            )
        }
    }

    postParaComentar?.let { postActual ->
        val comentariosPost = comentariosPorPost[postActual.id] ?: emptyList()
        ModalBottomSheet(
            onDismissRequest = { postParaComentar = null },
            containerColor = DarkSurface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ComentariosSheetContent(
                post = postActual,
                comentarios = comentariosPost,
                miHandle = miHandle,
                miAnonHandle = miAnonHandle,
                defaultAnon = sessionManager.getSetting(SessionManager.PREF_DEFAULT_ANON_POST, false),
                onEnviarComentario = { texto, esAnon ->
                    scope.launch {
                        comentarioDao.insertar(
                            Comentario(
                                publicacionId = postActual.id,
                                autorId = miUserId,
                                autorNombre = miNombre,
                                autorHandle = miHandle,
                                autorAnonimo = miAnonHandle,
                                esAnonimo = esAnon,
                                contenido = texto
                            )
                        )
                    }
                },
                onEliminarComentario = { c ->
                    scope.launch { comentarioDao.eliminar(c) }
                },
                miUserId = miUserId
            )
        }
    }

    imagenAmpliadaPath?.let { path ->
        VisorImagenDialog(path = path, onDismiss = { imagenAmpliadaPath = null })
    }

    videoReproducirPath?.let { path ->
        ReproductorVideoDialog(path = path, onDismiss = { videoReproducirPath = null })
    }

    if (mostrarCrearCategoria) {
        CrearCategoriaDialog(
            amigos = amigos,
            onDismiss = { mostrarCrearCategoria = false },
            onCrear = { nombreNuevo, visibilidad, permitidosCsv ->
                val limpio = nombreNuevo.trim()
                scope.launch {
                    if (limpio.equals(CATEGORIA_PRINCIPAL, ignoreCase = true) ||
                        limpio.equals("Todas", ignoreCase = true)
                    ) {
                        Toast.makeText(context, "Ese nombre está reservado", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    if (categoriaDao.obtenerPorNombre(limpio) != null) {
                        Toast.makeText(context, "La categoría '$limpio' ya existe", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    categoriaDao.insertar(
                        Categoria(
                            nombre = limpio,
                            autorId = miUserId,
                            visibilidad = visibilidad,
                            permitidosHandles = permitidosCsv
                        )
                    )
                    mostrarCrearCategoria = false
                    Toast.makeText(context, "Categoría '$limpio' creada", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    postParaBorrar?.let { post ->
        AlertDialog(
            onDismissRequest = { postParaBorrar = null },
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
                            comentarioDao.eliminarPorPublicacion(post.id)
                            dao.eliminar(post)
                            postParaBorrar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { postParaBorrar = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    categoriaParaBorrar?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoriaParaBorrar = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    "¿Eliminar categoría?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Se eliminará '${cat.nombre}'. Solo tú como creador puedes borrarla, y sus publicaciones pasarán a '$CATEGORIA_PRINCIPAL'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            dao.moverACategoriaPrincipal(cat.nombre)
                            categoriaDao.eliminar(cat)
                            if (categoriaFiltro == cat.nombre) {
                                categoriaFiltro = CATEGORIA_PRINCIPAL
                            }
                            categoriaParaBorrar = null
                            Toast.makeText(context, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoriaParaBorrar = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
internal fun TarjetaPostTwitter(
    post: Publicacion,
    esMio: Boolean,
    compacto: Boolean,
    comentariosCount: Int,
    onLike: () -> Unit,
    onRepost: () -> Unit,
    onGuardar: () -> Unit,
    onComentar: () -> Unit,
    onCitar: (() -> Unit)? = null,
    onVotarEncuesta: ((Int) -> Unit)? = null,
    onCompartir: () -> Unit,
    onEditar: (() -> Unit)? = null,
    onAlternarFijado: (() -> Unit)? = null,
    onAlternarPrivacidad: (() -> Unit)? = null,
    onEliminar: () -> Unit,
    onAbrirImagen: (String) -> Unit,
    onAbrirVideo: (String) -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    var menuRepostAbierto by remember { mutableStateOf(false) }
    val esVisible = post.esAmigoDirecto
    val nombreMostrar = if (esVisible) {
        post.autorNombre.ifBlank { post.autorHandle.removePrefix("@") }
    } else {
        "Círculo Anónimo"
    }
    val handleMostrar = if (esVisible) post.autorHandle else post.autorAnonimo
    val tiempoRelativo = remember(post.timestamp) {
        MediaStorageHelper.formatearTiempoRelativo(post.timestamp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (esVisible) DarkBackground else SubtleGrayPurple.copy(alpha = 0.45f))
            .padding(
                horizontal = 16.dp,
                vertical = if (compacto) 10.dp else 14.dp
            ),
        verticalAlignment = Alignment.Top
    ) {
        AvatarUsuario(
            nombreVisible = nombreMostrar,
            avatarUri = post.autorAvatarUri,
            esAnonimo = !esVisible,
            size = if (compacto) 38.dp else 44.dp
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (post.fijado) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = MutedPurpleLight,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Publicación fijada",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedPurpleLight
                    )
                }
            }

            // Cabecera Twitter: Nombre + Badge + @handle · tiempo + menú ⋮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = nombreMostrar,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = if (esVisible) Icons.Default.Verified else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (esVisible) MutedPurpleLight else MutedGray,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "$handleMostrar · $tiempoRelativo",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = if (!esVisible) FontFamily.Monospace else FontFamily.Default
                        ),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (esMio) {
                    Box {
                        IconButton(
                            onClick = { menuAbierto = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones de publicación",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false },
                            containerColor = DarkSurface
                        ) {
                            if (onEditar != null) {
                                DropdownMenuItem(
                                    text = { Text("Editar publicación", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = MutedPurpleLight) },
                                    onClick = {
                                        menuAbierto = false
                                        onEditar()
                                    }
                                )
                            }
                            if (onAlternarFijado != null) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (post.fijado) "Desfijar publicación" else "Fijar arriba",
                                            color = TextPrimary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.PushPin, contentDescription = null, tint = MutedPurpleLight)
                                    },
                                    onClick = {
                                        menuAbierto = false
                                        onAlternarFijado()
                                    }
                                )
                            }
                            if (onAlternarPrivacidad != null) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (post.esAmigoDirecto) "Pasar a Anónimo" else "Pasar a Visible",
                                            color = TextPrimary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (post.esAmigoDirecto) Icons.Default.VisibilityOff else Icons.Default.Public,
                                            contentDescription = null,
                                            tint = MutedAccent
                                        )
                                    },
                                    onClick = {
                                        menuAbierto = false
                                        onAlternarPrivacidad()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = ErrorColor) },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = ErrorColor) },
                                onClick = {
                                    menuAbierto = false
                                    onEliminar()
                                }
                            )
                        }
                    }
                }
            }

            // Metadatos del post (Categoría, Audiencia, Ubicación y Estado)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (esVisible) MutedPurple.copy(alpha = 0.2f) else MutedGray.copy(alpha = 0.22f)
                ) {
                    Text(
                        text = post.categoria,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (esVisible) MutedPurpleLight else MutedAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                BadgeAudienciaPost(
                    visibilidad = post.audiencia,
                    permitidosCsv = post.audienciaPermitidos
                )

                if (!post.etiquetaLugar.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MutedAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = post.etiquetaLugar,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (esMio) {
                    Text(
                        text = if (esVisible) "• Tuyo" else "• Tuyo (Anónimo)",
                        fontSize = 11.sp,
                        color = MutedPurpleLight
                    )
                }
            }

            // Cuerpo del tweet / post
            if (post.contenido.isNotBlank()) {
                Text(
                    text = post.contenido,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 21.sp),
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))
            }

            // Multimedia adjunta (Foto o Video)
            val uri = post.mediaUri
            if (!uri.isNullOrBlank()) {
                when (post.mediaTipo) {
                    MediaTipoPost.IMAGEN -> {
                        FotoPreviewCard(
                            path = uri,
                            maxHeight = if (compacto) 190.dp else 250.dp,
                            onClick = { onAbrirImagen(uri) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    MediaTipoPost.VIDEO -> {
                        VideoPreviewCard(
                            path = uri,
                            maxHeight = if (compacto) 190.dp else 240.dp,
                            onClick = { onAbrirVideo(uri) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // Encuesta interactiva si la publicación incluye opciones
            val encuesta = remember(post.encuestaOpciones, post.encuestaVotos) {
                PollHelper.parsearEncuesta(post.encuestaOpciones, post.encuestaVotos)
            }
            if (encuesta != null && onVotarEncuesta != null) {
                EncuestaPostCard(
                    encuesta = encuesta,
                    votoUsuario = post.encuestaVotoUsuario,
                    onVotar = onVotarEncuesta
                )
                Spacer(Modifier.height(8.dp))
            }

            // Cita incrustada si es Quote Post
            if (!post.citadoContenido.isNullOrBlank()) {
                CitaPostPreviewBox(
                    autor = post.citadoAutor ?: "Publicación citada",
                    contenido = post.citadoContenido
                )
                Spacer(Modifier.height(8.dp))
            }

            // Barra de interacciones estilo Twitter: Responder, Repost/Citar, Like, Guardar, Compartir
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AccionPostBoton(
                    icono = Icons.AutoMirrored.Filled.Reply,
                    conteo = comentariosCount,
                    activo = false,
                    colorActivo = MutedPurpleLight,
                    onClick = onComentar
                )

                Box {
                    AccionPostBoton(
                        icono = Icons.Default.Repeat,
                        conteo = post.repostsCount,
                        activo = post.repostedByMe,
                        colorActivo = Color(0xFF81C784),
                        onClick = {
                            if (onCitar != null) {
                                menuRepostAbierto = true
                            } else {
                                onRepost()
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = menuRepostAbierto,
                        onDismissRequest = { menuRepostAbierto = false },
                        containerColor = DarkSurface
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (post.repostedByMe) "Deshacer repost" else "Repostear",
                                    color = TextPrimary
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Repeat, contentDescription = null, tint = Color(0xFF81C784))
                            },
                            onClick = {
                                menuRepostAbierto = false
                                onRepost()
                            }
                        )
                        if (onCitar != null) {
                            DropdownMenuItem(
                                text = { Text("Citar publicación", color = TextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = MutedPurpleLight)
                                },
                                onClick = {
                                    menuRepostAbierto = false
                                    onCitar()
                                }
                            )
                        }
                    }
                }

                AccionPostBoton(
                    icono = if (post.likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    conteo = post.likesCount,
                    activo = post.likedByMe,
                    colorActivo = ErrorColor,
                    onClick = onLike
                )

                IconButton(
                    onClick = onGuardar,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (post.guardado) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Guardar publicación",
                        tint = if (post.guardado) MutedPurpleLight else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onCompartir,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EncuestaPostCard(
    encuesta: com.example.redsocialnfc.util.EncuestaData,
    votoUsuario: Int,
    onVotar: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface.copy(alpha = 0.7f))
            .border(1.dp, DarkOutline, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        encuesta.opciones.forEachIndexed { idx, textoOpcion ->
            val votosOpcion = encuesta.votos.getOrElse(idx) { 0 }
            val porcentaje = if (encuesta.totalVotos > 0) (votosOpcion * 100) / encuesta.totalVotos else 0
            val elegida = votoUsuario == idx

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBackground)
                    .border(
                        width = 1.dp,
                        color = if (elegida) MutedPurpleLight else DarkOutline,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onVotar(idx) }
            ) {
                if (votoUsuario >= 0 && encuesta.totalVotos > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (porcentaje / 100f).coerceIn(0.03f, 1f))
                            .height(38.dp)
                            .background(
                                if (elegida) MutedPurple.copy(alpha = 0.35f) else MutedGray.copy(alpha = 0.22f)
                            )
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (elegida) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MutedPurpleLight,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = textoOpcion,
                            fontSize = 13.sp,
                            fontWeight = if (elegida) FontWeight.Bold else FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (votoUsuario >= 0) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "$porcentaje%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (elegida) MutedPurpleLight else TextSecondary
                        )
                    }
                }
            }
        }

        Text(
            text = if (encuesta.totalVotos == 1) "1 voto · Toca una opción para votar" else "${encuesta.totalVotos} votos · Toca una opción para votar",
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun CitaPostPreviewBox(
    autor: String,
    contenido: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, DarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = autor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MutedPurpleLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = contenido,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AccionPostBoton(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    conteo: Int,
    activo: Boolean,
    colorActivo: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (activo) colorActivo else TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        if (conteo > 0) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = "$conteo",
                fontSize = 12.sp,
                fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                color = if (activo) colorActivo else TextSecondary
            )
        }
    }
}

@Composable
private fun BarraCategoriasChips(
    categorias: List<Categoria>,
    categoriaSeleccionada: String,
    miUserId: Int,
    incluirTodas: Boolean,
    onSeleccionar: (String) -> Unit,
    onSolicitarCrear: () -> Unit,
    onSolicitarBorrar: (Categoria) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (incluirTodas) {
            item(key = "cat_todas") {
                val sel = categoriaSeleccionada == "Todas"
                FilterChip(
                    selected = sel,
                    onClick = { onSeleccionar("Todas") },
                    label = { Text("Todas", fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MutedPurple,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = sel,
                        borderColor = if (sel) MutedPurple else DarkOutline
                    )
                )
            }
        }

        item(key = "cat_principal") {
            val sel = categoriaSeleccionada == CATEGORIA_PRINCIPAL
            FilterChip(
                selected = sel,
                onClick = { onSeleccionar(CATEGORIA_PRINCIPAL) },
                label = {
                    Text(
                        CATEGORIA_PRINCIPAL,
                        fontSize = 12.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MutedPurple,
                    selectedLabelColor = Color.White,
                    containerColor = DarkSurface,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = sel,
                    borderColor = if (sel) MutedPurple else DarkOutline
                )
            )
        }

        items(categorias, key = { it.id }) { cat ->
            val sel = categoriaSeleccionada == cat.nombre
            val esDueno = cat.autorId == miUserId
            FilterChip(
                selected = sel,
                onClick = { onSeleccionar(cat.nombre) },
                leadingIcon = if (cat.visibilidad != VisibilidadMuro.TODOS) {
                    {
                        Icon(
                            imageVector = iconoPorVisibilidad(cat.visibilidad),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else null,
                label = {
                    Text(
                        cat.nombre,
                        fontSize = 12.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                    )
                },
                trailingIcon = if (esDueno) {
                    {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Borrar categoría ${cat.nombre}",
                            modifier = Modifier
                                .size(15.dp)
                                .clickable { onSolicitarBorrar(cat) }
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MutedPurple,
                    selectedLabelColor = Color.White,
                    containerColor = DarkSurface,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = sel,
                    borderColor = if (sel) MutedPurple else DarkOutline
                )
            )
        }

        item(key = "cat_crear") {
            FilterChip(
                selected = false,
                onClick = onSolicitarCrear,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MutedPurpleLight,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = { Text("Crear", fontSize = 12.sp, color = MutedPurpleLight, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = SubtleGrayPurple,
                    labelColor = MutedPurpleLight
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = MutedPurple.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
private fun CrearCategoriaDialog(
    amigos: List<Amigo>,
    onDismiss: () -> Unit,
    onCrear: (String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var visibilidad by remember { mutableStateOf(VisibilidadMuro.TODOS) }
    var permitidos by remember { mutableStateOf(emptySet<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                "Nueva Categoría",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Solo tú como creador podrás eliminarla y definir quién puede verla.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { if (it.length <= 24) nombre = it },
                    label = { Text("Nombre de categoría") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedPurple,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "¿Quién puede ver este muro?",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MutedPurpleLight
                )
                SelectorAudienciaMuro(
                    visibilidad = visibilidad,
                    permitidos = permitidos,
                    amigos = amigos,
                    onCambioVisibilidad = { visibilidad = it },
                    onToggleAmigo = { handle ->
                        permitidos = if (handle in permitidos) permitidos - handle else permitidos + handle
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val permitidosCsv = if (visibilidad == VisibilidadMuro.SELECCIONADOS) {
                        VisibilidadMuro.serializarHandles(permitidos)
                    } else ""
                    onCrear(nombre, visibilidad, permitidosCsv)
                },
                enabled = nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MutedPurple)
            ) {
                Text("Crear", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun CrearPostTwitterSheet(
    miNombre: String,
    miHandle: String,
    miAnonHandle: String,
    miAvatarUri: String?,
    miUserId: Int,
    categorias: List<Categoria>,
    amigos: List<Amigo>,
    categoriaInicial: String = CATEGORIA_PRINCIPAL,
    modoAnonimoInicial: Boolean,
    postEdicion: Publicacion? = null,
    postCitado: Publicacion? = null,
    onSolicitarCrearCategoria: () -> Unit,
    onSolicitarBorrarCategoria: (Categoria) -> Unit,
    onPublicar: (
        contenido: String,
        publicarAnonimo: Boolean,
        categoria: String,
        lugar: String?,
        mediaTipo: String,
        mediaUri: String?,
        encuestaOpcionesJson: String,
        encuestaVotosJson: String,
        audiencia: String,
        audienciaPermitidos: String
    ) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun resolverAudienciaDeMuro(catNombre: String): Pair<String, Set<String>> {
        if (catNombre == CATEGORIA_PRINCIPAL) {
            return VisibilidadMuro.TODOS to emptySet()
        }
        val catObj = categorias.find { it.nombre == catNombre }
        return if (catObj != null) {
            catObj.visibilidad to VisibilidadMuro.parsearHandles(catObj.permitidosHandles).toSet()
        } else {
            VisibilidadMuro.TODOS to emptySet()
        }
    }

    var texto by remember(postEdicion?.id, postCitado?.id) { mutableStateOf(postEdicion?.contenido ?: "") }
    var modoAnonimo by remember(postEdicion?.id, postCitado?.id) { mutableStateOf(modoAnonimoInicial) }
    var categoriaSeleccionada by remember(postEdicion?.id, postCitado?.id) {
        mutableStateOf(postEdicion?.categoria ?: categoriaInicial)
    }
    var audiencia by remember(postEdicion?.id, postCitado?.id) {
        mutableStateOf(postEdicion?.audiencia ?: resolverAudienciaDeMuro(categoriaSeleccionada).first)
    }
    var audienciaPermitidos by remember(postEdicion?.id, postCitado?.id) {
        mutableStateOf(
            if (postEdicion != null) {
                VisibilidadMuro.parsearHandles(postEdicion.audienciaPermitidos).toSet()
            } else {
                resolverAudienciaDeMuro(categoriaSeleccionada).second
            }
        )
    }
    var audienciaPersonalizadaManualmente by remember(postEdicion?.id, postCitado?.id) {
        mutableStateOf(postEdicion != null)
    }
    var mostrarSelectorAudienciaPost by remember(postEdicion?.id, postCitado?.id) { mutableStateOf(false) }

    var lugarTexto by remember(postEdicion?.id) { mutableStateOf(postEdicion?.etiquetaLugar ?: "") }
    var mostrarCampoLugar by remember(postEdicion?.id) { mutableStateOf(!postEdicion?.etiquetaLugar.isNullOrBlank()) }

    var mediaTipo by remember(postEdicion?.id) { mutableStateOf(postEdicion?.mediaTipo ?: MediaTipoPost.NINGUNO) }
    var mediaPath by remember(postEdicion?.id) { mutableStateOf(postEdicion?.mediaUri) }

    val encuestaInicial = remember(postEdicion?.id) {
        postEdicion?.let { PollHelper.parsearEncuesta(it.encuestaOpciones, it.encuestaVotos) }
    }
    var mostrarEncuesta by remember(postEdicion?.id) { mutableStateOf(encuestaInicial != null) }
    val opcionesEncuesta = remember(postEdicion?.id) {
        mutableStateListOf<String>().apply {
            if (encuestaInicial != null) addAll(encuestaInicial.opciones)
            else {
                add("")
                add("")
            }
        }
    }

    val categoriaActiva = if (
        categoriaSeleccionada != CATEGORIA_PRINCIPAL &&
        categorias.none { it.nombre == categoriaSeleccionada }
    ) CATEGORIA_PRINCIPAL else categoriaSeleccionada

    val maxCaracteres = 280

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = MediaStorageHelper.copiarUriAAlmacenamientoLocal(context, uri, prefijo = "post_img")
                if (res != null) {
                    mediaTipo = MediaTipoPost.IMAGEN
                    mediaPath = res.localPath
                }
            }
        }
    }

    val lanzarCamara = rememberCameraPhotoLauncher(prefijo = "post_cam") { res ->
        mediaTipo = MediaTipoPost.IMAGEN
        mediaPath = res.localPath
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = MediaStorageHelper.copiarUriAAlmacenamientoLocal(
                    context, uri, prefijo = "post_vid", extraerDuracion = true
                )
                if (res != null) {
                    mediaTipo = MediaTipoPost.VIDEO
                    mediaPath = res.localPath
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        // Cabecera estilo Twitter: Cancelar / Publicar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = TextSecondary)
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (modoAnonimo) MutedGray.copy(alpha = 0.25f) else MutedPurple.copy(alpha = 0.25f),
                modifier = Modifier.clickable { modoAnonimo = !modoAnonimo }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = if (modoAnonimo) Icons.Default.VisibilityOff else Icons.Default.Public,
                        contentDescription = null,
                        tint = if (modoAnonimo) MutedAccent else MutedPurpleLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (modoAnonimo) miAnonHandle else miHandle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (modoAnonimo) MutedAccent else MutedPurpleLight
                    )
                }
            }

            val opcionesValidas = if (mostrarEncuesta) {
                opcionesEncuesta.map { it.trim() }.filter { it.isNotEmpty() }
            } else emptyList()
            val encuestaValida = mostrarEncuesta && opcionesValidas.size >= 2
            val puedePublicar = (texto.isNotBlank() || mediaPath != null || encuestaValida || postCitado != null) &&
                (!mostrarEncuesta || encuestaValida)

            Button(
                onClick = {
                    if (puedePublicar) {
                        val (opcJson, votosJson) = if (encuestaValida) {
                            if (encuestaInicial != null && opcionesValidas == encuestaInicial.opciones) {
                                postEdicion!!.encuestaOpciones to postEdicion.encuestaVotos
                            } else {
                                PollHelper.serializarEncuesta(opcionesValidas)
                            }
                        } else {
                            "" to ""
                        }
                        val audienciaPermitidosCsv = if (audiencia == VisibilidadMuro.SELECCIONADOS) {
                            VisibilidadMuro.serializarHandles(audienciaPermitidos)
                        } else ""
                        onPublicar(
                            texto.trim(),
                            modoAnonimo,
                            categoriaActiva,
                            if (mostrarCampoLugar) lugarTexto.trim() else null,
                            mediaTipo,
                            mediaPath,
                            opcJson,
                            votosJson,
                            audiencia,
                            audienciaPermitidosCsv
                        )
                    }
                },
                enabled = puedePublicar,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MutedPurple,
                    contentColor = Color.White
                )
            ) {
                Text(if (postEdicion != null) "Guardar" else "Publicar", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Cuerpo compositor con avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            AvatarUsuario(
                nombreVisible = if (modoAnonimo) "Anónimo" else miNombre,
                avatarUri = miAvatarUri,
                esAnonimo = modoAnonimo,
                size = 40.dp
            )
            Spacer(Modifier.width(10.dp))
            OutlinedTextField(
                value = texto,
                onValueChange = {
                    if (it.length <= maxCaracteres) texto = it
                },
                placeholder = {
                    Text(
                        if (modoAnonimo) "¿Qué quieres decir anónimamente?" else "¿Qué está pasando en Local?",
                        color = TextSecondary
                    )
                },
                minLines = 3,
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
                            color = if (texto.length >= maxCaracteres) ErrorColor else TextSecondary
                        )
                    }
                }
            )
        }

        // Vista previa de foto o video adjunto
        val path = mediaPath
        if (path != null) {
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                when (mediaTipo) {
                    MediaTipoPost.IMAGEN -> FotoPreviewCard(path = path, maxHeight = 200.dp, onClick = {})
                    MediaTipoPost.VIDEO -> VideoPreviewCard(path = path, maxHeight = 190.dp, onClick = {})
                }
                IconButton(
                    onClick = {
                        mediaTipo = MediaTipoPost.NINGUNO
                        mediaPath = null
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.72f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Quitar adjunto", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Vista previa si está citando otro post
        if (postCitado != null) {
            Spacer(Modifier.height(8.dp))
            val autorCita = if (postCitado.esAmigoDirecto) {
                "${postCitado.autorNombre.ifBlank { postCitado.autorHandle.removePrefix("@") }} (${postCitado.autorHandle})"
            } else {
                "Círculo Anónimo (${postCitado.autorAnonimo})"
            }
            CitaPostPreviewBox(
                autor = autorCita,
                contenido = postCitado.contenido.ifBlank { "[Publicación multimedia]" }
            )
        }

        // Constructor opcional de encuesta rápida (2 a 4 opciones)
        AnimatedVisibility(visible = mostrarEncuesta) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .border(1.dp, MutedPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Encuesta (mín. 2 opciones)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MutedPurpleLight
                    )
                    TextButton(onClick = { mostrarEncuesta = false }) {
                        Text("Quitar", color = ErrorColor, fontSize = 12.sp)
                    }
                }

                opcionesEncuesta.forEachIndexed { idx, valor ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = valor,
                            onValueChange = { if (it.length <= 45) opcionesEncuesta[idx] = it },
                            label = { Text("Opción ${idx + 1}") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MutedPurple,
                                unfocusedBorderColor = DarkOutline,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (opcionesEncuesta.size > 2) {
                            IconButton(onClick = { opcionesEncuesta.removeAt(idx) }) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar opción", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (opcionesEncuesta.size < 4) {
                    TextButton(onClick = { opcionesEncuesta.add("") }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MutedPurpleLight, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar opción (${opcionesEncuesta.size}/4)", color = MutedPurpleLight, fontSize = 12.sp)
                    }
                }
            }
        }

        // Campo opcional de lugar
        AnimatedVisibility(visible = mostrarCampoLugar) {
            Column {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = lugarTexto,
                    onValueChange = { if (it.length <= 40) lugarTexto = it },
                    label = { Text("Zona / Lugar (ej. Biblioteca, FIM)") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MutedPurpleLight)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedPurple,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Selector explícito: Visible vs Anónimo (sin confusiones ni recortes)
        Text(
            text = "¿Cómo se verá esta publicación?",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (!modoAnonimo) MutedPurple.copy(alpha = 0.25f) else DarkBackground,
                border = BorderStroke(1.dp, if (!modoAnonimo) MutedPurple else DarkOutline),
                modifier = Modifier
                    .weight(1f)
                    .clickable { modoAnonimo = false }
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = if (!modoAnonimo) MutedPurpleLight else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Visible",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!modoAnonimo) TextPrimary else TextSecondary
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = miHandle,
                        fontSize = 11.sp,
                        color = if (!modoAnonimo) MutedPurpleLight else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (modoAnonimo) MutedGray.copy(alpha = 0.30f) else DarkBackground,
                border = BorderStroke(1.dp, if (modoAnonimo) MutedAccent else DarkOutline),
                modifier = Modifier
                    .weight(1f)
                    .clickable { modoAnonimo = true }
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (modoAnonimo) MutedAccent else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Anónimo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (modoAnonimo) TextPrimary else TextSecondary
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = miAnonHandle,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (modoAnonimo) MutedAccent else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (modoAnonimo) {
                "Tu nombre real queda oculto; otros verán $miAnonHandle."
            } else {
                "Se mostrará como $miNombre ($miHandle)."
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (modoAnonimo) MutedAccent else MutedPurpleLight
        )

        Spacer(Modifier.height(10.dp))

        // Selector de Categoría: solo Principal + creadas por usuarios + Crear
        Text(
            text = "Categoría / Muro:",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        BarraCategoriasChips(
            categorias = categorias,
            categoriaSeleccionada = categoriaActiva,
            miUserId = miUserId,
            incluirTodas = false,
            onSeleccionar = { nuevaCat ->
                categoriaSeleccionada = nuevaCat
                if (!audienciaPersonalizadaManualmente) {
                    val (visMuro, permMuro) = resolverAudienciaDeMuro(nuevaCat)
                    audiencia = visMuro
                    audienciaPermitidos = permMuro
                }
            },
            onSolicitarCrear = onSolicitarCrearCategoria,
            onSolicitarBorrar = onSolicitarBorrarCategoria,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(Modifier.height(6.dp))

        // Selector de quién puede ver esta publicación (hereda del muro o se personaliza)
        val resumenAudienciaPost = VisibilidadMuro.formatearResumen(
            audiencia,
            if (audiencia == VisibilidadMuro.SELECCIONADOS) {
                VisibilidadMuro.serializarHandles(audienciaPermitidos)
            } else ""
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkBackground,
            border = BorderStroke(
                1.dp,
                if (audiencia != VisibilidadMuro.TODOS) MutedPurple.copy(alpha = 0.65f) else DarkOutline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { mostrarSelectorAudienciaPost = !mostrarSelectorAudienciaPost }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = iconoPorVisibilidad(audiencia),
                        contentDescription = null,
                        tint = MutedPurpleLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "¿Quién puede ver esta publicación?",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = resumenAudienciaPost,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = if (mostrarSelectorAudienciaPost) "Listo" else "Cambiar",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedPurpleLight
                )
            }
        }

        AnimatedVisibility(visible = mostrarSelectorAudienciaPost) {
            SelectorAudienciaMuro(
                visibilidad = audiencia,
                permitidos = audienciaPermitidos,
                amigos = amigos,
                onCambioVisibilidad = { nuevaVis ->
                    audiencia = nuevaVis
                    audienciaPersonalizadaManualmente = true
                },
                onToggleAmigo = { handle ->
                    audienciaPermitidos = if (handle in audienciaPermitidos) {
                        audienciaPermitidos - handle
                    } else {
                        audienciaPermitidos + handle
                    }
                    audienciaPersonalizadaManualmente = true
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = DarkOutline.copy(alpha = 0.45f))
        Spacer(Modifier.height(6.dp))

        // Barra inferior del compositor: Cámara, Galería, Video y Ubicación
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = lanzarCamara) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Tomar foto con cámara",
                    tint = MutedPurpleLight
                )
            }

            IconButton(
                onClick = {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Adjuntar foto de galería",
                    tint = MutedPurpleLight
                )
            }

            IconButton(
                onClick = {
                    pickVideoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Adjuntar video",
                    tint = MutedPurpleLight
                )
            }

            IconButton(
                onClick = { mostrarCampoLugar = !mostrarCampoLugar }
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Agregar zona",
                    tint = if (mostrarCampoLugar) MutedPurpleLight else TextSecondary
                )
            }

            IconButton(
                onClick = { mostrarEncuesta = !mostrarEncuesta }
            ) {
                Icon(
                    imageVector = Icons.Default.Poll,
                    contentDescription = "Agregar encuesta",
                    tint = if (mostrarEncuesta) MutedPurpleLight else TextSecondary
                )
            }

            if (mostrarCampoLugar) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Zona activa",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedPurpleLight
                )
            }
        }
    }
}

@Composable
internal fun ComentariosSheetContent(
    post: Publicacion,
    comentarios: List<Comentario>,
    miUserId: Int,
    miHandle: String,
    miAnonHandle: String,
    defaultAnon: Boolean,
    onEnviarComentario: (String, Boolean) -> Unit,
    onEliminarComentario: (Comentario) -> Unit
) {
    var textoRespuesta by remember { mutableStateOf("") }
    var responderAnonimo by remember { mutableStateOf(defaultAnon) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Respuestas (${comentarios.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Text(
            text = "Publicación de ${if (post.esAmigoDirecto) post.autorHandle else post.autorAnonimo}",
            style = MaterialTheme.typography.labelSmall,
            color = MutedPurpleLight
        )
        if (post.contenido.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "\"${post.contenido}\"",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = DarkOutline.copy(alpha = 0.45f))

        if (comentarios.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sé el primero en responder a esta publicación",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(comentarios, key = { it.id }) { c ->
                    val autorLabel = if (c.esAnonimo) c.autorAnonimo else c.autorHandle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkBackground)
                            .border(1.dp, DarkOutline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$autorLabel · ${MediaStorageHelper.formatearTiempoRelativo(c.timestamp)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = if (c.esAnonimo) FontFamily.Monospace else FontFamily.Default
                                ),
                                color = if (c.esAnonimo) MutedAccent else MutedPurpleLight
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = c.contenido,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                        if (c.autorId == miUserId) {
                            IconButton(
                                onClick = { onEliminarComentario(c) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Borrar comentario",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Toggle rápido de identidad al responder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Responder como ${if (responderAnonimo) miAnonHandle else miHandle}",
                style = MaterialTheme.typography.labelSmall,
                color = if (responderAnonimo) MutedAccent else MutedPurpleLight
            )
            TextButton(onClick = { responderAnonimo = !responderAnonimo }) {
                Text("Cambiar", fontSize = 12.sp, color = MutedPurpleLight)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textoRespuesta,
                onValueChange = { if (it.length <= 200) textoRespuesta = it },
                placeholder = { Text("Escribe tu respuesta...", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MutedPurple,
                    unfocusedBorderColor = DarkOutline,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textoRespuesta.isNotBlank()) {
                        onEnviarComentario(textoRespuesta.trim(), responderAnonimo)
                        textoRespuesta = ""
                    }
                },
                enabled = textoRespuesta.isNotBlank(),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (textoRespuesta.isNotBlank()) MutedPurple else DarkSurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar respuesta",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
