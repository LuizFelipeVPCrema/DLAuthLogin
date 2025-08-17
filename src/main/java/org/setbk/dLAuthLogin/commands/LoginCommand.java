package org.setbk.dLAuthLogin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.setbk.dLAuthLogin.DLAuthLogin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.setbk.dLAuthLogin.models.Account;

public class LoginCommand implements CommandExecutor {
    
    private final DLAuthLogin plugin;
    
    public LoginCommand(DLAuthLogin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("general.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        
        // Verificar permissão
        if (!player.hasPermission("dlauthlogin.login")) {
            player.sendMessage(plugin.getMessageManager().getMessage("general.no_permission"));
            return true;
        }
        
        // Verificar se já está logado
        if (plugin.getAuthManager().isLoggedIn(uuid)) {
            // Se está logado mas ainda está protegido, remover proteção
            if (plugin.getProtectionManager().isProtected(uuid)) {
                plugin.getProtectionManager().removeProtectedPlayer(uuid);
                player.sendMessage(plugin.getMessageManager().getMessage("login.protection_removed"));
            } else {
                player.sendMessage(plugin.getMessageManager().getMessage("login.already_logged"));
            }
            return true;
        }
        
        // Verificar se está registrado
        if (!plugin.getAuthManager().isRegistered(uuid)) {
            player.sendMessage(plugin.getMessageManager().getMessage("login.not_registered"));
            return true;
        }
        
        // Verificar argumentos
        if (args.length != 1) {
            player.sendMessage(plugin.getMessageManager().getMessage("login.usage"));
            return true;
        }
        
        String password = args[0];
        
        // Tentar fazer login
        boolean success = plugin.getAuthManager().loginPlayer(player, password);
        
        if (success) {
            // Login bem-sucedido
            player.sendMessage(plugin.getMessageManager().getMessage("login.success"));
            
            // Log da tentativa
            plugin.getLogManager().logLoginAttempt(player);
            
        } else {
            // Login falhou
            Account account = plugin.getDatabaseManager().getAccount(uuid);
            if (account != null && account.isLocked()) {
                long remainingTime = (account.getLockedUntil().getTime() - System.currentTimeMillis()) / 1000;
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("time", String.valueOf(remainingTime));
                player.sendMessage(plugin.getMessageManager().getMessage("login.locked", placeholders));
            } else {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("attempt", String.valueOf(account != null ? account.getLoginAttempts() : 1));
                placeholders.put("max_attempts", String.valueOf(plugin.getConfigManager().getMaxLoginAttempts()));
                player.sendMessage(plugin.getMessageManager().getMessage("login.failed", placeholders));
            }
        }
        
        return true;
    }
}
