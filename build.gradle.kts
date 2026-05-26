plugins {
	java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
}

group = "com.zaliznyimh"
version = "1.0.0-DEV"
description = "A REST API proxy for GitHub using Java 25 and Spring Boot 4 to list non-forked repositories and branches."

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.restclient)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webflux)
    testImplementation(libs.wiremock.spring.boot)
    testImplementation(libs.spring.boot.webtestclient)
    testImplementation(libs.commons.lang3)

    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
	useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}
