package org.setbk.dLAuthLogin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.setbk.dLAuthLogin.DLAuthLogin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.setbk.dLAuthLogin.utils.PasswordUtils;

public class RegisterCommand implements CommandExecutor {
    
    private final DLAuthLogin plugin;
    
    public RegisterCommand(DLAuthLogin plugin) {
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
        if (!player.hasPermission("dlauthlogin.register")) {
            plugin.getMessageManager().sendSimpleMessage(player, "general.no_permission");
            return true;
        }
        
        // Verificar se já está registrado
        if (plugin.getAuthManager().isRegistered(uuid)) {
            plugin.getMessageManager().sendBoxMessage(player, "register.already_registered");
            return true;
        }
        
        // Verificar argumentos
        if (args.length != 2) {
            plugin.getMessageManager().sendBoxMessage(player, "register.usage");
            return true;
        }
        
        String password = args[0];
        String confirmPassword = args[1];
        
        // Verificar se as senhas coincidem
        if (!password.equals(confirmPassword)) {
            plugin.getMessageManager().sendBoxMessage(player, "register.password_mismatch");
            return true;
        }
        
        // Verificar força da senha
        if (!PasswordUtils.isPasswordStrong(password, plugin.getConfigManager().getPasswordStrength())) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("min_length", String.valueOf(6));
            placeholders.put("strength_required", String.valueOf(plugin.getConfigManager().getPasswordStrength()));
            placeholders.put("current_strength", String.valueOf(PasswordUtils.getPasswordStrength(password)));
            plugin.getMessageManager().sendBoxMessage(player, "register.password_too_weak", placeholders);
            return true;
        }
        
        // Verificar comprimento da senha
        if (password.length() < 6) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("min_length", String.valueOf(6));
            plugin.getMessageManager().sendBoxMessage(player, "register.password_too_short", placeholders);
            return true;
        }
        
        if (password.length() > 32) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max_length", String.valueOf(32));
            plugin.getMessageManager().sendBoxMessage(player, "register.password_too_long", placeholders);
            return true;
        }
        
        // Tentar registrar
        boolean success = plugin.getAuthManager().registerPlayer(player, password, confirmPassword);
        
        if (success) {
            // Registro bem-sucedido
            plugin.getMessageManager().sendBoxMessage(player, "register.success");
            
            // Fazer login automático se configurado
            if (plugin.getConfigManager().getConfig().getBoolean("auto_login_after_register", false)) {
                plugin.getAuthManager().loginPlayer(player, password);
                plugin.getProtectionManager().removeProtectedPlayer(uuid);
                plugin.getMessageManager().sendBoxMessage(player, "login.success");
            }
            
        } else {
            // Registro falhou
            plugin.getMessageManager().sendBoxMessage(player, "error.unknown_error");
        }
        
        return true;
    }
}
