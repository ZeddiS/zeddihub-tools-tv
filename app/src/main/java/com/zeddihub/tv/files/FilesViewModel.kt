package com.zeddihub.tv.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

enum class FileSource { LOCAL, EXTERNAL, LOCALSEND, SMB }

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    @ApplicationContext private val appCtx: Context,
) : ViewModel() {

    private val _source = MutableStateFlow(FileSource.LOCAL)
    val source: StateFlow<FileSource> = _source.asStateFlow()

    private val _items = MutableStateFlow<List<FileItem>>(emptyList())
    val items: StateFlow<List<FileItem>> = _items.asStateFlow()

    private val _currentPath = MutableStateFlow<String>(initialPathFor(FileSource.LOCAL))
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    val currentPathLabel: StateFlow<String> = currentPath

    fun switchSource(s: FileSource) {
        _source.value = s
        _currentPath.value = initialPathFor(s)
        refresh()
    }

    fun refresh() {
        when (_source.value) {
            FileSource.LOCAL, FileSource.EXTERNAL, FileSource.LOCALSEND -> loadFolder(_currentPath.value)
            FileSource.SMB -> _items.value = emptyList() // placeholder
        }
    }

    fun canGoUp(): Boolean {
        if (_source.value == FileSource.SMB) return false
        val current = File(_currentPath.value)
        val root = File(initialPathFor(_source.value))
        return current.absolutePath != root.absolutePath && current.parentFile != null
    }

    fun goUp() {
        val parent = File(_currentPath.value).parentFile ?: return
        _currentPath.value = parent.absolutePath
        refresh()
    }

    fun openItem(ctx: Context, item: FileItem) {
        if (item.isDirectory) {
            _currentPath.value = item.path
            refresh()
            return
        }
        val file = File(item.path)
        if (!file.exists()) return
        val authority = "${ctx.packageName}.fileprovider"
        val uri: Uri = runCatching { FileProvider.getUriForFile(ctx, authority, file) }
            .getOrNull() ?: return
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toString()).lowercase()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { ctx.startActivity(intent) }
    }

    private fun loadFolder(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(path)
            if (!dir.exists() || !dir.canRead()) {
                _items.value = emptyList(); return@launch
            }
            val children = dir.listFiles()?.sortedWith(
                compareBy({ !it.isDirectory }, { it.name.lowercase() })
            ) ?: emptyList()
            _items.value = children.map {
                FileItem(
                    name = it.name,
                    path = it.absolutePath,
                    isDirectory = it.isDirectory,
                    sizeBytes = if (it.isDirectory) 0L else it.length(),
                )
            }
        }
    }

    private fun initialPathFor(s: FileSource): String = when (s) {
        FileSource.LOCAL -> Environment.getExternalStorageDirectory().absolutePath
        FileSource.EXTERNAL -> {
            // First mounted secondary storage; falls back to /storage if none.
            // Reflection-free approach: scan /storage/* excluding the primary
            // Environment-reported "self" / "emulated" entries.
            val storageRoot = File("/storage")
            val candidates = storageRoot.listFiles()?.filter {
                val name = it.name
                it.isDirectory && it.canRead() && name != "self" && name != "emulated"
            }.orEmpty()
            candidates.firstOrNull()?.absolutePath ?: "/storage"
        }
        FileSource.LOCALSEND -> {
            // Matches LocalSendServer's drop dir (Documents/ZeddiHubTV)
            val docs = appCtx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: appCtx.filesDir
            File(docs, "localsend").also { it.mkdirs() }.absolutePath
        }
        FileSource.SMB -> "smb://"
    }
}
