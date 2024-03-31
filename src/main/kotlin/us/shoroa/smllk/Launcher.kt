package us.shoroa.smllk

import currentOs
import us.shoroa.smllk.downloader.Downloader
import us.shoroa.smllk.serialization.VersionManifest
import us.shoroa.smllk.version.Version
import java.io.File

class Launcher private constructor(
    private val version: Version,
    val rootDir: File,
    val gameDir: File,
    private val args: List<String> = emptyList()
) {
    val assetsDir = File(rootDir, "assets")
    val libsDir = File(rootDir, "libs")
    val nativesDir = File(gameDir, "natives")
    val libraries = mutableListOf<String>()
    lateinit var manifest: VersionManifest

    suspend fun launch() {
        version.init()
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }
        if (!gameDir.exists()) {
            gameDir.mkdirs()
        }
        if (!libsDir.exists()) {
            libsDir.mkdirs()
        }
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
        }
        if (!nativesDir.exists()) {
            nativesDir.mkdirs()
        }
        Downloader(manifest, this).download()
        launchGame()
    }

    private fun launchGame() {
        println("Launching game")
        val cpSeparator = when(currentOs()) {
            "windows" -> ";"
            else -> ":"
        }
        val command = mutableListOf<String>().apply {
            add("java")
            add("-Djava.library.path=${nativesDir.absolutePath}")
            add("-cp")
            add("\"" + libraries.joinToString(cpSeparator) { it } + cpSeparator + "client.jar\"")
            add(manifest.mainClass)
            add("--gameDir")
            add(gameDir.absolutePath)
            add("--assetsDir")
            add(assetsDir.absolutePath)
            add("--assetIndex")
            add(manifest.assets)
            add("--version")
            add(manifest.id)
            addAll(args)
        }
        println(command.joinToString(" "))
        val process = ProcessBuilder(command).directory(gameDir).start()
        val reader = process.inputStream.bufferedReader()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            println(line)
        }
    }

    class Builder {
        private lateinit var _version: Version
        private lateinit var _rootDir: File
        private lateinit var _gameDir: File
        private var _args: List<String> = emptyList()
        fun version(version: Version) = apply { _version = version }
        fun rootDir(rootDir: File) = apply { _rootDir = rootDir }
        fun gameDir(gameDir: File) = apply { _gameDir = gameDir }
        fun args(args: List<String>) = apply { _args = args }
        fun build() = Launcher(_version, _rootDir, _gameDir, _args).apply {
            this.version.launcher = this
        }
    }
}
