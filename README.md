# Kanhe（勘合）—— Minecraft Fabric 进服密码 Mod

一个功能性 Fabric Mod：进服务器**前**弹窗要求输入密码（密码框是掩码显示，输入内容全部显示为 `*`），
服务端校验，管理员可**随时重置**密码；随机密码的形态（数字 / 字母 / 混合）和位数（默认 6 位）都可以自定义，也可以手动指定任意密码。

* 客户端与服务端**都要装**这个 Mod（同一个 jar）
* 不依赖 Fabric API，只依赖 Fabric Loader
* 需要 Java 25

> 名字取自明代用于**对合验证**的凭证「勘合」：两份凭证对得上才生效，和这里 `_id` 的对合校验是同一套思路。

## 支持版本

| 我的世界 | Fabric Loader | Mod 版本 | 下载 |
| --- | --- | --- | --- |
| 26.4-snapshot-2 | 0.19.5 | 1.1.0 | `kanhe-fabric-1.1.0+26.4-snapshot-2.jar` |
| 26.4-snapshot-1 | 0.19.5 | 1.1.0 | `kanhe-fabric-1.1.0+26.4-snapshot-1.jar` |
| 26.4-snapshot-2 | 0.19.5 | 1.0.0 | `kanhe-fabric-1.0.0+26.4-snapshot-2.jar` |
| 26.4-snapshot-1 | 0.19.5 | 1.0.0 | `kanhe-fabric-1.0.0+26.4-snapshot-1.jar` |

> 同一个游戏版本可能有多个 Mod 版本，**下载表格最上面那一行**（最新）即可。Fabric mod 与游戏版本是绑定的，支持新版本时需要**重新编译一份 jar**，然后在这张表里加一行、发一个新的 Release —— 项目名、标题、描述都不用动。
> Release 约定：tag 用 `v<mod版本>`（如 `v1.0.0`），标题用 `Kanhe <mod版本>`，支持的我的世界版本写在 Release 说明和上表里。

---

## 1. 实现原理

本 Mod 复用原版的“服务器地址查询参数”机制来承载密码，其余部分（弹窗、校验、重置、隐身）本mod实现的：

| 部分 | 来源 |
| --- | --- |
| 密码在网络上怎么传（服务器地址里的 `?_id=xxxxxx` 查询参数、`minecraft:intent` 包） | **原版机制**（服务器地址 `?_id=` 查询参数），直接复用，没有另造协议 |
| 官方对密码的校验（`server.properties` 里的 `allowed-connection-ids`，在 `DedicatedServer.acceptsConnection` 里比对 `_id`） | 官方有，但**只能写死在 server.properties 里、改完要重启、对“对局域网开放”的存档完全无效** |
| 进服前弹窗、掩码输入、按服务器记住密码、密码错误后重新弹窗 | **本 Mod 实现**（Mixin 进 `ConnectScreen.startConnecting`） |
| 随机密码的生成（数字 / 字母 / 混合）、保存、运行时重置、形态与位数调整、`/kanhe` 指令 | **本 Mod 实现**（Mixin 进 `ServerHandshakePacketListenerImpl.handleIntention` + `Commands` 构造函数） |
| 局域网（集成服务器）也被密码保护、没带密码的连接静默拒绝（服务器看起来像“根本不存在”） | **本 Mod 实现**，原版机制做不到 |

一句话：**传输通道沿用原版的 `_id`，而密码的完整生命周期（生成 → 弹窗输入 → 对合校验 → 重置 → 对外隐身）由本 Mod 实现。**

---

## 2. 安装

### 客户端
把 jar（选上表里对应你游戏版本的那个）放进：
* 未开启版本隔离：`.minecraft/mods/`
* 开启版本隔离：`.minecraft/versions/<版本名>/mods/`

### 服务端（Fabric 服务端）
放进服务器的 `mods/` 目录，重启服务器即可。

> 需要 Fabric Loader 0.19.5。

---

## 3. 使用

