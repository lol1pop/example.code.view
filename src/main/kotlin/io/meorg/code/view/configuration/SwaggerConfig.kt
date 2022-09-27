package com.switips.us.marketplace.unit.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket

private fun apiKey(): ApiKey = ApiKey("Bearer", "Authorization", "header")

private fun securityContext(): SecurityContext =
    SecurityContext.builder()
        .securityReferences(defaultAuth())
        .operationSelector { it.requestMappingPattern().startsWith("/api") }
        .build()

private fun defaultAuth(): List<SecurityReference?> =
    listOf(SecurityReference("Bearer", arrayOf(AuthorizationScope("admin", "Admin scope"))))

@Configuration
class SwaggerConfig {
    @Bean
    fun api(): Docket =
        Docket(DocumentationType.SWAGGER_2)
            .securityContexts(listOf(securityContext()))
            .securitySchemes(listOf(apiKey()))
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.switips.us.marketplace.admin"))
            .paths(PathSelectors.any())
            .build()
}