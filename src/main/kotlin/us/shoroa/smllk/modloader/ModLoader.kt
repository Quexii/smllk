package us.shoroa.smllk.modloader

import us.shoroa.smllk.version.Version

abstract class ModLoader {
    lateinit var version: Version
    abstract suspend fun apply()
}