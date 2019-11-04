/**
 * This project is using independent gradle sub projects. I.e, this may look like a multi project gradle project but it isn't.
 * 
 * The reason for this is that the dependencies get a bit weird with multimodule. We need the plugin in the codegen
 * project to be built & deployed into a local maven repo in it's build directory. The client project uses that in it's build
 * to generate code. This does not work with a traditional multi module setup because you end up with chicken egg problems 
 * where the plugin has not been built yet when the client project is initializing.
 * 
 * Instead we have 3 independent gradle projects parent, codegen, and client. Client simply points at the plugin in the codegen
 * build directory. Parent knows how to build code gen and then client.
 */

plugins {
//    base
    `maven-publish`
}


tasks {
    val codegenJar by registering(GradleBuild::class) {
        dir = file("codegen")
        tasks = listOf("jar","publish")
    }

    val codegenClean by registering(GradleBuild::class) {
        dir = file("codegen")
        tasks = listOf("clean")
    }

    val clientJar by registering(GradleBuild::class) {
        dir = file("client")
        tasks = listOf("jar")
    }

    val clientBuild by registering(GradleBuild::class) {
        dir = file("client")
        tasks = listOf("build")
    }

    val clientClean by registering(GradleBuild::class) {
        dir = file("client")
        tasks = listOf("clean")
    }

    val build  by registering(GradleBuild::class) {}
    val clean  by registering(GradleBuild::class) {}

    build {
        dependsOn(codegenJar,clientBuild)
    }

    clean {
        // because clientClean does not work unless we have the codeGen plugin we build it before cleaning it again
        // yay gradle DSL madness, so hacky
        dependsOn(codegenJar,clientClean)
    }

    clientJar {
        dependsOn(codegenJar)
    }

//    findByName("build")?.dependsOn(clientBuild)

//    artifacts {
//        add("archives", mapOf("file" to File("${projectDir}/client/build/lib/client.jar"), "type" to "jar", "name" to "es-kotlin-wrapper-client"))
//        // add("es-kotlin-wrapper-client", client.map { it -> it.destFile }) {
//        //         name = "es-kotlin-wrapper-client"
//        //         type = "jar"
//        //         builtBy(client)
//        // }
//    }

    configure<PublishingExtension> {
        publications {
            this.create<MavenPublication>("client") {
                artifactId = "es-kotlin-wrapper-client"
                artifact(File("$projectDir/client/build/libs/client.jar"))
            }
        }
    }
}

tasks.withType<AbstractPublishToMaven> {
    dependsOn("client")
}



