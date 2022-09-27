package io.meorg.code.view.props

import io.meorg.code.view.constants.PropsConstants.ALGOPIX_REPOSITORY_CONFIG
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(value = ALGOPIX_REPOSITORY_CONFIG)
@Deprecated("It is static", ReplaceWith("CredentialsSerivce.getAlgopix()"))
data class AlgopixProps(
        val xapikey: String = "",
        val xappid: String = "",
        val webkey: String = "",
        val useragent: String = "Paw/3.2 (Macintosh; OS X/10.16.0) GCDHTTPRequest",
)
