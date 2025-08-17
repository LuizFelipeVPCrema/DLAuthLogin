package org.setbk.dLAuthLogin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.setbk.dLAuthLogin.DLAuthLogin;

import java.util.UUID;

public class PlayerListener implements Listener {
    
    private final DLAuthLogin plugin;
    
    public PlayerListener(DLAuthLogin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Verificar se o jogador tem bypass
        if (plugin.getProtectionManager().hasBypassPermission(uuid)) {
            return;
        }
        
        // Garantir que o jogador não esteja em estado inconsistente
        // Limpar qualquer estado residual
        plugin.getAuthManager().forceCleanupPlayerState(uuid);
        
        // Forçar verificação de estado
        plugin.getProtectionManager().forceStateCheck(uuid);
        
        // Verificar se o jogador está registrado
        if (!plugin.getAuthManager().isRegistered(uuid)) {
            // Jogador não registrado, mostrar mensagem de registro
            player.sendMessage(plugin.getMessageManager().getMessage("info.register_required"));
            plugin.getProtectionManager().addProtectedPlayer(uuid);
            
            // Definir timeout de login
            plugin.getAuthManager().setLoginTimeout(uuid);
            
            return;
        }
        
        // Verificar se tem sessão válida
        if (plugin.getSessionManager().hasValidSession(uuid)) {
            // Sessão válida, fazer login automático
            plugin.getAuthManager().setLoggedIn(uuid);
            plugin.getProtectionManager().removeProtectedPlayer(uuid);
            
            player.sendMessage(plugin.getMessageManager().getMessage("session.restored"));
            
            // Log da sessão restaurada
            plugin.getLogManager().logSessionEvent("restored", player.getName(), "Auto-login from session");
            
            return;
        }
        
        // Jogador registrado mas sem sessão válida
        player.sendMessage(plugin.getMessageManager().getMessage("info.login_required"));
        plugin.getProtectionManager().addProtectedPlayer(uuid);
        
        // Definir timeout de login
        plugin.getAuthManager().setLoginTimeout(uuid);
        
        // Log da tentativa de login
        plugin.getLogManager().logLoginAttempt(player);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Remover proteção
        plugin.getProtectionManager().removeProtectedPlayer(uuid);
        
        // Remover timeout de login
        plugin.getAuthManager().removeLoginTimeout(uuid);
        
        // Sempre fazer logout quando o jogador sai
        // Isso garante que o estado seja limpo corretamente
        plugin.getAuthManager().logoutPlayer(uuid);
        
        // Log do logout
        plugin.getLogManager().logSecurityEvent("player_quit", player.getName());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Remover proteção
        plugin.getProtectionManager().removeProtectedPlayer(uuid);
        
        // Remover timeout de login
        plugin.getAuthManager().removeLoginTimeout(uuid);
        
        // Fazer logout
        plugin.getAuthManager().logoutPlayer(uuid);
    }
}
