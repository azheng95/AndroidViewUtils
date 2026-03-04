[![](https://jitpack.io/v/azheng95/AndroidViewUtils.svg)](https://jitpack.io/#azheng95/AndroidViewUtils)



在 `settings.gradle` 文件中加入

```groovy
dependencyResolutionManagement {
    repositories {
        // JitPack 远程仓库：https://jitpack.io
        maven { url 'https://jitpack.io' }
    }
}
```
或者在 `settings.gradle.kts` 文件中加入

```groovy
dependencyResolutionManagement {
    repositories {
        // JitPack 远程仓库：https://jitpack.io
        maven { url = uri("https://jitpack.io") }
    }
}
```

[ImageViewer.kt](AndroidViewUtils/src/main/java/com/azheng/viewutils/imageviewer/ImageViewer.kt)和[ImagePicker.kt](AndroidViewUtils/src/main/java/com/azheng/viewutils/imagepicker/ImagePicker.kt)工具会自动初始化

在项目 app 模块下的 `build.gradle` 文件中加入远程依赖 ，Tag 替换为最新版本

```groovy
dependencies {
	        implementation 'com.github.azheng95:AndroidViewUtils:Tag'
}
```


***********添加以下依赖**********

 ```
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:ksp:4.16.0")
    implementation("com.github.wasabeef:glide-transformations:4.3.0")

    implementation("io.noties.markwon:core:4.6.2")
    // Markwon + Glide图片加载插件（处理网络图片）
    implementation("io.noties.markwon:image-glide:4.6.2")
    implementation("io.noties.markwon:ext-strikethrough:4.6.2")
    implementation("io.noties.markwon:ext-tables:4.6.2")
    implementation("io.noties.markwon:ext-tasklist:4.6.2")
    implementation("io.noties.markwon:ext-latex:4.6.2")
    implementation("io.noties.markwon:html:4.6.2")
    implementation("io.noties.markwon:image:4.6.2")
    implementation("io.noties.markwon:linkify:4.6.2")
    implementation("io.noties.markwon:simple-ext:4.6.2")
    implementation("io.noties.markwon:editor:4.6.2")
    implementation("io.noties.markwon:syntax-highlight:4.6.2")
    implementation("com.github.piasy:BigImageViewer:1.8.1")
    implementation("com.github.piasy:GlideImageLoader:1.8.1")
    implementation("com.github.piasy:GlideImageViewFactory:1.8.1")
```
