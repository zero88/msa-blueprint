import org.gradle.internal.jvm.Jvm
import org.gradle.util.GradleVersion
import java.time.Instant
import java.util.jar.Attributes.Name

plugins {
    `java-library`
    `maven-publish`
    jacoco
    signing
    id(PluginLibs.sonarQube) version PluginLibs.Version.sonarQube
    id(PluginLibs.nexusStaging) version PluginLibs.Version.nexusStaging
}
val jacocoHtml: String? by project
val semanticVersion: String by project

allprojects {
    group = "io.github.zero88"

    repositories {
        mavenLocal()
        maven { url = uri("https://maven.pkg.github.com/zero88/java-utils") }
        maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "eclipse")
    apply(plugin = "idea")
    apply(plugin = "jacoco")
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    project.version = "$version$semanticVersion"

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        withJavadocJar()
        withSourcesJar()
    }

    dependencies {
        compileOnlyApi(JacksonLibs.annotations)
        compileOnly(UtilLibs.lombok)
        annotationProcessor(UtilLibs.lombok)

        testImplementation(TestLibs.jsonAssert)
        testImplementation(TestLibs.junit5Api)
        testRuntimeOnly(TestLibs.junit5Engine)
        testCompileOnly(UtilLibs.lombok)
        testAnnotationProcessor(UtilLibs.lombok)
    }

    tasks {
        jar {
            manifest {
                attributes(
                    mapOf(Name.IMPLEMENTATION_TITLE.toString() to project.name,
                          Name.IMPLEMENTATION_VERSION.toString() to project.version,
                          "Created-By" to GradleVersion.current(),
                          "Build-Jdk" to Jvm.current(),
                          "Build-By" to project.property("buildBy"),
                          "Build-Hash" to project.property("buildHash"),
                          "Build-Date" to Instant.now())
                )
            }
        }
        javadoc {
            options {
                this as StandardJavadocDocletOptions
                tags = mutableListOf("apiNote:a:API Note:", "implSpec:a:Implementation Requirements:",
                                     "implNote:a:Implementation Note:")
            }
        }
        test {
            useJUnitPlatform()
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group as String?
                artifactId = project.name
                version = project.version as String?
                from(components["java"])

                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
                pom {
                    name.set(project.name)
                    description.set("A Vertx framework for microservice")
                    url.set("https://github.com/zero88/blueprint")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://github.com/zero88/blueprint/blob/master/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("zero88")
                            email.set("sontt246@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://git@github.com:zero88/blueprint.git")
                        developerConnection.set("scm:git:ssh://git@github.com:zero88/blueprint.git")
                        url.set("https://github.com/zero88/blueprint")
                    }
                }
            }
        }
        repositories {
            maven {
                val path = if (project.hasProperty("github")) {
                    "${project.property("github.nexus.url")}/${project.property("nexus.username")}/${rootProject.name}"
                } else {
                    val releasesRepoUrl = project.property("ossrh.release.url")
                    val snapshotsRepoUrl = project.property("ossrh.snapshot.url")
                    if (project.hasProperty("release")) releasesRepoUrl else snapshotsRepoUrl
                }
                url = path?.let { uri(it) }!!
                credentials {
                    username = project.property("nexus.username") as String?
                    password = project.property("nexus.password") as String?
                }
            }
        }
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["maven"])
    }
}

task<JacocoReport>("jacocoRootReport") {
    dependsOn(subprojects.map { it.tasks.withType<Test>() })
    dependsOn(subprojects.map { it.tasks.withType<JacocoReport>() })
    additionalSourceDirs.setFrom(subprojects.map { it.sourceSets.main.get().allSource.srcDirs })
    sourceDirectories.setFrom(subprojects.map { it.sourceSets.main.get().allSource.srcDirs })
    classDirectories.setFrom(subprojects.map { it.sourceSets.main.get().output })
    executionData.setFrom(project.fileTree(".") {
        include("**/build/jacoco/test.exec")
    })
    reports {
        csv.isEnabled = false
        xml.isEnabled = true
        xml.destination = file("${buildDir}/reports/jacoco/coverage.xml")
        html.isEnabled = (jacocoHtml ?: "true").toBoolean()
        html.destination = file("${buildDir}/reports/jacoco/html")
    }
}

project.tasks["sonarqube"].group = "analysis"
project.tasks["sonarqube"].dependsOn("build", "jacocoRootReport")
sonarqube {
    properties {
        property("jacocoHtml", "false")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/coverage.xml")
    }
}

task<Sign>("sign") {
    dependsOn(subprojects.map { it.tasks.withType<Sign>() })
}

nexusStaging {
    packageGroup = project.group as String?
    username = project.property("nexus.username") as String?
    password = project.property("nexus.password") as String?
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
}