# MakoSU v4.1.8 中文修复说明

本文记录 MakoSU Manager `v4.1.8`（`versionCode 40840`）当前维护版本中与主题、自定义壁纸、页面导航和界面稳定性有关的修复。本文只描述已经落入源码的行为，不扩大声明内核、KMI、驱动或设备兼容范围。

## 版本身份

| 项目 | 值 |
| --- | --- |
| 应用名称 | MakoSU |
| 应用包名 | `com.makosu.manager` |
| 版本名称 | `v4.1.8` |
| 版本代码 | `40840` |
| Release 证书 SHA-256 | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |
| 最低 Android 版本 | Android 8.0（API 26） |

本轮修复没有修改包名、版本号、驱动版本、KMI 内容或 Release 签名。使用相同正式证书安装时，可以覆盖已有的 MakoSU Manager。

## 修复摘要

- 新增可持久化的自定义壁纸选择、开关、更换和清除功能。
- 新增壁纸透明度、模糊度和暗化程度调节。
- 修复壁纸无法覆盖状态栏、标题栏、底部导航栏和横屏侧栏的问题。
- 修复透明页面在导航动画中透出旧页面内容的“串页”问题。
- 恢复与无壁纸模式相同的 MIUIX 原版页面转场。
- 修复预测性返回默认关闭且运行时开关无效的问题。
- 删除转场期间的全屏实时模糊，降低动画卡顿和 GPU 过度绘制。
- 修复更换主题色后 Miuix 嵌套主题继续使用旧颜色的问题。
- 修复“调整背景”展开箭头状态不变化的问题。
- 新增从 FolkPatch CircleUI 适配的第三种首页布局。
- 恢复首页原版 `MakoSU` 标题栏和内容间距。
- 增加壁纸模糊算法单元测试和 Release 构建校验。

## 自定义壁纸

### 使用方法

1. 打开“设置”。
2. 进入“主题设置”。
3. 找到“自定义背景”。
4. 首次开启时选择一张本地图片。
5. 点击“调整背景”展开详细参数。
6. 根据需要调整透明度、模糊度和暗化程度。
7. 可以随时更换图片、关闭壁纸或清除已保存的图片引用。

### 参数范围

| 参数 | 默认值 | 范围 | 作用 |
| --- | ---: | ---: | --- |
| 透明度 | `100%` | `0% - 100%` | 控制壁纸本身的可见程度 |
| 模糊度 | `6 dp` | `0 - 24 dp` | 控制后台生成的模糊半径 |
| 暗化程度 | `5%` | `0% - 30%` | 在壁纸上覆盖黑色遮罩，提高文字可读性 |

滑块只在拖动结束后提交设置，避免拖动过程中不断重建壁纸缓存。关闭自定义壁纸不会删除已选择的 URI；使用“清除背景”才会同时关闭功能并移除保存的 URI。

### 文件访问

壁纸选择使用 Android `OpenDocument` 接口，并申请持久化读取权限。应用重启后仍可读取原文件，不需要把整张图片复制到应用私有目录。

以下情况可能导致原壁纸失效：

- 图片被删除、移动或重命名。
- 文件提供器撤销了访问权限。
- 系统清除了应用数据或文档提供器数据。
- 图片来自临时文件提供器，提供器本身不支持持久授权。

遇到这些情况时，在主题设置中重新选择图片即可。

## 全屏显示修复

自定义壁纸启用后，以下界面层会切换为透明或壁纸感知模式：

- 根页面容器。
- 首页、超级用户、模块和设置页面。
- 主题设置页面。
- 顶部标题栏。
- 底部导航栏。
- 宽屏设备的侧边导航栏。
- 系统状态栏和手势导航区域。

窗口继续使用 edge-to-edge 布局，壁纸使用 `ContentScale.Crop` 填满可用区域。页面卡片和部分表面保留半透明颜色，避免文字直接压在复杂图片上。

Miuix 表面透明度会根据壁纸透明度调整，范围约为 `78% - 90%`。页面最底层仍使用完全不透明的主题表面作为兜底，防止图片尚未加载时闪出旧页面、黑屏或窗口默认白色。

## 导航串页修复

