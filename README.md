# Terminals - Minecraft 终端插件
> [!WARNING]  
> **此插件允许在服务器系统上执行任意命令，请谨慎使用！**

![Minecraft](https://img.shields.io/badge/Minecraft-1.13+-green.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Version](https://img.shields.io/badge/Version-1.0.1-orange.svg)

## 📖 简介

Terminals 是一个革命性的 Minecraft 服务器插件，它允许具有权限的玩家在游戏内直接执行系统终端命令。这打破了游戏与现实的界限，让 Minecraft 成为一个真正的"元宇宙操作系统"。

## ✨ 特性

### 🔧 核心功能
- 🖥️ **系统终端集成** - 在游戏中直接访问 Linux/Windows 命令行
- 💬 **实时交互** - 聊天栏输入直接作为终端命令执行
- 📊 **结果回显** - 命令执行结果实时显示在游戏客户端
- 🎮 **会话管理** - 完整的终端会话生命周期管理

### 🛡️ 安全特性
- 🔐 **权限控制** - 默认仅 OP 可使用，可配置权限节点
- ⚠️ **安全确认** - 必须手动确认风险后才能启用插件
- 📝 **操作日志** - 所有命令执行都会被记录到文件

### 🎯 便捷功能
- ⚡ **快捷命令** - 支持 `/term` 短命令
- 🖱️ **快捷键** - 空手+Shift+左键发送 Ctrl+C 中断信号
- 💾 **自动清理** - 终端退出时自动释放资源
- 🔄 **会话挂起** - 可临时挂起终端而不关闭

## 📥 安装

1. 下载 `Terminals.jar` 文件
2. 将其放入服务器的 `plugins` 文件夹
3. 重启服务器
4. 编辑 `plugins/Terminals/config.yml`
5. 将 `Confirme: false` 改为 `true`
6. 再次重启服务器

## 🚀 使用方法

### 基础命令
| 命令               | 描述       | 权限                  |
|------------------|----------|---------------------|
| `/term new`      | 创建新的终端会话 | `terminals.execute` |
| `/term suspend`  | 挂起当前终端   | `terminals.execute` |
| `/term continue` | 恢复挂起的终端  | `terminals.execute` |
| `/term exit`     | 正常退出终端   | `terminals.execute` |
| `/term end`      | 强制终止终端   | `terminals.execute` |
| `/term help`     | 查看命令帮助   | `terminals.execute` |

### 使用示例
```minecraft
# 创建终端
/terminal new

# 查看系统信息 (Linux)
ls -la
df -h
free -m

# 挂起终端 (返回正常聊天)
/terminal suspend

# 恢复终端
/terminal continue

# 退出终端
/terminal exit
```

## ⚙️ 配置

```yaml
# 安全确认 - 必须设为 true 才能继续使用本插件
Confirm: false
```

## 🔒 权限节点

| 权限节点                | 描述       | 默认 |
|---------------------|----------|----|
| `terminals.execute` | 使用所有终端命令 | op |

## 📊 日志系统
插件会在 `plugins/Terminals/Logs/` 目录下创建按日期命名的日志文件：
```text
plugins/Terminals/Logs/
├── 2024-01-01.log
├── 2024-01-02.log
└── ...
```

## 🛡️ 安全警告

### ⚠️ 极高风险操作
此插件允许执行任意系统命令，可能导致：
- 🗑️ **数据丢失** - 误删服务器文件
- 🔓 **权限提升** - 获取系统更高权限
- 🌐 **网络攻击** - 将服务器作为攻击跳板
- 💸 **资源滥用** - 植入挖矿程序等恶意软件

### ✅ 安全使用建议
1. **仅限受信任的管理员**使用
2. **定期备份**重要数据
3. 启用**命令白名单**功能
4. 监控**日志文件**中的可疑活动
5. 考虑在**容器环境**中运行服务器

## ⚠️ 免责声明

此插件按"原样"提供，作者不对使用此插件造成的任何损害负责。使用者应自行承担所有风险，包括但不限于数据丢失、系统损坏或安全漏洞。

---

**享受在 Minecraft 中探索真实世界的乐趣！** 🎮➡️💻