plugins {
    kotlin("multiplatform") version "1.5.31"
    `maven-publish`
}

group = "org.getaviz"
version = "1.0"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("layoutLib") {
            from(components["kotlin"])
        }
    }
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

tasks.register<Copy>("jsArtefact") {
    from(layout.buildDirectory.dir("js/packages/Layout_multiplatform/kotlin/layout_multiplatform.js"),
        layout.buildDirectory.dir("js/packages_imported/kotlin/1.5.31/kotlin.js"))
    into(layout.projectDirectory.dir("../ui/libs/kotlin-layouter"))
}

tasks.named("build") {
    finalizedBy("jsArtefact", "publishToMavenLocal")
}