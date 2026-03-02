plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.4"
    java
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Spring Mail für Email-Versand
    implementation("org.springframework.boot:spring-boot-starter-mail")
    // Bean Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}


tasks.withType<Test> {
    useJUnitPlatform()
}
