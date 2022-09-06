import io.spine.examples.kanban.dependency.JavaX

/*
 * Add the Gradle plugin for bootstrapping projects built with Spine.
 */
plugins {
    id("io.spine.tools.gradle.bootstrap") version("1.8.2")
}

spine {
    assembleModel()
    enableJava()
}

dependencies {
    implementation("io.spine:spine-server:1.8.2")
    implementation(JavaX.annotations)
}
