package com.zeddihub.tv.localsend

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Environment
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Compatible-mode receiver for the LocalSend protocol (v2). Implements
 * just the two endpoints needed to receive a file from a sender that
 * already knows our address:
 *
 *   POST /api/localsend/v2/prepare-upload  — sender announces incoming files
 *   POST /api/localsend/v2/upload          — actual file body
 *
 * Discovery (UDP multicast on 53317) is intentionally skipped here —
 * Android TV boxes are on Wi-Fi and routers often filter multicast.
 * Senders enter our IP+port manually (we display it in the UI). For
 * v0.4.0 we'll add NSD-based mDNS so phones see us automatically.
 */
@Singleton
class LocalSendServer @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val moshi: Moshi,
) {
    @JsonClass(generateAdapter = true)
    data class FileInfo(val id: String, val fileName: String, val size: Long, val fileType: String? = null)

    @JsonClass(generateAdapter = true)
    data class PrepareUploadReq(val info: Map<String, String>?, val files: Map<String, FileInfo>)

    data class ReceivedFile(val name: String, val size: Long, val timestamp: Long, val path: String)

    private var server: Server? = null
    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private val port = 53317

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _received = MutableStateFlow<List<ReceivedFile>>(emptyList())
    val received: StateFlow<List<ReceivedFile>> = _received.asStateFlow()

    private val _mdnsRegistered = MutableStateFlow(false)
    val mdnsRegistered: StateFlow<Boolean> = _mdnsRegistered.asStateFlow()

    fun start(): Boolean {
        if (server != null) return true
        return runCatching {
            server = Server().also { it.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false) }
            _running.value = true
            registerMdns()
            true
        }.getOrDefault(false)
    }

    fun stop() {
        server?.stop()
        server = null
        unregisterMdns()
        _running.value = false
    }

    /**
     * Announce ourselves on `_localsend._tcp.` so phones running LocalSend
     * see us in their device list automatically. Falls back silently if
     * NsdManager isn't available (rare on Android TV but possible on
     * stripped-down vendor firmware).
     */
    private fun registerMdns() {
        if (registrationListener != null) return
        val mgr = (ctx.getSystemService(Context.NSD_SERVICE) as? NsdManager) ?: return
        nsdManager = mgr
        val info = NsdServiceInfo().apply {
            serviceName = "ZeddiHub TV"
            serviceType = "_localsend._tcp."
            this.port = this@LocalSendServer.port
        }
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(svc: NsdServiceInfo) { _mdnsRegistered.value = true }
            override fun onServiceUnregistered(svc: NsdServiceInfo) { _mdnsRegistered.value = false }
            override fun onRegistrationFailed(svc: NsdServiceInfo, errorCode: Int) { _mdnsRegistered.value = false }
            override fun onUnregistrationFailed(svc: NsdServiceInfo, errorCode: Int) { _mdnsRegistered.value = false }
        }
        registrationListener = listener
        runCatching { mgr.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener) }
    }

    private fun unregisterMdns() {
        val mgr = nsdManager
        val listener = registrationListener
        if (mgr != null && listener != null) {
            runCatching { mgr.unregisterService(listener) }
        }
        registrationListener = null
        _mdnsRegistered.value = false
    }

    /** First non-loopback IPv4 address — used for the "send to" hint. */
    fun listenAddress(): String? {
        val ifaces = runCatching { NetworkInterface.getNetworkInterfaces() }.getOrNull() ?: return null
        for (iface in ifaces) {
            if (!iface.isUp || iface.isLoopback) continue
            for (addr in iface.inetAddresses) {
                if (!addr.isLoopbackAddress && addr.hostAddress?.contains('.') == true) {
                    return "${addr.hostAddress}:$port"
                }
            }
        }
        return null
    }

    private fun receiveDir(): File {
        val base = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: ctx.filesDir
        val dir = File(base, "LocalSend")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private inner class Server : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession): Response {
            return runCatching {
                when (session.uri.trimEnd('/')) {
                    "/api/localsend/v2/info"          -> info()
                    "/api/localsend/v2/prepare-upload" -> prepareUpload(session)
                    "/api/localsend/v2/upload"         -> upload(session)
                    "/" -> newFixedLengthResponse("ZeddiHub TV LocalSend receiver — running on port $port")
                    else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found")
                }
            }.getOrElse {
                newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain",
                    "Server error: ${it.message}")
            }
        }

        private fun info(): Response {
            val body = """{"alias":"ZeddiHub TV","version":"2.0","deviceModel":"Android TV","deviceType":"server","fingerprint":"zeddihub-tv","port":$port,"protocol":"http","download":false,"announce":true}"""
            return newFixedLengthResponse(Response.Status.OK, "application/json", body)
        }

        private fun prepareUpload(session: IHTTPSession): Response {
            val files = HashMap<String, String>()
            val req = readJsonBody(session) ?: return newFixedLengthResponse(
                Response.Status.BAD_REQUEST, "application/json", """{"error":"bad json"}""")
            // Generate a token per file id; senders echo it on /upload.
            val resp = StringBuilder()
            resp.append("""{"sessionId":"zh-tv-${System.currentTimeMillis()}","files":{""")
            req.files.entries.forEachIndexed { idx, (id, _) ->
                val token = id + "-tok"
                if (idx > 0) resp.append(",")
                resp.append(""""$id":"$token"""")
                files[token] = id
            }
            resp.append("}}")
            return newFixedLengthResponse(Response.Status.OK, "application/json", resp.toString())
        }

        private fun upload(session: IHTTPSession): Response {
            val params = session.parameters
            val fileName = params["fileName"]?.firstOrNull() ?: "upload-${System.currentTimeMillis()}.bin"
            val safe = fileName.replace(Regex("[/\\\\\\:*?\"<>|]"), "_")
            val out = File(receiveDir(), safe)
            // NanoHTTPD streams the body — copy it. For multipart there's session.parseBody;
            // for raw stream we can use session.inputStream.
            FileOutputStream(out).use { fos ->
                session.inputStream.copyTo(fos)
            }
            val now = System.currentTimeMillis()
            _received.value = (_received.value + ReceivedFile(out.name, out.length(), now, out.absolutePath))
                .takeLast(50)
            val ts = SimpleDateFormat("HH:mm:ss", Locale("cs")).format(Date(now))
            return newFixedLengthResponse(Response.Status.OK, "application/json",
                """{"received":"$safe","size":${out.length()},"at":"$ts"}""")
        }

        private fun readJsonBody(session: IHTTPSession): PrepareUploadReq? {
            val len = session.headers["content-length"]?.toIntOrNull() ?: return null
            val buf = ByteArray(len)
            var read = 0
            while (read < len) {
                val n = session.inputStream.read(buf, read, len - read)
                if (n <= 0) break
                read += n
            }
            val json = String(buf, 0, read, Charsets.UTF_8)
            return runCatching { moshi.adapter(PrepareUploadReq::class.java).fromJson(json) }
                .getOrNull()
        }
    }
}
