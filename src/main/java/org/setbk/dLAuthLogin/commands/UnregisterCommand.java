package org.setbk.dLAuthLogin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.setbk.dLAuthLogin.DLAuthLogin;

import java.util.UUID;

public class UnregisterCommand implements CommandExecutor {
    
    private final DLAuthLogin plugin;
    
    public UnregisterCommand(DLAuthLogin plugin) {
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
        if (!player.hasPermission("dlauthlogin.unregister")) {
            player.sendMessage(plugin.getMessageManager().getMessage("general.no_permission"));
            return true;
        }
        
        // Verificar se está registrado
        if (!plugin.getAuthManager().isRegistered(uuid)) {
            player.sendMessage(plugin.getMessageManager().getMessage("register.already_registered"));
            return true;
        }
        
        // Verificar argumentos
        if (args.length != 1) {
            player.sendMessage(plugin.getMessageManager().getMessage("unregister.usage"));
            return true;
        }
        
        String password = args[0];
        
        // Tentar remover conta
        boolean success = plugin.getAuthManager().unregisterPlayer(player, password);
        
        if (success) {
            // Remoção bem-sucedida
            player.sendMessage(plugin.getMessageManager().getMessage("unregister.success"));
            
            // Adicionar proteção novamente
            plugin.getProtectionManager().addProtectedPlayer(uuid);
            
        } else {
            // Remoção falhou
            player.sendMessage(plugin.getMessageManager().getMessage("unregister.password_incorrect"));
        }
        
        return true;
    }
}
