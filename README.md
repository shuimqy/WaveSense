# WaveSense

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="WaveSense Logo" width="120"/>
  <p><strong>智能波形监测系统 Android 客户端</strong></p>
</div>

## 📱 项目简介

WaveSense 是一款基于 Android 平台的智能波形监测应用，提供实时数据监控、历史记录查询、告警管理等功能。应用采用现代化的 Material Design 设计风格，为用户提供流畅的使用体验。

## ✨ 功能特性

- **用户系统**
  - 用户注册与登录
  - 密码修改
  - 个人信息管理

- **实时监控**
  - 实时波形数据展示
  - 图表可视化
  - 数据刷新

- **历史记录**
  - 历史数据查询
  - 数据筛选与排序
  - 详细信息查看

- **告警管理**
  - 告警列表展示
  - 告警详情查看
  - 告警通知推送

- **个人中心**
  - 用户信息展示
  - 系统设置
  - 账户管理

## 🛠️ 技术栈

- **开发语言**: Kotlin
- **最低 SDK**: Android 7.0 (API 24)
- **目标 SDK**: Android 15 (API 35)
- **编译工具**: Gradle 8.x + Kotlin DSL
- **架构模式**: MVC/MVP

### 主要依赖库

| 库名称 | 版本 | 用途 |
|--------|------|------|
| Retrofit | 2.9.0 | 网络请求框架 |
| Gson | - | JSON 解析 |
| OkHttp | 4.9.0 | HTTP 客户端 |
| Lottie | 6.4.0 | 动画效果 |
| MPAndroidChart | v3.1.0 | 图表绘制 |
| Toasty | 1.5.2 | Toast 提示 |
| ViewBinding | - | 视图绑定 |
| DataBinding | - | 数据绑定 |

## 📂 项目结构

```
WaveSense/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/llglh/wavesense/app/
│   │   │   │   ├── db/              # 数据库层
│   │   │   │   │   ├── DataBaseHelper.kt
│   │   │   │   │   └── User.kt
│   │   │   │   ├── network/         # 网络层
│   │   │   │   │   ├── ApiService.kt
│   │   │   │   │   ├── RetrofitClient.kt
│   │   │   │   │   ├── Alarm.kt
│   │   │   │   │   └── ...
│   │   │   │   └── ui/              # UI 层
│   │   │   │       ├── activity/    # Activity
│   │   │   │       ├── fragment/    # Fragment
│   │   │   │       └── adapter/     # 适配器
│   │   │   ├── res/                 # 资源文件
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/             # Android 测试
│   │   └── test/                    # 单元测试
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 11 或更高版本
- Android SDK API 35
- Gradle 8.x

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/WaveSense.git
   cd WaveSense
   ```

2. **配置本地环境**

   创建或修改 `local.properties` 文件，配置 Android SDK 路径：
   ```properties
   sdk.dir=YOUR_ANDROID_SDK_PATH
   ```

3. **同步项目依赖**

   在 Android Studio 中打开项目，等待 Gradle 自动同步依赖。

4. **配置后端 API 地址**

   修改 `app/src/main/java/com/llglh/wavesense/app/network/Settings.kt` 文件中的 API 地址：
   ```kotlin
   const val BASE_URL = "http://your-api-server:port/"
   ```

5. **运行应用**

   连接 Android 设备或启动模拟器，点击 Run 按钮运行应用。

## 🔧 开发指南

### 构建项目

```bash
# 构建 Debug 版本
./gradlew assembleDebug

# 构建 Release 版本
./gradlew assembleRelease

# 运行单元测试
./gradlew test

# 运行 Android 测试
./gradlew connectedAndroidTest
```

### 代码规范

- 遵循 Kotlin 官方编码规范
- 使用 ViewBinding 替代 findViewById
- 网络请求统一使用 Retrofit
- 异步操作使用 Kotlin Coroutines

### 权限说明

应用需要以下权限：

- `INTERNET`: 网络访问
- `ACCESS_NETWORK_STATE`: 网络状态检测
- `VIBRATE`: 震动反馈
- `POST_NOTIFICATIONS`: 推送通知（Android 13+）

## 📱 应用截图

> 待添加应用截图

## 🔄 版本历史

- **v1.4** - 最新版本
  - 新增设置页面
  - 新增告警详情页面
  - 优化用户体验

- **v1.3** - 修改密码功能
  - 实现密码修改功能
  - 优化安全性

- **v1.2** - 基础功能完善
  - 完善核心功能
  - 修复已知问题

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 📧 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue: [GitHub Issues](https://github.com/yourusername/WaveSense/issues)
- Email: your.email@example.com

---

<div align="center">
  Made with ❤️ by WaveSense Team
</div>