### 原因

MIUIX `NavDisplay` 默认使用约 `500 ms` 的横向转场。转场期间，新页面和旧页面会同时存在于场景栈中。普通主题的页面背景不透明，因此不会看到下层页面；启用透明壁纸容器后，旧页面的文字和卡片会透过新页面，看起来像旧页面被带进了新页面。

### 处理方式

当前实现不再修改 MIUIX 的转场时长、方向或缓动曲线。每个导航页面都有独立、完整且不透明的壁纸底层：

- 前进动画仍使用原版从右向左滑入。
- 返回动画仍使用原版反向滑动。
- 预测返回仍由系统手势进度驱动。
- 新页面的壁纸底层会遮住下方旧页面内容。
- 转场结束后，导航框架照常销毁不再需要的旧场景。

因此，壁纸模式和无壁纸模式使用同一套 MIUIX 转场配置，不再依赖 `1 ms` 强制淡出或完全关闭动画等规避方式。

## 预测性返回修复

### 原因

旧实现没有在 Manifest 中静态启用 `android:enableOnBackInvokedCallback`，而是在应用启动和设置页中通过隐藏 API 修改 `ApplicationInfo`。对应偏好的默认值还是 `false`，因此应用可能在启动时主动关闭系统回调。运行时修改系统标志也不属于稳定的公开接口，Activity 重建后是否生效取决于系统实现。

### 当前实现

- 在应用 Manifest 中固定声明 `android:enableOnBackInvokedCallback="true"`。
- 删除 `ApplicationInfo` 反射和 Hidden API 豁免调用。
- 删除默认关闭的预测返回偏好和无效设置开关。
- 由 Android 系统、Navigation3 和 MIUIX 自动处理返回进度。
- 前进、普通返回和预测返回继续共用同一导航栈。

预测返回需要系统使用手势导航。Android 13 设备通常还需要在开发者选项中开启预测性返回动画；Android 14 及以上由系统版本和桌面实现决定最终的返回预览效果。三键导航不会显示边缘滑动预览，部分厂商系统或第三方桌面也可能关闭系统级动画。

## 动画性能修复

### 旧实现的问题

早期移植方案直接在全屏 `Image` 上使用 Compose `Modifier.blur()`。页面切换时，GPU 需要同时处理：

- 全屏壁纸采样。
- 全屏 RenderEffect 模糊。
- 半透明页面和卡片合成。
- 新旧导航场景的滑动与裁剪。
- MIUIX 转场暗化效果。

在高分辨率屏幕或性能较弱的设备上，这会造成明显掉帧，严重时看起来像页面停在转场中间。

### 当前实现

当前壁纸链路如下：

1. Coil `2.7.0` 按当前窗口尺寸解码图片。
2. 模糊任务在 Coil 后台 Transformation 中执行，不占用 UI 线程。
3. 图片在模糊前按边长 `35%` 采样，像素数量约为原目标的 `12.25%`。
4. 使用两次分离盒式模糊近似柔和模糊效果。
5. 处理结果进入 Coil 内存缓存。
6. 页面转场期间只绘制普通缓存位图，不再执行实时 RenderEffect。
7. 根窗口不再重复绘制第二张相同壁纸，减少一层全屏过度绘制。

更换壁纸、改变窗口尺寸或提交新的模糊度时会生成新的缓存键。普通页面跳转不会重新解码或重新模糊图片。

## 主题颜色修复

### 原因

Miuix 外层主题更新后，内层半透明主题曾只以基础颜色对象作为 `remember` 键。部分主题切换中，基础对象没有按预期失效，导致页面继续使用旧的强调色。

### 处理方式

内层主题缓存现在同时跟踪：

- 完整的 `AppSettings`。
- 当前深色模式状态。
- 解析后的主题关键色。
- 自定义壁纸开关。
- 自定义壁纸透明度。

更换固定主题色、Monet 模式、深浅色模式或壁纸状态时，Miuix 色板会重新计算并立即应用。

## 控件与布局修复

