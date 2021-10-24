# monitor base 简介
koom 监控功能共用的基础模块，所有监控模块初始化之前，请确保该模块已经初始化

# monitor base 初始化
## 依赖配置
- 项目根目录 build.gradle 的 repositories 中增加 mavenCentral
```groovy
repositories {
    mavenCentral()
}
```
- 项目 app/build.gradle 中增加依赖
```groovy
dependencies {
    implementation "com.kuaishou.koom:koom-monitor-base:${latest_version}"
}
```
## 代码初始化
- 定义初始化任务
```kotlin
object CommonInitTask : InitTask {
  override fun init(application: Application) {
    val config = CommonConfig.Builder()
      .setApplication(application) // Set application
      .setVersionNameInvoker { "1.0.0" } // Set version name, java leak feature use it
      .build()

    MonitorManager.initCommonConfig(config)
      .apply { onApplicationCreate() }
  }
}
```  
- 在 `Application` 的 `onCreate` 方法中初始化
```java
public class KOOMApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    CommonInitTask.INSTANCE.init(this);
  }
}
```