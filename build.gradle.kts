plugins {
    id("java")
}

group = "org.monitor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("junit:junit:4.13.1")
    implementation ("org.jsoup:jsoup:1.16.1")
    implementation("org.json:json:20230227")
    implementation("commons-io:commons-io:2.13.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}