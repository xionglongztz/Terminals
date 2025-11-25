package xionglongztz;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TerminalLogger {
    private final JavaPlugin plugin;
    private final String logFolder;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static TerminalLogger instance;
    public static void initialize(JavaPlugin plugin) {
        instance = new TerminalLogger(plugin);
    }
    public static TerminalLogger getInstance() {
        return instance;
    }
    // 静态便捷方法
    public static void log(String contents) {
        if (instance != null) {
            instance._log(contents);
        }
    }
    // 终端日志类
    public TerminalLogger(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logFolder = plugin.getDataFolder().getPath() + File.separator + "Logs";
        createLogFolder();
    }
    // 创建日志目录
    private void createLogFolder() {
        File folder = new File(logFolder);
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().severe("无法创建目录: Terminals\\Logs");
        }
    }
    // 记录终端日志
    public void _log(String contents) {
        try {
            String date = dateFormat.format(new Date());
            String time = timeFormat.format(new Date());
            String logFile = logFolder + File.separator + date + ".log";

            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(String.format("[%s] %s\n", time, contents));
            }
        } catch (IOException e) {
            plugin.getLogger().severe("记录终端日志失败: " + e.getMessage());
        }
    }
}