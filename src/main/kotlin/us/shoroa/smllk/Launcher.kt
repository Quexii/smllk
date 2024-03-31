package us.shoroa.smllk

import us.shoroa.smllk.utils.*
import us.shoroa.smllk.downloader.Downloader
import us.shoroa.smllk.serialization.VersionManifest
import us.shoroa.smllk.version.Version
import java.io.File

class Launcher private constructor(
    private val version: Version,
    val rootDir: File,
    val gameDir: File,
    private val jvmArgs: List<String> = emptyList(),
    private val gameArgs: List<String> = emptyList(),
    private val cp: List<String> = emptyList(),
    private var mainClass: String = "",
    private var javaPath: String = "java",
) {
    val assetsDir = File(rootDir, "assets")
    val libsDir = File(rootDir, "libs")
    val nativesDir = File(gameDir, "natives")
    val libraries = mutableListOf<String>()
    lateinit var manifest: VersionManifest

    suspend fun launch() {
        version.init()
        if (mainClass.isEmpty()) mainClass = manifest.mainClass
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
        val cpSeparator = when (currentOs()) {
            "windows" -> ";"
            else -> ":"
        }
        val command = mutableListOf<String>().apply {
            add(javaPath)
            add("-Djava.library.path=${nativesDir.absolutePath}")
            addAll(jvmArgs)
            add("-cp")
            add("\"" + cp.joinToString(cpSeparator) { it } + cpSeparator + libraries.joinToString(cpSeparator) { it } + cpSeparator + "client.jar\"")
            add(mainClass)
            add("--gameDir")
            add(gameDir.absolutePath)
            add("--assetsDir")
            add(assetsDir.absolutePath)
            add("--assetIndex")
            add(manifest.assets)
            add("--version")
            add(manifest.id)
            addAll(gameArgs)
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
        private var _jvmArgs: List<String> = emptyList()
        private var _gameArgs: List<String> = emptyList()
        private var _cp: List<String> = emptyList()
        private var _mainClass = ""
        private var _javaPath = "java"
        fun version(version: Version) = apply { _version = version }
        fun rootDir(rootDir: File) = apply { _rootDir = rootDir }
        fun gameDir(gameDir: File) = apply { _gameDir = gameDir }
        fun jvmArgs(args: List<String>) = apply { _jvmArgs = args }
        fun gameArgs(args: List<String>) = apply { _gameArgs = args }
        fun cp(cp: List<String>) = apply { _cp = cp }
        fun mainClass(mainClass: String) = apply { _mainClass = mainClass }
        fun javaPath(javaPath: String) = apply { _javaPath = javaPath }
        fun build() = Launcher(_version, _rootDir, _gameDir, _jvmArgs, _gameArgs, _cp, _mainClass, _javaPath).apply {
            this.version.launcher = this
        }
    }
}