### 玩家
1. 在多人游戏里正常输入 `IP:端口`，点“加入服务器”。
2. 弹出 **服务器密码** 界面（标题、服务器名、提示、密码输入框、记住密码勾选框、进入/取消按钮）。
3. 输入管理员给的密码（输入内容显示为 `*`），回车或点“进入服务器”。
4. 密码框里的内容始终显示为 `*`，输入的字符数看得见、内容看不见。
5. 密码错了会回到这个界面并给出原因；勾了“记住密码”下次会自动填好（仍会弹窗，直接回车即可）。

没有任何密码的服务器：留空直接进。
已经在地址里写了 `?_id=xxxxxx`（或 `xxxxxx@IP`）时，Mod 不再弹窗，按原样连接。

### 管理员命令（需要 OP / 权限等级 GameMaster 以上）
指令名是 `/kanhe`（从 1.0.0 起只有这一个指令名）
```
/kanhe help                列出所有用法（只输入 /kanhe 效果相同）
/kanhe show                查看当前密码、随机密码的形态与位数、开关状态
/kanhe reset               按当前的随机形态与位数生成新的随机密码（立刻生效，不用重启）
/kanhe set <密码>          手动指定密码（1~64 个可见字符，不会改动随机形态与位数）
/kanhe length <位数>       修改随机密码的位数（4~32）并生成新密码
/kanhe mode <形态>         修改随机密码的形态并生成新密码（digits 数字 / letters 小写字母 / mixed 小写字母+数字）
/kanhe on                  开启保护
/kanhe off                 关闭保护（关闭后谁都能进）
```
重置后旧密码立即失效。命令也会在服务器控制台打印新密码（控制台没有语言文件时显示英文兜底文本）。

### 配置文件
| 文件 | 作用 |
| --- | --- |
| 服务端 `<服务器目录>/config/kanhe.json` | `enabled`（开关）、`code`（当前密码）、`mode`（随机密码形态：`digits` / `letters` / `mixed`）、`length`（随机密码位数，默认 6，范围 4~32）、`logCode`（是否在日志里打印密码） |
| 局域网存档 `<.minecraft>/config/kanhe.json` | 同上，作用于“对局域网开放”的那个集成服务器 |
| 客户端 `<.minecraft>/config/kanhe-client.json` | `promptAlways`（已记住密码时是否仍然弹窗，默认 true）、`rememberByDefault`、`passwords`（按服务器地址记住的密码） |

首次启动会按配置的形态与位数（默认 6 位数字）生成一个随机密码并写进配置，同时打印在日志里：
`Server password is 123456 - players enter it in the join password prompt`

---

## 4. 行为细节

* **没带密码的连接**（包括服务器列表的 ping）：和官方一样**静默断开**，不返回任何状态，所以外人扫端口时服务器看起来是“死的”，不会泄露服务器信息。
* **带了错误密码的登录**：会被明确告知“密码错误”（因为对方本来就知道这个服务器存在），客户端 Mod 会据此重新弹出密码框。
* **存档主自己**（单机/局域网主机）不会被拦：主机走内存连接，不经过握手校验。
* **局域网客人**会被拦，所以开黑的朋友也要装这个 Mod。
* 随机密码支持三种形态：数字（6 位约 100 万种组合）、小写字母（6 位约 3 亿种组合）、小写字母+数字（6 位约 22 亿种组合）；位数用 `/kanhe length` 改，范围 4~32。字母只用**小写**（密码框是掩码显示，区分大小写的话打错了看不出来）。`set` 手动指定时最长 64 个可见字符（不能含空格）。建议配合正版验证/白名单使用；密码泄露或被爆破时，直接 `/kanhe reset` 换一个即可。

---

## 5. 从源码构建


```bash
./gradlew build          # Windows: .\gradlew.bat build
```

产物：`build/libs/kanhe-fabric-<mod版本>+<游戏版本>.jar`（可直接丢进 `mods/`）。

要给另一个游戏版本编 jar：把 `gradle.properties` 里的 `minecraft_version` 改成目标版本再 `build` 即可，文件名会自动带上游戏版本。

命名规则：`<modid>-<加载器>-<modversion>+<游戏版本>.jar`，以后出 Forge / NeoForge 版本时把 `gradle.properties` 里的 `mod_loader` 改成 `forge` / `neoforge`，产物名自动区分（构建脚本另需一套，不是改个名字就能编）。

