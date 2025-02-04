plugins {
	id("java")
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.6"
	id("maven-publish")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17)) // Utiliser Java 17
	}
	withSourcesJar() // Générer un JAR contenant les sources
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.kafka:spring-kafka")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
	implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
	implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")

	implementation("org.slf4j:slf4j-api:2.0.7")
	implementation("org.slf4j:slf4j-simple:2.0.7")
}

tasks.withType<Test> {
	useJUnitPlatform()
	isEnabled = false // Désactiver les tests
}

// Désactiver complètement la tâche `bootJar` pour éviter tout conflit
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
	enabled = false
}

// Configurer explicitement la tâche `jar` pour générer le fichier JAR standard
tasks.named<Jar>("jar") {
	enabled = true
	archiveClassifier.set("") // Supprimer le suffixe '-plain'
	dependsOn("classes") // Assurer que les classes sont générées avant le JAR
}

// Configuration pour Maven Publish
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifact(tasks.named("jar").get()) // Publier le JAR standard
			artifact(tasks.named("sourcesJar").get()) // Ajouter le JAR des sources
			groupId = "com.example"
			artifactId = "common-library"
			version = "0.0.1-SNAPSHOT"
		}
	}
	repositories {
		maven {
			name = "local"
			url = uri("file:///Users/mourad/.m2/local-repo") // Dépôt Maven local
		}
	}
}
