package io.meorg.code.view.props

import io.meorg.code.view.constants.PropsConstants.GENIUS_REPOSITORY_CONFIG
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(value = GENIUS_REPOSITORY_CONFIG)
@Deprecated("It is static", ReplaceWith("CredentialsSerivce.getGenius()"))
data class GeniusProps(
        val productToken: String = "",
        val reviewToken: String = "",
        val reviewSchedulerEnabled: Boolean = false,
        val reviewScheduledLimit: Int = 5000,
        val repository: GeniusRepositoryProps = GeniusRepositoryProps()
)

data class GeniusRepositoryProps(
        val host: String = "localhost",
        val port: Long = 27017,
        val database: String = "genius",
        val collection: String = "review"
)
