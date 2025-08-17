package org.setbk.dLAuthLogin.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.setbk.dLAuthLogin.DLAuthLogin;

import java.io.File;

public class ConfigManager {
    
    private final DLAuthLogin plugin;
    private FileConfiguration config;
    
    public ConfigManager(DLAuthLogin plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    // Getters para configurações específicas
    public int getPasswordStrength() {
        return config.getInt("security.password_strength", 6);
    }
    
    public int getLoginTimeout() {
        return config.getInt("security.login_timeout", 60);
    }
    
    public int getMaxLoginAttempts() {
        return config.getInt("security.max_login_attempts", 3);
    }
    
    public int getLockoutDuration() {
        return config.getInt("security.lockout_duration", 300);
    }
    
    public boolean useBCrypt() {
        return config.getBoolean("security.use_bcrypt", true);
    }
    
    public int getBCryptRounds() {
        return config.getInt("security.bcrypt_rounds", 12);
    }
    
    public String getDatabaseType() {
        return config.getString("database.type", "H2");
    }
    
    public String getH2File() {
        // Sempre usar um caminho simples dentro do diretório do plugin
        File pluginDataFolder = plugin.getDataFolder();
        return pluginDataFolder.getAbsolutePath() + File.separator + "accounts";
    }
    
    public boolean isH2AutoBackup() {
        return config.getBoolean("database.h2.auto_backup", true);
    }
    
    public int getH2BackupInterval() {
        return config.getInt("database.h2.backup_interval", 24);
    }
    
    public String getDefaultLanguage() {
        return config.getString("messages.default_language", "pt_BR");
    }
    
    public String getMessagePrefix() {
        return config.getString("messages.prefix", "&8[&cDLAuth&8] &r");
    }
    
    public boolean isBlockMovement() {
        return config.getBoolean("protection.block_movement", true);
    }
    
    public boolean isBlockBlockInteraction() {
        return config.getBoolean("protection.block_block_interaction", true);
    }
    
    public boolean isBlockEntityInteraction() {
        return config.getBoolean("protection.block_entity_interaction", true);
    }
    
    public boolean isBlockItemUse() {
        return config.getBoolean("protection.block_item_use", true);
    }
    
    public boolean isBlockChat() {
        return config.getBoolean("protection.block_chat", true);
    }
    
    public boolean isBlockDamage() {
        return config.getBoolean("protection.block_damage", true);
    }
    
    public boolean isBlockTeleport() {
        return config.getBoolean("protection.block_teleport", true);
    }
    
    public boolean isBlockCommands() {
        return config.getBoolean("protection.block_commands", true);
    }
    
    public boolean isKeepSession() {
        return config.getBoolean("session.keep_session", true);
    }
    
    public int getSessionTimeout() {
        return config.getInt("session.session_timeout", 30);
    }
    
    public boolean isCheckIP() {
        return config.getBoolean("session.check_ip", false);
    }
    
    public boolean isLogLoginAttempts() {
        return config.getBoolean("logging.log_login_attempts", true);
    }
    
    public boolean isLogRegistrations() {
        return config.getBoolean("logging.log_registrations", true);
    }
    
    public boolean isLogPasswordChanges() {
        return config.getBoolean("logging.log_password_changes", true);
    }
    
    public boolean isLogUnregistrations() {
        return config.getBoolean("logging.log_unregistrations", true);
    }
}
