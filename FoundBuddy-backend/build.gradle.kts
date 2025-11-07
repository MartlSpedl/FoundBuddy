plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.4"
    java
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    // H2
    // implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // implementation("com.h2database:h2") // statt runtimeOnly
    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}


tasks.withType<Test> {
    useJUnitPlatform()
}
