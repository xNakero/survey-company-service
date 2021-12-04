import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("groovy")

    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    kotlin("plugin.jpa") version "1.5.31"
}

group = "pl.wat"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

extra["testcontainersVersion"] = "1.16.0"

val integration: SourceSet by sourceSets.creating {
    groovy {
        groovy.srcDir("src/integration/groovy")
        resources.srcDir("src/integration/resources")
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.auth0:java-jwt:3.18.2")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
    testImplementation("org.codehaus.groovy:groovy-all:3.0.9")

    "integrationImplementation"("org.spockframework:spock-spring:2.0-groovy-3.0")
    "integrationImplementation"("org.springframework.boot:spring-boot-starter-test")
    "integrationImplementation"("org.springframework.security:spring-security-test")
    "integrationImplementation"("org.codehaus.groovy.modules.http-builder:http-builder:0.7.1")
    "integrationImplementation"("org.springframework.boot:spring-boot-starter-webflux")

    "integrationImplementation"("org.testcontainers:elasticsearch")
    "integrationImplementation"("org.testcontainers:spock")
    "integrationImplementation"("org.testcontainers:mongodb")
    "integrationImplementation"("org.testcontainers:postgresql")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

val integrationTaskTask = task<Test>("integrationTest") {
    description = "Runs the integration tests"
    group = "verification"

    testClassesDirs = integration.output.classesDirs
    classpath = integration.runtimeClasspath

    mustRunAfter(tasks.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.check {
    dependsOn(integrationTaskTask)
}
