plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation.safe.args)
    id("org.jetbrains.kotlin.kapt")
    id("kotlin-parcelize")

}

hilt {
    enableAggregatingTask = false
}

android {
    namespace = "com.example.androidapprpg"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.androidapprpg"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
        }
    }

    flavorDimensions += "env"

    productFlavors {
        create("mock") {
            dimension = "env"
            applicationIdSuffix = ".mock"
            versionNameSuffix = "-mock"
            buildConfigField("boolean", "MOCK_MODE", "true")
            buildConfigField("String", "BASE_URL_GAME", "\"https://t7tsd4gbsd.execute-api.sa-east-1.amazonaws.com/\"")
            buildConfigField("String", "WS_BASE", "\"https://alob-rpg-958777443.sa-east-1.elb.amazonaws.com\"")
            buildConfigField("String", "WS_ENDPOINT", "\"ws\"")
            buildConfigField("boolean", "WS_SOCKJS", "false")


        }
        create("prod") {
            dimension = "env"
            buildConfigField("boolean", "MOCK_MODE", "false")
            buildConfigField("String", "BASE_URL_GAME", "\"https://t7tsd4gbsd.execute-api.sa-east-1.amazonaws.com/\"")
            buildConfigField("String", "WS_BASE", "\"https://alob-rpg-958777443.sa-east-1.elb.amazonaws.com\"")
            buildConfigField("String", "WS_ENDPOINT", "\"ws\"")
            buildConfigField("boolean", "WS_SOCKJS", "false")

        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.smoothbottombar)
    implementation(libs.picasso)
    //implementation(libs.spinkit)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.hilt.android)
    implementation(libs.dyn4j)
    implementation(libs.javapoet)
    implementation(libs.circleimageview)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.navigation.testing.android)
    implementation(libs.lottie)
    implementation(libs.androidx.fragment.testing)
    implementation(libs.androidx.scenecore)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.scenecore)
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    kapt(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.48")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}