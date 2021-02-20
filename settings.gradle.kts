/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/6.7/userguide/multi_project_builds.html
 */

rootProject.name = "qwe"
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    }
}

include("cache")
include("http:client")
include("http:server")
include("micro:rpc")
include("micro")
include("scheduler")
include("protocol")
include("storage:json")

include(":qwe-base", ":http:http-metadata", ":micro:micro-metadata", ":scheduler:scheduler-metadata")
project(":qwe-base").projectDir = file("base")
project(":http:http-metadata").projectDir = file("http/metadata")
project(":micro:micro-metadata").projectDir = file("micro/metadata")
project(":scheduler:scheduler-metadata").projectDir = file("scheduler/metadata")
