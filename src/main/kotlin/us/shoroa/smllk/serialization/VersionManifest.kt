package us.shoroa.smllk.serialization
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement

@Serializable
data class VersionManifest(
    val arguments: Arguments? = null,
    val assetIndex: AssetIndex,
    val assets: String,
    val complianceLevel: Int,
    val downloads: Downloads,
    val id: String,
    val javaVersion: JavaVersion,
    val libraries: MutableList<LibraryX>,
    val logging: Logging,
    var mainClass: String,
    val minecraftArguments: String? = null,
    val minimumLauncherVersion: Int,
    val releaseTime: String,
    val time: String,
    val type: String
)

@Serializable
data class Arguments(
    @Contextual val game: MutableList<JsonElement>,
    @Contextual val jvm: MutableList<JsonElement>
)

@Serializable
data class AssetIndex(
    val id: String,
    val sha1: String,
    val size: Long,
    val totalSize: Int,
    val url: String
)

@Serializable
data class Downloads(
    val client: Client
)

@Serializable
data class JavaVersion(
    val component: String,
    val majorVersion: Int
)

@Serializable
data class LibraryX(
    val downloads: DownloadsX,
    val name: String,
    val rules: List<Rule>? = null,
    val extract: Extract? = null,
    val natives: Natives? = null
)

@Serializable
data class Logging(
    val client: ClientX
)

@Serializable
data class Client(
    val sha1: String,
    val size: Long,
    val url: String
)

@Serializable
data class DownloadsX(
    val artifact: Artifact? = null,
    val classifiers: Classifiers? = null
)

@Serializable
data class Rule(
    val action: String,
    val os: Os? = null,
    val features: Features? = null
)

@Serializable
data class Features(val ele: JsonElement)

@Serializable
data class Extract(
    val exclude: List<String>
)

@Serializable
data class Natives(
    val linux: String? = null,
    val osx: String? = null,
    val windows: String? = null
)

@Serializable
data class Artifact(
    val path: String,
    val sha1: String,
    val size: Long,
    val url: String
)

@Serializable
data class Classifiers(
    @SerialName("natives-linux") val nativesLinux: NativesX? = null,
    @SerialName("natives-osx") val nativesOsx: NativesX? = null,
    @SerialName("natives-windows") val nativesWindows: NativesX? = null
)

@Serializable
data class NativesX(
    val path: String,
    val sha1: String,
    val size: Long,
    val url: String
)

@Serializable
data class Os(
    val name: String
)

@Serializable
data class ClientX(
    val argument: String,
    val file: FileX,
    val type: String
)

@Serializable
data class FileX(
    val id: String,
    val sha1: String,
    val size: Long,
    val url: String
)