- “调整背景”使用紧凑设置行，不再长期占用大块空间。
- 展开时显示向上箭头，收起时显示向下箭头。
- 透明度、模糊度和暗化程度只在展开后显示。
- 滑块尺寸与现有 Miuix 设置控件保持一致。
- 主题预览区域的顶部、底部留白已压缩。
- 首页标题恢复原版 `TopAppBar`，没有保留实验性的紧凑标题栏。
- 首页内容恢复原版 `12 dp` 垂直间距。

### CircleUI 首页

FolkPatch 当前提供 `ListUI`、`GridUI`、`FocusUI`、`SignUI`、`CircleUI`、`DashboardUI` 和 `StatsUI` 共 7 种首页布局。MakoSU 选择移植其中较克制的 CircleUI，并按现有 Miuix 数据模型重新实现：

- 使用横向工作状态卡显示 Root 状态、驱动版本和 `LKM/Built-in` 模式。
- 使用两个等宽计数卡显示超级用户数量和模块数量。
- 保留原有安装、超级用户和模块页面跳转行为。
- 保留原有警告、系统信息、支持和了解更多卡片。
- 自定义壁纸开启时，状态卡使用半透明颜色。
- 设置页和主题预览会同步显示 CircleUI 结构。

CircleUI 作为第三个可选布局加入，不替换原有 `Miuix` 和 `MIUIXMode`，已有用户的布局偏好不会自动改变。

## 状态持久化

以下设置保存在 Manager 的设置仓库中：

| 键 | 类型 | 说明 |
| --- | --- | --- |
| `custom_background_enabled` | Boolean | 是否启用自定义壁纸 |
| `custom_background_uri` | String | Android 文档 URI |
| `custom_background_opacity` | Float | 壁纸透明度 |
| `custom_background_blur` | Float | 模糊度 |
| `custom_background_dim` | Float | 暗化程度 |

读取设置时会再次限制数值范围，防止旧配置、手动修改或异常数据传入界面。

## 代码位置

| 功能 | 主要文件 |
| --- | --- |
| 壁纸绘制与 Coil 请求 | `manager/app/src/main/java/com/sukisu/ultra/ui/component/CustomBackground.kt` |
| 后台模糊 Transformation | `manager/app/src/main/java/com/sukisu/ultra/ui/component/BackgroundBlurTransformation.kt` |
| 纯 Kotlin 模糊算法 | `manager/app/src/main/java/com/sukisu/ultra/ui/component/FastBoxBlur.kt` |
| 页面壁纸层与导航入口 | `manager/app/src/main/java/com/sukisu/ultra/ui/MainActivity.kt` |
| 主题设置界面 | `manager/app/src/main/java/com/sukisu/ultra/ui/screen/colorpalette/ColorPaletteScreenMiuix.kt` |
| 文件选择与持久授权 | `manager/app/src/main/java/com/sukisu/ultra/ui/screen/colorpalette/ColorPaletteScreen.kt` |
| 设置持久化 | `manager/app/src/main/java/com/sukisu/ultra/data/repository/SettingsRepositoryImpl.kt` |
| Miuix 半透明色板 | `manager/app/src/main/java/com/sukisu/ultra/ui/theme/MiuixTheme.kt` |
| 模糊算法测试 | `manager/app/src/test/java/com/sukisu/ultra/ui/component/FastBoxBlurTest.kt` |

## 自动验证

本轮源码已经通过以下检查：

```powershell
Set-Location D:\MakoSU\manager
.\gradlew.bat :app:testDebugUnitTest :app:compileDebugKotlin --console=plain
.\gradlew.bat :app:assembleRelease --console=plain
```

模糊算法测试覆盖：

- 模糊半径为零时不修改像素。
- 纯色图片经过多次模糊后保持原色。
- 单个亮点能够按预期扩散到相邻像素。

Release APK 还通过了以下检查：

```powershell
apksigner verify --print-certs MakoSU_v4.1.8_40840-release.apk
aapt2 dump badging MakoSU_v4.1.8_40840-release.apk
Get-FileHash -Algorithm SHA256 MakoSU_v4.1.8_40840-release.apk
```

当前本地产物：

