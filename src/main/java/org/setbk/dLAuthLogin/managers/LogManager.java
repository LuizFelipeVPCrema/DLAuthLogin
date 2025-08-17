package org.setbk.dLAuthLogin.managers;

import org.bukkit.entity.Player;
import org.setbk.dLAuthLogin.DLAuthLogin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class LogManager {
    
    private final DLAuthLogin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private File logFile;
    
    public LogManager(DLAuthLogin plugin) {
        this.plugin = plugin;
        initializeLogFile();
    }
    
    private void initializeLogFile() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            logFile = new File(dataFolder, "auth.log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao criar arquivo de log: " + e.getMessage());
        }
    }
    
    public void logLoginAttempt(Player player) {
        if (!plugin.getConfigManager().isLogLoginAttempts()) {
            return;
        }
        
        String message = String.format("[LOGIN_ATTEMPT] %s (%s) tentou fazer login", 
                player.getName(), player.getAddress().getAddress().getHostAddress());
        writeLog(message);
    }
    
    public void logLoginSuccess(Player player) {
        if (!plugin.getConfigManager().isLogLoginAttempts()) {
            return;
        }
        
        String message = String.format("[LOGIN_SUCCESS] %s (%s) fez login com sucesso", 
                player.getName(), player.getAddress().getAddress().getHostAddress());
        writeLog(message);
    }
    
    public void logLoginFailed(Player player) {
        if (!plugin.getConfigManager().isLogLoginAttempts()) {
            return;
        }
        
        String message = String.format("[LOGIN_FAILED] %s (%s) falhou no login", 
                player.getName(), player.getAddress().getAddress().getHostAddress());
        writeLog(message);
    }
    
    public void logRegistration(Player player) {
        if (!plugin.getConfigManager().isLogRegistrations()) {
            return;
        }
        
        String message = String.format("[REGISTRATION] %s (%s) registrou uma nova conta", 
                player.getName(), player.getAddress().getAddress().getHostAddress());
        writeLog(message);
    }
    
    public void logPasswordChange(Player player) {
        if (!plugin.getConfigManager().isLogPasswordChanges()) {
            return;
        }
        
        String message = String.format("[PASSWORD_CHANGE] %s (%s) alterou sua senha", 
                player.getName(), player.getAddress().getAddress().getHostAddress());
        writeLog(message);
    }
    
    public void logUnregister(Player player) {
        if (!plugin.getConfigManager().isLogUnregistrations()) {
            return;
        }
        
        String message = String.format("[UNREGISTER] %s (%s) removeu sua conta", 
                player.getName(), player.getAddress().getAddress().getHostAddress());
        writeLog(message);
    }
    
    public void logAdminAction(String adminName, String action, String targetPlayer) {
        String message = String.format("[ADMIN_ACTION] %s executou %s em %s", 
                adminName, action, targetPlayer);
        writeLog(message);
    }
    
    public void logSecurityEvent(String event, String details) {
        String message = String.format("[SECURITY] %s: %s", event, details);
        writeLog(message);
    }
    
    public void logDatabaseError(String error) {
        String message = String.format("[DATABASE_ERROR] %s", error);
        writeLog(message);
        plugin.getLogger().log(Level.SEVERE, "Erro no banco de dados: " + error);
    }
    
    public void logEncryptionError(String error) {
        String message = String.format("[ENCRYPTION_ERROR] %s", error);
        writeLog(message);
        plugin.getLogger().log(Level.SEVERE, "Erro na criptografia: " + error);
    }
    
    public void logSessionEvent(String event, String playerName, String details) {
        String message = String.format("[SESSION] %s - %s: %s", playerName, event, details);
        writeLog(message);
    }
    
    public void logConfigReload(String adminName) {
        String message = String.format("[CONFIG_RELOAD] %s recarregou a configuração", adminName);
        writeLog(message);
    }
    
    public void logPluginStart() {
        String message = "[PLUGIN_START] DLAuthLogin iniciado";
        writeLog(message);
    }
    
    public void logPluginStop() {
        String message = "[PLUGIN_STOP] DLAuthLogin parado";
        writeLog(message);
    }
    
    private void writeLog(String message) {
        if (logFile == null) {
            return;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            String timestamp = dateFormat.format(new Date());
            writer.println(String.format("[%s] %s", timestamp, message));
        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao escrever no arquivo de log: " + e.getMessage());
        }
    }
    
    public void logToConsole(String message) {
        plugin.getLogger().info(message);
    }
    
    public void logWarning(String message) {
        plugin.getLogger().warning(message);
    }
    
    public void logError(String message) {
        plugin.getLogger().severe(message);
    }
    
    public void logDebug(String message) {
        // Log de debug - pode ser habilitado via configuração
        if (plugin.getConfigManager().getConfig().getBoolean("debug.enabled", false)) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
    
    public File getLogFile() {
        return logFile;
    }
    
    public void clearLogFile() {
        if (logFile != null && logFile.exists()) {
            try (PrintWriter writer = new PrintWriter(logFile)) {
                writer.print(""); // Limpar o arquivo
            } catch (IOException e) {
                plugin.getLogger().warning("Erro ao limpar arquivo de log: " + e.getMessage());
            }
        }
    }
    
    public long getLogFileSize() {
        if (logFile != null && logFile.exists()) {
            return logFile.length();
        }
        return 0;
    }
}
