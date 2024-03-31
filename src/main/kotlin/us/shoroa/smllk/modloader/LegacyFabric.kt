package us.shoroa.smllk.modloader

class LegacyFabric(loaderVersion: String) : Fabric(loaderVersion) {
    override fun url() = super.url().replaceFirst("fabricmc.net", "legacyfabric.net")
}