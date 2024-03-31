package us.shoroa.smllk.modloader

import FabricManifest
import downloadFile
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

open class Fabric(val loaderVersion: String) : ModLoader() {
    override suspend fun apply() {
        val manifest = version.launcher?.manifest ?: error("Manifest not found")
        val client = HttpClient(OkHttp)
        val get = client.get(url())
        val fabricManifest = Json.decodeFromString<FabricManifest>(get.bodyAsText())
        manifest.mainClass = fabricManifest.mainClass
        coroutineScope {
            fabricManifest.libraries.forEach { library ->
                launch {
                    val (link, lib, ver) = library.name.split(":")
                    val splitLink = link.split(".").joinToString("/") { it }
                    val fabricurl = "https://maven.fabricmc.net/$splitLink/$lib/$ver/$lib-$ver.jar"
                    val url = if (library.url == "https://maven.fabricmc.net/") fabricurl else fabricurl.replaceFirst("maven.fabricmc.net","repo.legacyfabric.net/repository/legacyfabric")
                    println(url)
                    val path = "$splitLink/$lib/$ver/$lib-$ver.jar"
                    val file = File(version.launcher?.libsDir, path)
                    if (!file.exists() || (library.size > 0L && file.length() != library.size)) {
                        file.parentFile.mkdirs()
                        file.createNewFile()
                        print("Fabric Library ${file.name}\r")
                        downloadFile(client, url, file)
                    }
                    version.launcher?.libraries?.add(file.absolutePath)
                }
            }
        }
        client.close()
    }

    open fun url() = "https://meta.fabricmc.net/v2/versions/loader/${version.launcher?.manifest?.id}/$loaderVersion/profile/json"
}