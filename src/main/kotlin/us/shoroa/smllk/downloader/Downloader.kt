package us.shoroa.smllk.downloader

import currentOs
import downloadFile
import extractZipFile
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import us.shoroa.smllk.Launcher
import us.shoroa.smllk.serialization.VersionManifest
import us.shoroa.smllk.utils.Timer
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

private const val RESOURCES_URL = "https://resources.download.minecraft.net/"

class Downloader(private val manifest: VersionManifest, val launcher: Launcher) {
    private val client = HttpClient(OkHttp)
    suspend fun download() {
        downloadAssets()
        downloadLibraries()
        downloadClient()
        client.close()
    }

    private suspend fun downloadAssets() {
        print("Checking Assets\r")
        val get = client.get(manifest.assetIndex.url)
        val assetIndex = File(launcher.assetsDir, "indexes/${manifest.assetIndex.id}.json")
        if (!assetIndex.exists()) {
            assetIndex.parentFile.mkdirs()
            assetIndex.createNewFile()
            get.bodyAsChannel().copyAndClose(assetIndex.writeChannel())
        }

        val json = Json.parseToJsonElement(get.bodyAsText())
        val objects = json.jsonObject["objects"]!!.jsonObject

        val downloadedAssets = AtomicInteger(0)
        val timer = Timer()

        val semaphore = Semaphore(20)
        coroutineScope {
            objects.map { (_, value) ->
                launch {
                    semaphore.withPermit {
                        val hash = value.jsonObject["hash"]!!.jsonPrimitive.content
                        val folder = File(launcher.assetsDir, "objects/" + hash.substring(0, 2))
                        val file = File(folder, hash)
                        val size = value.jsonObject["size"]!!.jsonPrimitive.long
                        if (!file.exists() || file.length() != size) {
                            val url = "$RESOURCES_URL${hash.substring(0, 2)}/$hash"
                            val response = client.get(url)
                            val data = response.bodyAsChannel()

                            print("Assets ${file.name} \r")
                            file.parentFile.mkdirs()
                            data.copyAndClose(file.writeChannel())
                            downloadedAssets.incrementAndGet()
                        }
                    }
                }
            }.forEach { it.join() }
        }

        println("Took ${timer.sinceStart()} to download ${downloadedAssets.get()} assets")
    }

    private suspend fun downloadLibraries() {
        val semaphore = Semaphore(20)
        val downloadedLibs = AtomicInteger(0)
        val timer = Timer()
        coroutineScope {
            manifest.libraries.forEach { library ->
                launch {
                    semaphore.withPermit {
                        val dw = library.downloads
                        val artifact = dw.artifact
                        val classifiers = dw.classifiers

                        if (artifact != null) {
                            val file = File(launcher.libsDir, artifact.path)
                            if (!file.exists() || file.length() != artifact.size) {
                                file.parentFile.mkdirs()
                                file.createNewFile()
                                print("Library ${file.name}\r")
                                downloadFile(client, artifact.url, file)
                                downloadedLibs.incrementAndGet()
                            }
                            launcher.libraries.add(file.absolutePath)
                        }
                        if (classifiers != null) {
                            val native = when (currentOs()) {
                                "windows" -> classifiers.nativesWindows
                                "osx" -> classifiers.nativesOsx
                                "linux" -> classifiers.nativesLinux
                                else -> null
                            }
                            if (native != null) {
                                val file = File(launcher.nativesDir, native.path)
                                if (!file.exists() || file.length() != native.size) {
                                    file.parentFile.mkdirs()
                                    file.createNewFile()
                                    print("Native ${file.name}\r")
                                    downloadFile(client, native.url, file)
                                    var excludeList = emptyList<String>()
                                    if (library.extract != null) {
                                        excludeList = library.extract.exclude
                                    }
                                    print("Extracting ${file.name}\r")
                                    extractZipFile(file.absolutePath, launcher.nativesDir.absolutePath, excludeList)
                                    downloadedLibs.incrementAndGet()
                                }
                                launcher.libraries.add(file.absolutePath)
                            }
                        }
                    }
                }
            }
        }
        println("Took ${timer.sinceStart()} to download ${downloadedLibs.get()} libraries")
    }

    private suspend fun downloadClient() {
        val client = manifest.downloads.client
        val timer = Timer()
        val file = File(launcher.gameDir, "client.jar")
        if (!file.exists() || file.length() != client.size) {
            file.parentFile.mkdirs()
            file.createNewFile()
            downloadFile(client.url, file)
        }
        println("Took ${timer.sinceStart()} to download client")
    }
}