package us.shoroa.smllk.utils

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

suspend fun downloadFile(url: String, outputFile: File) {
    downloadFile(HttpClient(OkHttp), url, outputFile)
}

suspend fun downloadFile(client: HttpClient, url: String, outputFile: File) {
    val response: HttpResponse = client.get(url)
    withContext(Dispatchers.IO) {
        outputFile.writeBytes(response.readBytes())
    }
}

fun currentOs(): String {
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("win") -> "windows"
        os.contains("mac") -> "osx"
        os.contains("nix") || os.contains("nux") || os.contains("aix") -> "linux"
        else -> throw UnsupportedOperationException("Unsupported OS")
    }
}

fun extractZipFile(zipFile: String, outputDirectory: String, exclusions: List<String> = emptyList()) {
    val buffer = ByteArray(1024)
    val folder = File(outputDirectory)

    if (!folder.exists()) {
        folder.mkdir()
    }

    ZipInputStream(FileInputStream(zipFile)).use { zis ->
        var zipEntry = zis.nextEntry

        while (zipEntry != null) {
            val newFile = File(outputDirectory, zipEntry.name)

            if (!exclusions.any { zipEntry?.name!!.contains(it) }) {
                File(newFile.parent).mkdirs()
                FileOutputStream(newFile).use { fos ->
                    var len: Int
                    while (zis.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                }
            } else {
                println("Excluding ${zipEntry.name}")
            }

            zipEntry = zis.nextEntry
        }
        zis.closeEntry()
    }
}