| 项目 | 值 |
| --- | --- |
| 文件名 | `MakoSU_v4.1.8_40840-release.apk` |
| 文件大小 | `12,566,311` 字节 |
| APK SHA-256 | `b6dd104ff85c7794e87120d5612a72b620c6b04c6a9df06c2b65679803f10dce` |
| 包名 | `com.makosu.manager` |
| 版本 | `v4.1.8 (40840)` |
| 证书 SHA-256 | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |

> [!IMPORTANT]
> 自动测试和构建成功不能替代真机回归。正式发布前仍需在至少一台手势导航设备上检查前进、返回、预测返回、横竖屏切换、亮暗主题和大尺寸图片。

## 建议的真机回归清单

- 在亮色、深色和 Monet 模式下分别选择壁纸。
- 检查状态栏、挖孔区域、底部手势区域是否连续显示。
- 从首页进入关于、主题、模块仓库、安装和工具页面。
- 连续执行前进、返回和预测返回，确认没有串页或明显跳帧。
- 在首页四个分页之间滑动，确认壁纸和底部导航栏显示正常。
- 调整透明度、模糊度和暗化程度，退出设置后再次进入。
- 杀死应用并重新启动，确认壁纸 URI 和参数仍然有效。
- 横竖屏切换后检查裁剪范围和缓存更新。
- 删除原图片后重新进入应用，确认失败时不会闪退。
- 关闭自定义壁纸，确认普通主题和原版动画没有回归。

## 故障排查

### 壁纸无法显示

1. 确认原图片仍然存在。
2. 在主题设置中清除背景。
3. 重新使用系统文档选择器选择图片。
4. 不要使用只能临时授权的第三方文件提供器。

### 页面仍然出现旧内容

1. 确认安装的是本文对应的 APK，而不是之前关闭动画或强制淡出的测试包。
2. 核对 APK SHA-256 和版本信息。
3. 强制停止应用后重新打开，排除旧进程仍在运行。
4. 提交问题时附上屏幕录像、进入的页面路径和系统动画倍率。

### 动画仍然卡顿

1. 首次选择图片或修改模糊度后，等待后台缓存生成完成。
2. 将模糊度设为 `0`，判断问题是否来自图片处理。
3. 暂时关闭系统开发者选项中的强制 GPU、调试过度绘制或布局边界。
4. 使用系统相册中的普通 JPEG/PNG 排除异常文件提供器。
5. 记录设备型号、Android 版本、屏幕分辨率、图片尺寸和复现路径。

### 主题色没有立即变化

当前版本应当在设置提交后重新计算 Miuix 色板。如果仍然不生效，请记录当前颜色模式、Monet 状态、固定关键色、自定义壁纸开关和目标页面后提交问题。

## 回退方式

壁纸功能不影响 Root、LKM、KMI 或启动镜像。出现界面问题时可以按以下顺序回退：

1. 在主题设置中关闭“自定义背景”。
2. 使用“清除背景”删除保存的 URI。
3. 强制停止并重新启动 Manager。
4. 无法进入设置时，清除 Manager 应用数据；此操作会重置 Manager 本地设置。

不需要因为壁纸或主题问题重新刷写内核、LKM 或启动镜像。

## 许可证与来源

壁纸层的初始设计参考并移植自 [LyraVoid/FolkPatch](https://github.com/LyraVoid/FolkPatch) 的 `BackgroundLayer.kt`。FolkPatch 与 MakoSU 均使用 GPL-3.0 兼容许可。当前 MakoSU 实现已经针对自身导航、设置仓库和性能要求进行重构。

图片加载使用 Coil `2.7.0`。所有第三方代码和依赖仍受各自许可证约束，发布源码和二进制时应继续履行对应的许可证义务。

## 反馈要求

提交与本修复有关的问题时，请至少提供：

- 设备型号与 Android 版本。
- MakoSU 版本和 APK SHA-256。
- 系统导航模式和动画倍率。
- 图片格式、分辨率和大致文件大小。
- 当前透明度、模糊度和暗化程度。
- 是否启用了 Monet、深色模式、Miuix 模糊或悬浮底栏。
- 最短复现步骤和屏幕录像。

请勿在公开 Issue 中上传包含个人信息的壁纸原图、完整系统日志、密钥、Token 或签名文件。
