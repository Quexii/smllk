package us.shoroa.smllk.version

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import us.shoroa.smllk.Launcher
import us.shoroa.smllk.modloader.ModLoader

class Version(private val mcVersion: String) {
    private val client = HttpClient(OkHttp)
    var launcher: Launcher? = null
    private var modLoader: ModLoader? = null

    suspend fun init() {
        val versionUrl = fetchVersionUrl()
        if (versionUrl.isNotEmpty()) {
            fetchAndSetVersionManifest(versionUrl)
            modLoader?.apply()
        } else {
            error("Version not found")
        }
    }

    private suspend fun fetchVersionUrl(): String {
        val response = client.get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject
        json["versions"]?.jsonArray?.forEach {
            val obj = it.jsonObject
            if (obj["id"]?.jsonPrimitive?.content == mcVersion) {
                return obj["url"]?.jsonPrimitive?.content ?: error("Version URL not found")
            }
        }
        return ""
    }

    private suspend fun fetchAndSetVersionManifest(versionUrl: String) {
        val versionGet = client.get(versionUrl)
        val versionBody = versionGet.bodyAsText()
        val json = Json { ignoreUnknownKeys = true }
        launcher?.manifest = json.decodeFromString(versionBody)
    }

    fun modLoader(modLoader: ModLoader) = apply {
        this.modLoader = modLoader
    }.also { modLoader.version = this }
}