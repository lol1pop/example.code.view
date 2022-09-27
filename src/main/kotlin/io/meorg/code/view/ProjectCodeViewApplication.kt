package io.meorg.code.view

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [ElasticsearchDataAutoConfiguration::class])
@EnableConfigurationProperties(
	GeniusProps::class,
	AlgopixProps::class,
)
@EnableWebFlux
@EnableScheduling
class ProjectCodeViewApplication

fun main(args: Array<String>) {
	runApplication<ProjectCodeViewApplication>(*args)
}
