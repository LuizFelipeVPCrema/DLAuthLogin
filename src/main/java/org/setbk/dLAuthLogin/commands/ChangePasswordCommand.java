package org.setbk.dLAuthLogin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.setbk.dLAuthLogin.DLAuthLogin;
import org.setbk.dLAuthLogin.utils.PasswordUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChangePasswordCommand implements CommandExecutor {
    
    private final DLAuthLogin plugin;
    
    public ChangePasswordCommand(DLAuthLogin plugin) {
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
        if (!player.hasPermission("dlauthlogin.changepassword")) {
            player.sendMessage(plugin.getMessageManager().getMessage("general.no_permission"));
            return true;
        }
        
        // Verificar se está logado
        if (!plugin.getAuthManager().isLoggedIn(uuid)) {
            player.sendMessage(plugin.getMessageManager().getMessage("info.login_required"));
            return true;
        }
        
        // Verificar argumentos
        if (args.length != 3) {
            player.sendMessage(plugin.getMessageManager().getMessage("changepassword.usage"));
            return true;
        }
        
        String currentPassword = args[0];
        String newPassword = args[1];
        String confirmNewPassword = args[2];
        
        // Verificar se as novas senhas coincidem
        if (!newPassword.equals(confirmNewPassword)) {
            player.sendMessage(plugin.getMessageManager().getMessage("changepassword.new_password_mismatch"));
            return true;
        }
        
        // Verificar se a nova senha é diferente da atual
        if (currentPassword.equals(newPassword)) {
            player.sendMessage(plugin.getMessageManager().getMessage("changepassword.new_password_same"));
            return true;
        }
        
        // Verificar força da nova senha
        if (!PasswordUtils.isPasswordStrong(newPassword, plugin.getConfigManager().getPasswordStrength())) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("min_length", String.valueOf(6));
            player.sendMessage(plugin.getMessageManager().getMessage("register.password_too_weak", placeholders));
            return true;
        }
        
        // Verificar comprimento da nova senha
        if (newPassword.length() < 6) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("min_length", String.valueOf(6));
            player.sendMessage(plugin.getMessageManager().getMessage("register.password_too_short", placeholders));
            return true;
        }
        
        if (newPassword.length() > 32) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max_length", String.valueOf(32));
            player.sendMessage(plugin.getMessageManager().getMessage("register.password_too_long", placeholders));
            return true;
        }
        
        // Tentar alterar senha
        boolean success = plugin.getAuthManager().changePassword(player, currentPassword, newPassword, confirmNewPassword);
        
        if (success) {
            // Alteração bem-sucedida
            player.sendMessage(plugin.getMessageManager().getMessage("changepassword.success"));
        } else {
            // Alteração falhou
            player.sendMessage(plugin.getMessageManager().getMessage("changepassword.current_password_incorrect"));
        }
        
        return true;
    }
}
