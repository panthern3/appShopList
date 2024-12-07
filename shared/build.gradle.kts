
plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktorClientCore)
            implementation(libs.ktorClientJson)
            implementation(libs.ktorClientSerialization)
            implementation(libs.kotlinxSerializationJson)
            implementation(libs.retrofit)
            implementation(libs.gson)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.ktorClientIos)
        }
    }
}
