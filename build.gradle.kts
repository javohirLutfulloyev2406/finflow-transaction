plugins {
    java
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.finflow"
version = "0.0.1-SNAPSHOT"
description = "FinFlow Transaction Service"

java {
    toolchain {
        // Java 21 (LTS) — kafolatlangan Boot 3.5 mosligi.
        // Java 25 ga o'tish: bu qiymatni 25 qiling va `./gradlew clean build` bilan tekshiring.
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val mapstructVersion = "1.6.3"
val lombokVersion = "1.18.36"
val lombokMapstructBindingVersion = "0.2.0"

configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

repositories {
    mavenCentral()
}

dependencies {
    // ---- Web / API ----
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // ---- Persistence ----
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // ---- Redis (idempotency, sliding window) ----
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // ---- Kafka (outbox publish, saga choreography) ----
    implementation("org.springframework.kafka:spring-kafka")

    // ---- Observability ----
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // ---- Mapping ----
    implementation("org.mapstruct:mapstruct:$mapstructVersion")

    // ---- Lombok ----
    compileOnly("org.projectlombok:lombok:$lombokVersion")

    // TARTIB MUHIM: lombok -> binding -> mapstruct.
    // Binding bo'lmasa, MapStruct Lombok generatsiya qilgan getter'larni "ko'rmaydi"
    // va mapper jimgina null qaytaradi. Bank loyihasida bu eng xavfli xato turi.
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // ---- Test ----
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // ---- Keyingi bosqichlarda ochiladi ----
    // implementation("org.springframework.boot:spring-boot-starter-security")
    // implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    // implementation("org.springframework.boot:spring-boot-starter-quartz")
    // implementation("io.github.resilience4j:resilience4j-spring-boot3")
    // implementation("net.devh:grpc-client-spring-boot-starter:3.1.0.RELEASE")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:1.20.4")
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "-parameters",
            "-Amapstruct.defaultComponentModel=spring",
            "-Amapstruct.unmappedTargetPolicy=ERROR",
            "-Amapstruct.verbose=true"
        )
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}
