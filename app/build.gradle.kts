/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.2/userguide/building_java_projects.html
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.11.0")


    // This dependency is used by the application.
    implementation("com.google.guava:guava:30.1.1-jre")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-csv
    implementation("org.apache.commons:commons-csv:1.11.0")
// https://mvnrepository.com/artifact/commons-cli/commons-cli
    implementation("commons-cli:commons-cli:1.9.0")



}

application {
    // Define the main class for the application.
    mainClass.set("rpa.Main")
}

tasks.test {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
