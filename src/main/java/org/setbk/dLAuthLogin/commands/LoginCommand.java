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
            plugin.getMessageManager().sendSimpleMessage(sender, "general.player_only");
            return true;
        }
        
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        
        // Verificar permissão
        if (!player.hasPermission("dlauthlogin.login")) {
            plugin.getMessageManager().sendSimpleMessage(player, "general.no_permission");
            return true;
        }
        
        // Verificar se já está logado
        if (plugin.getAuthManager().isLoggedIn(uuid)) {
            // Se está logado mas ainda está protegido, remover proteção
            if (plugin.getProtectionManager().isProtected(uuid)) {
                plugin.getProtectionManager().removeProtectedPlayer(uuid);
                plugin.getMessageManager().sendBoxMessage(player, "login.protection_removed");
            } else {
                plugin.getMessageManager().sendBoxMessage(player, "login.already_logged");
            }
            return true;
        }
        
        // Verificar se está registrado
        if (!plugin.getAuthManager().isRegistered(uuid)) {
            plugin.getMessageManager().sendBoxMessage(player, "login.not_registered");
            return true;
        }
        
        // Verificar argumentos
        if (args.length != 1) {
            plugin.getMessageManager().sendBoxMessage(player, "login.usage");
            return true;
        }
        
        String password = args[0];
        
        // Tentar fazer login
        boolean success = plugin.getAuthManager().loginPlayer(player, password);
        
        if (success) {
            // Login bem-sucedido
            plugin.getMessageManager().sendBoxMessage(player, "login.success");
            
            // Log da tentativa
            plugin.getLogManager().logLoginAttempt(player);
            
        } else {
            // Login falhou
            Account account = plugin.getDatabaseManager().getAccount(uuid);
            if (account != null && account.isLocked()) {
                long remainingTime = (account.getLockedUntil().getTime() - System.currentTimeMillis()) / 1000;
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("time", String.valueOf(remainingTime));
                plugin.getMessageManager().sendBoxMessage(player, "login.locked", placeholders);
            } else {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("attempt", String.valueOf(account != null ? account.getLoginAttempts() : 1));
                placeholders.put("max_attempts", String.valueOf(plugin.getConfigManager().getMaxLoginAttempts()));
                plugin.getMessageManager().sendBoxMessage(player, "login.failed", placeholders);
            }
        }
        
        return true;
    }
}
