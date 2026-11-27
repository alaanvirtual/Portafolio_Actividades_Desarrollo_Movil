package com.example.redsocialnfc.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.redsocialnfc.data.Amigo
import com.example.redsocialnfc.data.Mensaje
import com.example.redsocialnfc.data.Publicacion
import com.example.redsocialnfc.data.VisibilidadMuro
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

data class PeerP2p(
    val handle: String,
    val nombre: String,
    val nfcUuid: String,
    val ip: String,
    val port: Int,
    val lastSeenMs: Long = System.currentTimeMillis()
)

object LocalP2pSyncManager {
    private const val TAG = "LocalP2pSync"
    private const val SERVICE_TYPE = "_redsocialnfc._tcp."
    private const val UDP_BEACON_PORT = 45678

    private var appContext: Context? = null
    private var scope: CoroutineScope? = null
    private var serverSocket: ServerSocket? = null
    private var udpSocket: DatagramSocket? = null
    private var localTcpPort: Int = 0

    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var beaconJob: Job? = null

    private val peers = ConcurrentHashMap<String, PeerP2p>()
    private val _peersConectados = MutableStateFlow<List<PeerP2p>>(emptyList())
    val peersConectados: StateFlow<List<PeerP2p>> = _peersConectados.asStateFlow()

    @Volatile
    private var activo = false

    fun iniciar(context: Context) {
        if (activo) return
        val ctx = context.applicationContext
        appContext = ctx
        activo = true
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope = newScope

        newScope.launch {
            iniciarServidorTcp()
            registrarServicioNsd(ctx)
            iniciarDescubrimientoNsd(ctx)
            iniciarUdpBeaconListener()
            iniciarUdpBeaconSender()
        }
    }

