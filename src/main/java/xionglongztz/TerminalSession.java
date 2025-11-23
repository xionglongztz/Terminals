package xionglongztz;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getLogger;
import static xionglongztz.Terminals.PF;

public class TerminalSession {
    private final Player player;
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private boolean suspended;
    private boolean active;

    public TerminalSession(Player player) throws IOException {
        this.player = player;
        this.suspended = false;
        this.active = true;
        // 启动系统终端
        startTerminal();
    }
    private void startTerminal() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows系统
            process = new ProcessBuilder("cmd.exe").start();
        } else {
            // Linux/Unix系统
            process = new ProcessBuilder("/bin/bash").start();
        }

        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        // 启动输出读取线程
        startOutputReader();

        // 发送欢迎消息
        player.sendMessage(colorize(PF + "&2终端已启动 - " + (os.contains("win") ? "Windows CMD" : "Linux Bash")));
    }
    private void startOutputReader() {
        Thread outputThread = new Thread(() -> {
            try {
                // 获取系统默认编码（Windows中文系统通常是GBK）
                String systemEncoding = System.getProperty("sun.jnu.encoding", "GBK");
                // 创建使用系统编码的Reader
                InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), systemEncoding);
                BufferedReader reader = new BufferedReader(inputStreamReader);

                String line;
                while (active && (line = reader.readLine()) != null) {

                    final String outputLine = line;
                    // 在主线程中发送消息给玩家
                    org.bukkit.Bukkit.getScheduler().runTask(
                            Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Terminals")),
                            () -> player.sendMessage("§7" + outputLine)
                    );
                }
            } catch (IOException e) {
                if (active) {
                    org.bukkit.Bukkit.getScheduler().runTask(
                            Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Terminals")),
                            () -> {
                                player.sendMessage(colorize(PF + "&c终端输出读取错误!请在控制台查看详情。"));
                                getLogger().severe("终端输出读取错误: " + e.getMessage());
                            }
                    );
                }
            }
        });
        outputThread.setDaemon(true);
        outputThread.start();
    }
    public void executeCommand(String command) {
        if (!active || suspended) {
            return;
        }
        try {
            String systemEncoding = System.getProperty("sun.jnu.encoding", "GBK");
            // 使用正确编码写入命令
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream(), systemEncoding);
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
            // 发送命令到终端
            writer.write(command);
            writer.newLine();
            writer.flush();

            // 显示执行的命令
            player.sendMessage("§e$ " + command);

        } catch (IOException e) {
            player.sendMessage(colorize(PF + "&c发送命令到终端时出错!请在控制台查看详情。"));
            getLogger().severe("发送命令到终端时出错: " + e.getMessage());
        }
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }
    public boolean isSuspended() {
        return suspended;
    }
    public void exit() {
        if (!active) return;

        active = false;
        try {
            // 发送退出命令
            if (writer != null) {
                writer.write("exit");
                writer.newLine();
                writer.flush();

                // 等待进程正常退出
                if (process != null && process.isAlive()) {
                    process.waitFor(3, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            // 忽略退出时的错误
        } finally {
            closeResources();
        }
    }
    public void terminate() {
        if (!active) return;

        active = false;
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
        closeResources();
    }

    private void closeResources() {
        try {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (process != null) {
                process.destroy();
            }
        } catch (IOException e) {
            // 忽略关闭时的错误
        }
    }
    private String colorize(String message) {
        // 转换颜色字符
        return message.replace('&', '§');
    }// 颜色代码转换
}