import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val debugApiBaseUrl = gradle.startParameter.projectProperties["apiBaseUrl"] ?: "http://localhost:3000"

android {
    namespace = "br.com.sosplus"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "br.com.sosplus"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"${providers.gradleProperty("apiBaseUrl").getOrElse("https://api.sosplus.example")}\"")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val encaminharApiLocal by tasks.registering {
    notCompatibleWithConfigurationCache("A tarefa consulta os dispositivos Android conectados ao instalar o app.")
    doLast {
        val localProperties = Properties().apply {
            rootProject.file("local.properties").inputStream().use { load(it) }
        }
        val sdk = localProperties.getProperty("sdk.dir") ?: error("sdk.dir ausente em local.properties")
        val adbName = if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) "adb.exe" else "adb"
        val adb = File(sdk, "platform-tools/$adbName")
        check(adb.isFile) { "ADB não encontrado em ${adb.absolutePath}" }

        fun runAdb(vararg args: String): String {
            val process = ProcessBuilder(listOf(adb.absolutePath) + args).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            check(process.waitFor() == 0) { output }
            return output
        }
        val serials = runAdb("devices").lineSequence()
            .map { it.trim().split(Regex("\\s+")) }
            .filter { it.size >= 2 && it[1] == "device" }
            .map { it[0] }
            .toList()
        check(serials.isNotEmpty()) { "Conecte e inicie um dispositivo Android antes de executar o app." }
        serials.forEach { serial -> runAdb("-s", serial, "reverse", "tcp:3000", "tcp:3000") }
    }
}
tasks.configureEach { if (name == "installDebug") dependsOn(encaminharApiLocal) }

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
