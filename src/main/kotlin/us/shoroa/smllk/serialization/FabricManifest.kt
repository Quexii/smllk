
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

import kotlinx.serialization.json.JsonElement

@Serializable
data class FabricManifest(
    val id: String,
    val inheritsFrom: String,
    val releaseTime: String,
    val time: String,
    val type: String,
    val mainClass: String,
    val arguments: Arguments? = null,
    val libraries: List<Library>
)

@Serializable
data class Arguments(
    @Contextual val game: MutableList<JsonElement>,
    @Contextual val jvm: MutableList<JsonElement>
)

@Serializable
data class Library(
    val name: String,
    val url: String,
    val md5: String = "",
    val sha1: String = "",
    val sha256: String = "",
    val sha512: String = "",
    val size: Long = 0L
)