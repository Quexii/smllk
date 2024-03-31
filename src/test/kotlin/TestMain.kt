import kotlinx.coroutines.*
import us.shoroa.smllk.Launcher
import us.shoroa.smllk.modloader.Fabric
import us.shoroa.smllk.modloader.LegacyFabric
import us.shoroa.smllk.version.Version
import java.io.File

fun main() {
    val rootDir = File("D:\\testing\\smllk")
    val gameVersion = "1.8.9"
    val gameDir = File(rootDir, "fabric-$gameVersion")
    val version = Version(gameVersion).modLoader(LegacyFabric("0.15.7"))
    val args = listOf(
        "--username", "TestPlayer",
        "--accessToken", "0",
    )
    val launcher = Launcher.Builder().version(version).rootDir(rootDir).gameDir(gameDir).args(args).build()
    runBlocking {
        launcher.launch()
    }
}