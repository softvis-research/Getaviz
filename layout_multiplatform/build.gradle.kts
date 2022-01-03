plugins {
    kotlin("multiplatform") version "1.5.31"
}

group = "org.getaviz"
version = "1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(LEGACY) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jsMain by getting
    }
}

tasks.register<Copy>("jvmArtefact") {
    from(layout.buildDirectory.dir("libs/layout_multiplatform-jvm-1.0.jar"))
    into(layout.projectDirectory.dir("../generator2/org.getaviz.generator/lib"))
}

tasks.register<Copy>("jsArtefact") {
    from(layout.buildDirectory.dir("js/packages/Layout_multiplatform/kotlin/layout_multiplatform.js"))
    into(layout.projectDirectory.dir("../ui/libs/kotlin-layouter"))
}

tasks.register<Exec>("installLocalJar") {
    isIgnoreExitValue = true
    workingDir = layout.projectDirectory.dir("../generator2/org.getaviz.generator").asFile
    println(workingDir)

    commandLine("mvn.cmd", "validate")

    doLast {
        println(errorOutput)
    }
}

tasks.named("build") {
    finalizedBy("jvmArtefact", "jsArtefact", "installLocalJar")
}
