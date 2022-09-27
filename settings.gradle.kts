rootProject.name = "code.view"
include("genius-connector", "algopix-connector")

//inline fun String?.ifNullOrBlank(default: () -> String): String = if (this.isNullOrBlank()) default() else this
//val affiliateConnectorsPath: String = System.getenv("AFFILIATE_CONNECTORS_PATH").ifNullOrBlank { "../affiliate-network-connectors" }
//if (file(affiliateConnectorsPath).exists()) {
//    includeBuild(affiliateConnectorsPath)
//}

