kotlin {
    iosX64() // Для x86_64 архитектуры

    // Настройка бинарных фреймворков для iOS
    iosX64().binaries.framework {
        baseName = "shared"
        isStatic = true
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
