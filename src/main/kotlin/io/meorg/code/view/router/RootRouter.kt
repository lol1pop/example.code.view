package io.meorg.code.view.router

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.*

@Configuration
class RootRouter(
        private val userService: UserService
) {
    @Bean
    fun rootRouters() = coRouter {
        GET("/hello") {
            ServerResponse.ok().html().bodyValueAndAwait("hello rest api")
        }

        GET("/internal/token") { req ->
            val token = req.awaitPrincipal()?.name
                    ?.let { userService.findByUserId(it) }
                    ?.switipsInfo()
                    ?.token
                    ?.accessToken
            ServerResponse.ok().bodyValueAndAwait(token.orEmpty())
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