* 需要 **JDK 25**。
* Gradle 版本由 `gradle/wrapper/gradle-wrapper.properties` 指定（9.7.1），首次构建时 wrapper 会自动下载；
  国内网络慢的话可以把里面的 `distributionUrl` 换成镜像，例如
  `https://mirrors.cloud.tencent.com/gradle/gradle-9.7.1-bin.zip`。
* 26.1 起 Minecraft 官方不再混淆，所以**不需要 yarn / officialMojangMappings**；
  Fabric 为这类版本发布的是一份空的 intermediary，`build.gradle` 直接把它当 mappings 用
  （见 <https://docs.fabricmc.net/develop/porting/mappings/>）。

源码结构：
```
src/main/java/com/kanhe/
  Kanhe.java              常量（modid、_id 属性名、翻译键）
  PasswordCodes.java               随机密码生成与校验（数字 / 字母 / 混合）
  KanheConfig.java        服务端配置读写（Gson）
  KanheGate.java          服务端密码状态：生成、重置、校验
  KanheCommand.java       /kanhe 指令
  KanheMod.java           Mod 入口
  mixin/ServerHandshakePacketListenerImplMixin.java   握手阶段拦截（核心校验）
  mixin/CommandsMixin.java                            注册指令
  client/KanheClient.java        客户端入口
  client/ClientPasswordStore.java         按服务器记住密码
  client/PasswordPrompt.java              接管原版进服流程
  client/PasswordScreen.java              密码界面（掩码输入）
  client/LanguageInjector.java            自己注入中英文翻译（Fabric Loader 不带 mod 资源包）
  client/mixin/ConnectScreenMixin.java                进服前弹窗
  client/mixin/ClientHandshakePacketListenerImplMixin.java  失败后重新弹窗
  client/mixin/ClientLanguageMixin.java               翻译注入点
src/main/resources/
  fabric.mod.json  kanhe.mixins.json  kanhe.client.mixins.json
  assets/kanhe/lang/{en_us,zh_cn}.json
```

---

## 6. 已做的验证（开发时实测）

服务端：Fabric 0.19.5 独立服务器（离线模式，端口 25566），日志显示
`Loading 5 mods: ... kanhe 1.0.0`、`Mappings not present!`（确认免混淆）。

原始 Minecraft 握手探测（不带/带/带错 `_id` 各发一次 status ping）：

| 情况 | 结果 |
| --- | --- |
| 不带 `_id` 的 status ping | `closed without data`（静默拒绝，看不到服务器） |
| 带正确 `_id` | `status ok {"description":"Kanhe test server", ...}` |
| 带错误 `_id` | `closed without data` |

走 RCON 验证管理员命令：

```
> kanhe show     -> Server password: 123456 (enabled)
> kanhe reset    -> New server password: 654321
旧密码 123456 再探测 -> closed without data
新密码 654321 再探测 -> status ok
```

客户端：用离线账号直连服务器，日志依次出现

```
[Render thread/INFO]: Asking for the join password of 127.0.0.1:25566
[Render thread/INFO]: Connecting to 127.0.0.1:25566 with a 6 digit join password
```

服务端：

```
[Server thread/INFO]: Dev[/127.0.0.1:6969] logged in with entity id 1 at (9.5, -60.0, 0.5)
[Server thread/INFO]: System chat: Dev joined the game
```

即：弹窗 → 掩码输入密码 → 通过校验 → 成功进入服务器。

---

## 7. 仓库内容

| 路径 | 说明 |
| --- | --- |
| `src/main/java/` | Mod 源码（16 个 .java） |
| `src/main/resources/` | `fabric.mod.json`、2 个 mixin 配置、中英文文本 |
| `build.gradle` / `settings.gradle` / `gradle.properties` | Gradle + Loom 构建配置 |
| `gradlew` / `gradlew.bat` / `gradle/wrapper/` | Gradle Wrapper（固定 Gradle 9.7.1） |
| `LICENSE` | MIT |
| `README.md` | 本文档 |
