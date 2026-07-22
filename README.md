# AndroidViewUtils

[![](https://jitpack.io/v/azheng95/AndroidViewUtils.svg)](https://jitpack.io/#azheng95/AndroidViewUtils)

一组可独立使用的 Android View、图片和动画工具。最低支持 Android 8.0（API 26）。

## 引入

在 `settings.gradle` 中添加 JitPack：

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

在应用模块中添加库依赖，将 `VERSION` 替换为发布 Tag：

```groovy
dependencies {
    implementation 'com.github.azheng95:AndroidViewUtils:VERSION'
}
```

库通过 `compileOnly` 保持可选功能彼此独立。只需为实际使用的功能添加下列依赖。

## 按需依赖

### 基础控件

适用于 `ArcProgressBar`、`DiamondProgressBar`、`DashedLineView`、`ImageViewGrid`、`Switch` 和文本高亮工具。

```kotlin
implementation("androidx.core:core-ktx:1.15.0")
implementation("androidx.appcompat:appcompat:1.7.0")
```

使用 `CircleLoadingView` 还需要：

```kotlin
implementation("androidx.lifecycle:lifecycle-common:2.10.0")
```

不使用 `ImageViewGrid` 或 `Switch` 时可以省略 AppCompat；不使用 `DiamondProgressBar`、`CircleLoadingView` 或文本高亮工具时可以省略 Core KTX。

### 顺序动画

```kotlin
implementation("androidx.core:core-ktx:1.15.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
```

```kotlin
val controller = views.animateSequentially(lifecycleScope) {
    delayBetween(80L)
    duration(300L)
    direction(AnimationDirection.BOTTOM_TO_TOP)
}

override fun onDestroy() {
    controller.release()
    super.onDestroy()
}
```

### Glide 图片工具

现代 `loadImage` API 只需要：

```kotlin
implementation("com.github.bumptech.glide:glide:4.16.0")
```

```kotlin
imageView.loadImage(
    model = imageUrl,
    config = ImageLoadConfig(
        shape = ImageShape.Rounded(radius = 16),
        scale = ImageScale.CENTER_CROP,
        placeholderRes = R.drawable.image_placeholder
    )
)
```

`ImageShape.Rounded.radius` 使用 px。`placeholderRes`、`errorRes` 和 `fallbackRes` 彼此独立，需要时分别设置。`ImageLoadConfig` 会复制并保护传入的变换列表，创建后不会受原列表修改影响。

`loadImageToBitmapResult` 使用同一份配置，包括 `thumbnailMultiplier`，并返回独立 Bitmap 副本；调用方不再使用时可以安全调用 `recycle()`。

只有使用 wasabeef 模糊等额外变换时，才添加：

```kotlin
implementation("com.github.wasabeef:glide-transformations:4.3.0")
```

使用 `loadImageToBitmapResult` 或 `ViewToImageUtils` 时，再添加协程：

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
```

```kotlin
val config = ImageLoadConfig(
    extraTransformations = listOf(BlurTransformation(radius = 12, sampling = 2))
)
```

库本身不静态引用 `glide-transformations`。旧 `setImageUrl*` API 已移除，请统一迁移到 `loadImage` 和 `ImageLoadConfig`。

仅使用这些扩展函数不需要 Glide KSP。只有应用自行声明 `AppGlideModule` 并使用生成 API 时，才需要配置 Glide 的 KSP 处理器。

`ViewToImageUtils` 还需要：

```kotlin
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
```

注册待预加载图片时直接传入相同配置：

```kotlin
ViewToImageUtils.registerImageViewLoadParam(imageView, imageUrl, config)
```

完整可运行示例见 [`ImageDemoActivity`](app/src/main/java/com/azheng/androidviewutils/demo/image/ImageDemoActivity.kt)，包含圆角、圆形、缩略图、error/fallback 和 Bitmap 加载及回收。

## 示例工程

Demo 模块按功能提供可运行页面：

- [`ComponentsDemoActivity`](app/src/main/java/com/azheng/androidviewutils/demo/components/ComponentsDemoActivity.kt)：所有自定义 View 和交互配置。
- [`ImageDemoActivity`](app/src/main/java/com/azheng/androidviewutils/demo/image/ImageDemoActivity.kt)：Glide 图片与 Bitmap 加载。
- [`TextDemoActivity`](app/src/main/java/com/azheng/androidviewutils/demo/text/TextDemoActivity.kt)：关键词高亮和全部 Markwon 插件。
- [`StorageDemoActivity`](app/src/main/java/com/azheng/androidviewutils/demo/storage/StorageDemoActivity.kt)：View 截图和全部 Bitmap 保存目的地。
- [`SequentialAnimDemoActivity`](app/src/main/java/com/azheng/androidviewutils/demo/sequentialanimator/SequentialAnimDemoActivity.kt)：顺序动画方向、策略、DSL 和生命周期管理。

直接使用 `BitmapSaveUtils` 时只需要 `kotlinx-coroutines-android`。

在 Android 8 和 Android 9 上保存到公共相册时，应用需要自行声明并申请 `WRITE_EXTERNAL_STORAGE` 权限。Android 10 及以上不需要该权限。

### Markwon 插件

`BlockImagePlugin`、`MatchParentImagePlugin` 和 `CustomTextSizePlugin` 需要：

```kotlin
implementation("io.noties.markwon:core:4.6.2")
implementation("io.noties.markwon:image:4.6.2")
```

## 自定义 View

XML 属性定义在 [`attrs.xml`](AndroidViewUtils/src/main/res/values/attrs.xml)。代码方式支持直接属性赋值，也支持链式批量配置：

```kotlin
binding.arcProgress
    .setProgress(70f)
    .setGradientColors(intArrayOf(Color.RED, Color.YELLOW))
    .apply()
```

## 构建验证

```shell
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

## 版本与变更

发布版本使用 Git Tag。破坏性变更和迁移说明记录在 [CHANGELOG.md](CHANGELOG.md)。

当前未发布版本移除了 Edge-to-Edge API，并将自定义 XML 属性统一增加 `avu_` 前缀；升级时需要同步修改布局文件。由于包含破坏性变更，建议下一个 Tag 使用 `0.1.0`，不要继续发布为 `0.0.x` patch。

贡献说明见 [CONTRIBUTING.md](CONTRIBUTING.md)，安全问题报告方式见 [SECURITY.md](SECURITY.md)。

## License

Copyright 2026 azheng95

Licensed under the [Apache License 2.0](LICENSE).
