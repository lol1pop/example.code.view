import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.4"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

allprojects {
	plugins.apply("java")
	plugins.apply("org.jetbrains.kotlin.jvm")

	group = "io.meorg"
	version = "0.0.1-SNAPSHOT"
	java.sourceCompatibility = JavaVersion.VERSION_17

	repositories {
		mavenCentral()
	}

	extra["retrofit.version"] = "2.9.0"
	extra["jackson.version"] = "2.11.0"

	dependencies {
		implementation(kotlin("stdlib-jdk8"))
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "17"
		}
	}
}


dependencies {
	//Spring
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	//Swagger
	implementation("io.springfox:springfox-boot-starter:3.0.0")

	//Jackson
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
	implementation("com.fasterxml.woodstox:woodstox-core:6.2.1")

	//Reactor/Coroutines
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")

	//Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// Logging
	implementation("io.github.microutils:kotlin-logging:1.7.8")

	//Connectors
	api(project(":genius-connector"))
	api(project(":algopix-connector"))

	//GraphQL
	implementation("com.expediagroup:graphql-kotlin-spring-server:3.6.6")
	implementation("com.expediagroup:graphql-kotlin-schema-generator:3.6.6")

	//Http client
	api("com.squareup.retrofit2:retrofit:${property("retrofit.version")}")
	implementation("com.squareup.retrofit2:converter-jackson:${property("retrofit.version")}")
	implementation("se.akerfeldt:okhttp-signpost:1.1.0")
	implementation("com.squareup.okhttp3:okhttp:4.2.1")
	implementation("com.squareup.okhttp3:logging-interceptor:4.2.1")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
	}
}


//Зависимости для коннекторов
allprojects.filter { it.name.endsWith("-connector") }.forEach { project ->
	project.dependencies {

		//RetroFit
		api("com.squareup.retrofit2:retrofit:${property("retrofit.version")}")
		implementation("com.squareup.retrofit2:converter-jackson:${property("retrofit.version")}")

		implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${property("jackson.version")}")

		testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
		testImplementation("com.squareup.okhttp3:mockwebserver:4.2.0")
	}
}