    fun detener() {
        activo = false
        beaconJob?.cancel()
        beaconJob = null
        try {
            registrationListener?.let { nsdManager?.unregisterService(it) }
        } catch (_: Exception) {}
        try {
            discoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
        } catch (_: Exception) {}
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        try {
            udpSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        udpSocket = null
        registrationListener = null
        discoveryListener = null
        peers.clear()
        _peersConectados.value = emptyList()
    }

    private fun iniciarServidorTcp() {
        try {
            val server = ServerSocket(0)
            serverSocket = server
            localTcpPort = server.localPort
            Log.d(TAG, "Servidor TCP P2P escuchando en puerto $localTcpPort")

            scope?.launch {
                while (activo && !server.isClosed) {
                    try {
                        val client = server.accept()
                        launch { manejarClienteEntrante(client) }
                    } catch (e: Exception) {
                        if (activo) Log.e(TAG, "Error accept TCP: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo iniciar ServerSocket: ${e.message}")
        }
    }

    private suspend fun manejarClienteEntrante(socket: Socket) {
        try {
            socket.soTimeout = 10_000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
            val linea = reader.readLine()
            val remoteIp = socket.inetAddress?.hostAddress ?: ""
            if (!linea.isNullOrBlank()) {
                procesarPaqueteJson(linea, remoteIp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error leyendo paquete entrante: ${e.message}")
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun registrarServicioNsd(context: Context) {
        val manager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
        nsdManager = manager
        val session = SessionManager(context)
        val cleanHandle = session.getRealHandle().replace("@", "").replace(Regex("[^A-Za-z0-9_]"), "")
        val serviceName = "Local_${cleanHandle.ifBlank { "User" }}_$localTcpPort"

        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = serviceName
            this.serviceType = SERVICE_TYPE
            this.port = localTcpPort
        }

        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                Log.d(TAG, "Servicio NSD registrado: ${info.serviceName}")
            }
            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "NSD registro falló: $errorCode")
            }
            override fun onServiceUnregistered(info: NsdServiceInfo) {}
            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {}
        }
        registrationListener = listener
        try {
            manager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción registrando NSD: ${e.message}")
        }
    }

    private fun iniciarDescubrimientoNsd(context: Context) {
        val manager = nsdManager ?: return
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Descubrimiento NSD iniciado")
            }
            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType != SERVICE_TYPE) return
                if (service.serviceName.endsWith("_$localTcpPort")) return
                try {
                    manager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {}
                        override fun onServiceResolved(info: NsdServiceInfo) {
                            val hostIp = info.host?.hostAddress ?: return
                            val port = info.port
                            if (port <= 0 || port == localTcpPort) return
                            enviarHelloAPeer(hostIp, port)
                        }
                    })
                } catch (_: Exception) {}
            }
            override fun onServiceLost(service: NsdServiceInfo) {}
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
        }
        discoveryListener = listener
        try {
            manager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción descubriendo NSD: ${e.message}")
        }
    }

    private fun iniciarUdpBeaconListener() {
        scope?.launch {
            try {
                val sock = DatagramSocket(null).apply {
                    reuseAddress = true
                    broadcast = true
                    bind(InetSocketAddress(UDP_BEACON_PORT))
                }
                udpSocket = sock
                val buffer = ByteArray(2048)
                while (activo && !sock.isClosed) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    sock.receive(packet)
                    val msg = String(packet.data, 0, packet.length, Charsets.UTF_8)
                    val senderIp = packet.address?.hostAddress ?: continue
                    procesarPaqueteJson(msg, senderIp)
                }
            } catch (e: Exception) {
                if (activo) Log.e(TAG, "UDP listener error: ${e.message}")
            }
        }
    }

    private fun iniciarUdpBeaconSender() {
        beaconJob = scope?.launch {
            while (isActive && activo) {
                try {
                    val ctx = appContext ?: break
                    val session = SessionManager(ctx)
                    val miHandle = session.getRealHandle()
                    if (miHandle.isNotBlank() && localTcpPort > 0) {
                        val json = JSONObject().apply {
                            put("type", "HELLO")
                            put("handle", miHandle)
                            put("nombre", session.getDisplayName())
                            put("nfcUuid", session.obtenerOGenerarDeviceNfcUuid())
                            put("port", localTcpPort)
                        }.toString()
                        val bytes = json.toByteArray(Charsets.UTF_8)
                        val sock = DatagramSocket().apply { broadcast = true }
                        val packet = DatagramPacket(
                            bytes,
                            bytes.size,
                            InetAddress.getByName("255.255.255.255"),
                            UDP_BEACON_PORT
                        )
                        sock.send(packet)
                        sock.close()
                    }
                } catch (_: Exception) {}
                delay(4000)
            }
        }
    }

    private fun enviarHelloAPeer(ip: String, port: Int) {
        val ctx = appContext ?: return
        val session = SessionManager(ctx)
        val miHandle = session.getRealHandle()
        if (miHandle.isBlank()) return
        val json = JSONObject().apply {
            put("type", "HELLO")
            put("handle", miHandle)
            put("nombre", session.getDisplayName())
            put("nfcUuid", session.obtenerOGenerarDeviceNfcUuid())
            put("port", localTcpPort)
        }.toString()
        enviarTcpDirecto(ip, port, json)
    }

    private suspend fun procesarPaqueteJson(jsonStr: String, senderIp: String) {
        val ctx = appContext ?: return
        val session = SessionManager(ctx)
        val miHandle = session.getRealHandle()
        val db = AppDatabase.getDatabase(ctx)

        try {
            val obj = JSONObject(jsonStr)
            val type = obj.optString("type", "")

            when (type) {
                "HELLO" -> {
                    val handle = obj.optString("handle", "").trim()
                    val nombre = obj.optString("nombre", "").trim()
                    val nfcUuid = obj.optString("nfcUuid", "").trim()
                    val port = obj.optInt("port", 0)
                    if (handle.isBlank() || port <= 0) return
                    if (handle.equals(miHandle, ignoreCase = true) && port == localTcpPort) return

                    val esNuevoPeer = !peers.containsKey(handle)
                    peers[handle] = PeerP2p(handle, nombre, nfcUuid, senderIp, port)
                    _peersConectados.value = peers.values.toList()

                    if (esNuevoPeer) {
                        // Responder HELLO para handshake TCP bidireccional y sincronizar amigos/posts recientes
                        enviarHelloAPeer(senderIp, port)
                        sincronizarConPeer(ctx, senderIp, port, handle)
                    }
                }

                "FRIEND_HANDSHAKE" -> {
                    val handle = obj.optString("handle", "").trim()
                    val nombre = obj.optString("nombre", "").trim()
                    val nfcUuid = obj.optString("nfcUuid", "").trim()
                    val targetHandle = obj.optString("targetHandle", "").trim()
                    if (handle.isBlank() || handle.equals(miHandle, ignoreCase = true)) return
                    if (targetHandle.isNotBlank() && !targetHandle.equals(miHandle, ignoreCase = true)) return

                    db.amigoDao().insertar(
                        Amigo(
                            handle = handle,
                            nombre = nombre.ifBlank { handle.removePrefix("@") },
                            nfcUuid = nfcUuid.ifBlank { "p2p-$handle" }
                        )
                    )
                }

                "NEW_MESSAGE" -> {
                    val remitente = obj.optString("remitenteHandle", "").trim()
                    val destinatario = obj.optString("destinatarioHandle", "").trim()
                    val texto = obj.optString("texto", "")
                    val mediaTipo = obj.optString("mediaTipo", "TEXTO")
                    val timestamp = obj.optLong("timestamp", System.currentTimeMillis())

                    if (remitente.isBlank() || remitente.equals(miHandle, ignoreCase = true)) return
                    if (!destinatario.equals(miHandle, ignoreCase = true)) return

                    if (db.mensajeDao().existeMensaje(remitente, timestamp) == 0) {
                        // Asegurar que el remitente aparezca en la lista de amigos si no estaba
                        if (db.amigoDao().obtenerPorHandle(remitente) == null) {
                            val nombreRem = obj.optString("remitenteNombre", remitente.removePrefix("@"))
                            db.amigoDao().insertar(
                                Amigo(handle = remitente, nombre = nombreRem, nfcUuid = "p2p-$remitente")
                            )
                        }
                        db.mensajeDao().insertar(
                            Mensaje(
                                remitenteHandle = remitente,
                                destinatarioHandle = miHandle,
                                texto = texto,
                                mediaTipo = mediaTipo,
                                leido = false,
                                timestamp = timestamp
                            )
                        )
                    }
                }

                "NEW_POST" -> {
                    val autorHandle = obj.optString("autorHandle", "").trim()
                    val timestamp = obj.optLong("timestamp", 0L)
                    if (autorHandle.isBlank() || autorHandle.equals(miHandle, ignoreCase = true)) return
                    if (timestamp <= 0L) return

                    if (db.publicacionDao().existePublicacion(autorHandle, timestamp) == 0) {
                        db.publicacionDao().insertar(
                            Publicacion(
                                autorId = -1, // Autor remoto para no confundir con el usuario local
                                autorNombre = obj.optString("autorNombre", autorHandle.removePrefix("@")),
                                autorHandle = autorHandle,
                                autorAvatarUri = null,
                                autorAnonimo = obj.optString("autorAnonimo", "@Anonimo_P2P"),
                                contenido = obj.optString("contenido", ""),
                                categoria = obj.optString("categoria", "Principal"),
                                etiquetaLugar = obj.optString("etiquetaLugar", "").ifBlank { null },
                                esAmigoDirecto = obj.optBoolean("esAmigoDirecto", true),
                                audiencia = obj.optString("audiencia", VisibilidadMuro.TODOS),
                                audienciaPermitidos = obj.optString("audienciaPermitidos", ""),
                                encuestaOpciones = obj.optString("encuestaOpciones", ""),
                                encuestaVotos = obj.optString("encuestaVotos", ""),
                                citadoAutor = obj.optString("citadoAutor", "").ifBlank { null },
                                citadoContenido = obj.optString("citadoContenido", "").ifBlank { null },
                                timestamp = timestamp
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando paquete JSON: ${e.message}")
        }
    }

    private suspend fun sincronizarConPeer(context: Context, ip: String, port: Int, peerHandle: String) {
        val db = AppDatabase.getDatabase(context)
        val session = SessionManager(context)
        val miHandle = session.getRealHandle()
        val miUserId = session.getUserId()

        // Si ya lo tengo como amigo (ej. por NFC), enviarle mi FRIEND_HANDSHAKE para reciprocidad
        val amigo = db.amigoDao().obtenerPorHandle(peerHandle)
        if (amigo != null) {
            val handshakeJson = JSONObject().apply {
                put("type", "FRIEND_HANDSHAKE")
                put("handle", miHandle)
                put("nombre", session.getDisplayName())
                put("nfcUuid", session.obtenerOGenerarDeviceNfcUuid())
                put("targetHandle", peerHandle)
            }.toString()
            enviarTcpDirecto(ip, port, handshakeJson)
        }

        // Enviar publicaciones propias no privadas
        val posts = db.publicacionDao().obtenerTodas().firstOrNull() ?: emptyList()
        posts.filter { it.autorId == miUserId && it.audiencia != VisibilidadMuro.PRIVADO }
            .take(15)
            .forEach { post ->
                enviarTcpDirecto(ip, port, serializarPost(post))
            }
    }

    fun enviarHandshakeAmigo(targetHandle: String) {
        val ctx = appContext ?: return
        val session = SessionManager(ctx)
        val json = JSONObject().apply {
            put("type", "FRIEND_HANDSHAKE")
            put("handle", session.getRealHandle())
            put("nombre", session.getDisplayName())
            put("nfcUuid", session.obtenerOGenerarDeviceNfcUuid())
            put("targetHandle", targetHandle)
        }.toString()

        scope?.launch {
            val peer = peers.values.firstOrNull { it.handle.equals(targetHandle, ignoreCase = true) }
            if (peer != null) {
                enviarTcpDirecto(peer.ip, peer.port, json)
            } else {
                peers.values.forEach { p -> enviarTcpDirecto(p.ip, p.port, json) }
            }
        }
    }

    fun enviarMensaje(mensaje: Mensaje) {
        val ctx = appContext ?: return
        val session = SessionManager(ctx)
        val textoMostrado = if (mensaje.texto.isNotBlank()) {
            mensaje.texto
        } else {
            "[Archivo ${mensaje.mediaTipo}: ${mensaje.mediaNombre ?: "adjunto"}]"
        }
        val json = JSONObject().apply {
            put("type", "NEW_MESSAGE")
            put("remitenteHandle", mensaje.remitenteHandle)
            put("remitenteNombre", session.getDisplayName())
            put("destinatarioHandle", mensaje.destinatarioHandle)
            put("texto", textoMostrado)
            put("mediaTipo", "TEXTO")
            put("timestamp", mensaje.timestamp)
        }.toString()

        scope?.launch {
            val peer = peers.values.firstOrNull { it.handle.equals(mensaje.destinatarioHandle, ignoreCase = true) }
            if (peer != null) {
                enviarTcpDirecto(peer.ip, peer.port, json)
            } else {
                peers.values.forEach { p -> enviarTcpDirecto(p.ip, p.port, json) }
            }
        }
    }

    fun difundirPublicacion(publicacion: Publicacion) {
        if (publicacion.audiencia == VisibilidadMuro.PRIVADO) return
        val json = serializarPost(publicacion)
        scope?.launch {
            peers.values.forEach { p ->
                enviarTcpDirecto(p.ip, p.port, json)
            }
        }
    }

    private fun serializarPost(post: Publicacion): String {
        return JSONObject().apply {
            put("type", "NEW_POST")
            put("autorNombre", post.autorNombre)
            put("autorHandle", post.autorHandle)
            put("autorAnonimo", post.autorAnonimo)
            put("contenido", post.contenido)
            put("categoria", post.categoria)
            put("etiquetaLugar", post.etiquetaLugar ?: "")
            put("esAmigoDirecto", post.esAmigoDirecto)
            put("audiencia", post.audiencia)
            put("audienciaPermitidos", post.audienciaPermitidos)
            put("encuestaOpciones", post.encuestaOpciones)
            put("encuestaVotos", post.encuestaVotos)
            put("citadoAutor", post.citadoAutor ?: "")
            put("citadoContenido", post.citadoContenido ?: "")
            put("timestamp", post.timestamp)
        }.toString()
    }

    private fun enviarTcpDirecto(ip: String, port: Int, jsonLine: String) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), 3000)
                val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8), true)
                writer.println(jsonLine)
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo enviar TCP a $ip:$port -> ${e.message}")
        }
    }
}
