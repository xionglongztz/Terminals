package xionglongztz;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Terminals extends JavaPlugin implements Listener {
    private Map<String, TerminalSession> playerSessions;
    private static final String EXECUTE_PERMISSION = "terminals.execute";
    private final AtomicLong enableTime = new AtomicLong();// 用于计时
    public static final String PF = "&r[&bTerminals&r] ";// 前缀
    private static Boolean adminPermission = false;// 是否有管理员权限
    private enum osType {
        Windows,
        Linux
    }
    private static osType currentOS;// 操作系统枚举
    public static FileConfiguration config;// 配置文件
    private static final List<String> SUB_COMMANDS = Arrays.asList(
            "new", "exit", "suspend", "continue", "end", "help");// 子命令
    // 载入与退出
    @Override
    public void onEnable() {
        enableTime.set(System.currentTimeMillis());// 启动计时器
        // 注册日志记录器类
        TerminalLogger.initialize(this);
        TerminalLogger.log("------------------------------");
        TerminalLogger.log("[*] 插件已载入!");
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();// 获得控制台消息sender
        console.sendMessage(colorize(PF + "&b版本 &7- v" + this.getDescription().getVersion()));
        console.sendMessage(colorize(PF + "&b作者 &7- " + this.getDescription().getAuthors().getFirst()));
        console.sendMessage(colorize(PF + "&b环境 &7- " + Bukkit.getName()));
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Windows系统
            console.sendMessage(colorize(PF + "&b系统 &7- Windows"));
            currentOS = osType.Windows;
            if (IsWindowsAdmin()) {
                console.sendMessage(colorize(PF + "&b管理员 &7- &a是"));
                TerminalLogger.log("[@] 管理员:是");
                adminPermission = true;
            } else {
                console.sendMessage(colorize(PF + "&b管理员 &7- &c否"));
                TerminalLogger.log("[@] 管理员:否");
            }
        } else {
            // Linux/Unix系统
            console.sendMessage(colorize(PF + "&b系统 &7- Linux"));
            currentOS = osType.Linux;
            if (IsUnixAdmin()){
                console.sendMessage(colorize(PF + "&b管理员 &7- &a是"));
                TerminalLogger.log("[@] 管理员:是");
            } else {
                console.sendMessage(colorize(PF + "&b管理员 &7- &c否"));
                TerminalLogger.log("[@] 管理员:否");
            }
        }
        console.sendMessage(colorize((PF + "&b路径 &7- ") + System.getProperty("user.dir")));
        console.sendMessage(colorize(PF + "&b  _____                   _             _     "));
        console.sendMessage(colorize(PF + "&b |_   _|__ _ __ _ __ ___ (_)_ __   __ _| |___ "));
        console.sendMessage(colorize(PF + "&b   | |/ _ \\ '__| '_ ` _ \\| | '_ \\ / _` | / __|"));
        console.sendMessage(colorize(PF + "&b   | |  __/ |  | | | | | | | | | | (_| | \\__ \\"));
        console.sendMessage(colorize(PF + "&b   |_|\\___|_|  |_| |_| |_|_|_| |_|\\__,_|_|___/"));
        console.sendMessage(colorize(PF));
        // 读取配置
        saveDefaultConfig();
        config = getConfig();
        // 初始化会话存储
        playerSessions = new HashMap<>();
        // 注册权限节点
        Permission permission = new Permission(EXECUTE_PERMISSION, PermissionDefault.OP);
        Bukkit.getPluginManager().addPermission(permission);
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);
        if (!config.getBoolean("Confirm")) {
            console.sendMessage(colorize(PF + "&e/!\\ &c请先在 config.yml 中进行确认, 然后重启服务器"));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        long totalTime = System.currentTimeMillis() - enableTime.get();// 记录启动时间
        console.sendMessage(colorize(PF + "&b加载完成&7(" + totalTime + "ms)"));
    }
    @Override
    public void onDisable() {
        // 关闭所有终端会话
        for (TerminalSession session : playerSessions.values()) {
            session.terminate();
        }
        playerSessions.clear();
        TerminalLogger.log("[*] 插件已卸载!");
    }
    // 命令处理
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(colorize(PF + "&c只有玩家可以使用此命令!"));
            return true;
        }
        // 检查权限
        if (!player.hasPermission(EXECUTE_PERMISSION)) {
            sender.sendMessage(colorize(PF + "&c你没有权限使用此命令!"));
            return true;
        }
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "new":
                createNewTerminal(player);
                break;
            case "suspend":
                suspendTerminal(player);
                break;
            case "continue":
                continueTerminal(player);
                break;
            case "exit":
                exitTerminal(player);
                break;
            case "end":
                endTerminal(player);
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }
    private void sendHelp(Player player) {
        player.sendMessage(colorize("&b===== Terminals ====="));
        player.sendMessage(colorize("&b/terminal new &7- 创建新的终端会话"));
        player.sendMessage(colorize("&b/terminal suspend &7- 挂起当前终端"));
        player.sendMessage(colorize("&b/terminal continue &7- 恢复挂起的终端"));
        player.sendMessage(colorize("&b/terminal exit &7- 退出终端"));
        player.sendMessage(colorize("&b/terminal end &7- 终止终端"));
        player.sendMessage(currentOS == osType.Windows ? colorize("&b当前操作系统:Windows") : colorize("&b当前操作系统:Linux"));
        player.sendMessage(colorize(adminPermission ? "&2当前终端具有管理员权限" : "&6当前终端不具有管理员权限"));
        player.sendMessage(colorize("&b注:Shift+空手+攻击 可以向终端发送Ctrl+C"));
    }// 显示帮助信息
    private void createNewTerminal(Player player) {
        String playerName = player.getName();
        // 检查是否已有会话
        if (playerSessions.containsKey(playerName)) {
            player.sendMessage(colorize(PF + "&c你已有一个活动的终端会话!"));
            return;
        }
        try {
            TerminalSession session = new TerminalSession(player,this);
            playerSessions.put(playerName, session);
            Bukkit.getServer().getConsoleSender().sendMessage(colorize(PF + "[&a+&r] &a" + playerName + " 创建了终端会话!"));
            TerminalLogger.log("[+] " + playerName + " 创建了终端会话!");
            player.sendMessage(colorize(PF + "&a终端会话已创建! 现在你输入的所有消息都将作为终端命令执行。"));
            player.sendMessage(colorize(PF + "&a输入 '/terminal suspend' 来挂起终端，'/terminal exit' 来退出。"));
        } catch (Exception e) {
            player.sendMessage(colorize(PF + "&c创建终端会话时出错!请在控制台查看详情。"));
            getLogger().severe("创建终端会话失败: " + e.getMessage());
        }
    }// 创建新的终端
    private void suspendTerminal(Player player) {
        TerminalSession session = playerSessions.get(player.getName());
        if (session == null) {
            player.sendMessage(colorize(PF + "&c你没有活动的终端会话!"));
            return;
        }
        if (!session.isSuspended()){
            session.setSuspended(true);
            TerminalLogger.log("[=] " + player.getName() + " 挂起了终端会话!");
            Bukkit.getServer().getConsoleSender().sendMessage(colorize(PF + "[&e=&r] &e" + player.getName() + " 挂起了终端会话!"));
            player.sendMessage(colorize(PF + "&e终端已挂起。现在你的消息将正常发送到聊天栏。"));
            player.sendMessage(colorize(PF + "&e输入 '/terminal continue' 回到终端。"));
        } else {
            player.sendMessage(colorize(PF + "&c当前终端已经挂起! 输入 '/terminal continue' 回到终端。"));
        }
    }// 挂起当前终端
    private void continueTerminal(Player player) {
        TerminalSession session = playerSessions.get(player.getName());
        if (session == null) {
            player.sendMessage(colorize(PF + "&c你没有活动的终端会话!"));
            return;
        }
        if (session.isSuspended()){
            session.setSuspended(false);
            TerminalLogger.log("[=] " + player.getName() + " 恢复了终端会话!");
            Bukkit.getServer().getConsoleSender().sendMessage(colorize(PF + "[&e=&r] &e" + player.getName() + " 恢复了终端会话!"));
            player.sendMessage(colorize(PF + "&a已回到终端模式。输入的命令将在终端中执行。"));
        } else {
            player.sendMessage(colorize(PF + "&c当前终端已经恢复! 输入 '/terminal suspend' 挂起终端。"));
        }
    }// 回到终端
    private void exitTerminal(Player player) {
        String playerName = player.getName();
        TerminalSession session = playerSessions.get(playerName);
        if (session == null) {
            player.sendMessage(colorize(PF + "&c你没有活动的终端会话!"));
            return;
        }
        session.exit();
        playerSessions.remove(playerName);
        TerminalLogger.log("[-] " + playerName + " 关闭了终端会话!");
        Bukkit.getServer().getConsoleSender().sendMessage(colorize(PF + "[&c-&r] &c" + player.getName() + " 关闭了终端会话!"));
        player.sendMessage(colorize(PF + "&c终端会话已正常退出。"));
    }// 退出终端
    private void endTerminal(Player player) {
        String playerName = player.getName();
        TerminalSession session = playerSessions.get(playerName);
        if (session == null) {
            player.sendMessage(colorize(PF + "&c你没有活动的终端会话!"));
            return;
        }
        session.terminate();
        playerSessions.remove(playerName);
        TerminalLogger.log("[×] " + playerName + " 终止了终端会话!");
        Bukkit.getServer().getConsoleSender().sendMessage(colorize(PF + "[&c×&r] &c" + player.getName() + " 终止了终端会话!"));
        player.sendMessage(colorize(PF + "&c终端会话已被强制终止。"));
    }// 终止终端
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        TerminalSession session = playerSessions.get(player.getName());

        // 如果玩家有终端会话且未挂起，则处理命令
        if (session != null && session.isActive() && !session.isSuspended()) {
            event.setCancelled(true); // 取消原版聊天消息

            String message = event.getMessage();

            // 异步执行命令
            CompletableFuture.runAsync(() -> {
                try {
                    session.executeCommand(message);
                    TerminalLogger.log("[!] " + player.getName() + " 执行:" + message);
                    Bukkit.getServer().getConsoleSender().sendMessage(colorize(PF + "[&e!&r] &e" + player.getName() + " 执行:&7") + message);
                } catch (Exception e) {
                    player.sendMessage(colorize(PF + "&c执行命令时出错!请在控制台查看详情。"));
                    getLogger().severe("执行终端命令失败: " + e.getMessage());
                }
            });
        }
    }
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(command.getName().equalsIgnoreCase("terminal"))) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return SUB_COMMANDS.stream()
                    .filter(sub -> sub.startsWith(partial))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("new") || sub.equals("suspend") || sub.equals("exit") || sub.equals("end") || sub.equals("continue") || sub.equals("help")) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }// 修改TAB自动补全的候选词
    // 其他事件监听
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 检查是否是空手+Shift+左键攻击
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (player.isSneaking() &&
                (player.getInventory().getItemInMainHand().getType() == Material.AIR)) {

                TerminalSession session = playerSessions.get(player.getName());
                if (session != null && !session.isSuspended()) {
                    event.setCancelled(true); // 取消原版攻击动作
                    session.sendCtrlC();
                    player.sendMessage(colorize(PF + "&c已发送中断信号 (Ctrl+C)"));
                }
            }
        }
    }
    // 判断管理员权限
    private boolean IsUnixAdmin() {
        try {
            // 检查用户ID，root用户的UID为0
            Process process = Runtime.getRuntime().exec(new String[]{"id -u"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String userId = reader.readLine().trim();
            process.waitFor();
            return "0".equals(userId);
        } catch (Exception e) {
            getLogger().warning("无法获取当前进程执行权限信息: " + e.getMessage());
        }
        return false;
    }// 判断当前进程是否具有管理员权限(Linux)
    private boolean IsWindowsAdmin() {
        try {
            // 使用一行PowerShell命令直接返回bool结果
            Process process = Runtime.getRuntime().exec(new String[]{
                    "powershell", "-Command", "([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] 'Administrator')"
            });
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String result = reader.readLine();
            reader.close();
            process.waitFor();
            return "True".equalsIgnoreCase(result);
        } catch (Exception e) {
            getLogger().warning("无法获取当前进程执行权限信息: " + e.getMessage());
        }
        return false;
    }// 判断当前进程是否具有管理员权限(Windows)
    // 辅助函数
    private String colorize(String message) {
        // 转换颜色字符
        return message.replace('&', '§');
    }// 颜色代码转换
    // 添加会话管理方法
    public void removeSession(String playerName) {
        // 哈基米南北绿豆, 阿西噶呀库奶龙
        playerSessions.remove(playerName);
    }
}