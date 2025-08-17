package org.setbk.dLAuthLogin.managers;

import org.bukkit.entity.Player;
import org.setbk.dLAuthLogin.DLAuthLogin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProtectionManager {
    
    private final DLAuthLogin plugin;
    private final Map<UUID, Boolean> protectedPlayers = new HashMap<>();
    
    public ProtectionManager(DLAuthLogin plugin) {
        this.plugin = plugin;
    }
    
    public void addProtectedPlayer(UUID uuid) {
        protectedPlayers.put(uuid, true);
    }
    
    public void removeProtectedPlayer(UUID uuid) {
        protectedPlayers.remove(uuid);
    }
    
    public boolean isProtected(UUID uuid) {
        return protectedPlayers.containsKey(uuid) && protectedPlayers.get(uuid);
    }
    
    public boolean shouldBlockMovement() {
        return plugin.getConfigManager().isBlockMovement();
    }
    
    public boolean shouldBlockBlockInteraction() {
        return plugin.getConfigManager().isBlockBlockInteraction();
    }
    
    public boolean shouldBlockEntityInteraction() {
        return plugin.getConfigManager().isBlockEntityInteraction();
    }
    
    public boolean shouldBlockItemUse() {
        return plugin.getConfigManager().isBlockItemUse();
    }
    
    public boolean shouldBlockChat() {
        return plugin.getConfigManager().isBlockChat();
    }
    
    public boolean shouldBlockDamage() {
        return plugin.getConfigManager().isBlockDamage();
    }
    
    public boolean shouldBlockTeleport() {
        return plugin.getConfigManager().isBlockTeleport();
    }
    
    public boolean shouldBlockCommands() {
        return plugin.getConfigManager().isBlockCommands();
    }
    
    public boolean isCommandAllowed(String command) {
        if (!shouldBlockCommands()) {
            return true;
        }
        
        // Comandos permitidos mesmo sem login
        String[] allowedCommands = {
            "login", "register", "changepassword", "unregister"
        };
        
        String cmd = command.toLowerCase().replace("/", "");
        
        for (String allowed : allowedCommands) {
            if (cmd.equals(allowed) || cmd.startsWith(allowed + " ")) {
                return true;
            }
        }
        
        return false;
    }
    
    public void clearAllProtections() {
        protectedPlayers.clear();
    }
    
    public int getProtectedPlayerCount() {
        return protectedPlayers.size();
    }
    
    public Map<UUID, Boolean> getProtectedPlayers() {
        return new HashMap<>(protectedPlayers);
    }
    
    public boolean hasBypassPermission(UUID uuid) {
        return plugin.getServer().getPlayer(uuid) != null && 
               plugin.getServer().getPlayer(uuid).hasPermission("dlauthlogin.bypass");
    }
    
    public boolean isPlayerProtected(UUID uuid) {
        // Verificar se o jogador tem bypass
        if (hasBypassPermission(uuid)) {
            return false;
        }
        
        // Verificar se está logado
        if (plugin.getAuthManager().isLoggedIn(uuid)) {
            return false;
        }
        
        // Verificar se tem sessão válida
        if (plugin.getSessionManager().hasValidSession(uuid)) {
            return false;
        }
        
        return true;
    }
    
    public boolean needsRegistration(UUID uuid) {
        // Verificar se o jogador tem bypass
        if (hasBypassPermission(uuid)) {
            return false;
        }
        
        // Verificar se está registrado
        return !plugin.getAuthManager().isRegistered(uuid);
    }
    
    public boolean needsLogin(UUID uuid) {
        // Verificar se o jogador tem bypass
        if (hasBypassPermission(uuid)) {
            return false;
        }
        
        // Verificar se está registrado mas não logado
        return plugin.getAuthManager().isRegistered(uuid) && !plugin.getAuthManager().isLoggedIn(uuid);
    }
    
    public void updatePlayerProtection(UUID uuid) {
        if (isPlayerProtected(uuid)) {
            addProtectedPlayer(uuid);
        } else {
            removeProtectedPlayer(uuid);
        }
    }
    
    public void updateAllPlayerProtections() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            updatePlayerProtection(player.getUniqueId());
        });
    }
    
    public void forceStateCheck(UUID uuid) {
        // Força uma verificação completa do estado do jogador
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            // Jogador não está online, remover de todas as listas
            removeProtectedPlayer(uuid);
            return;
        }
        
        // Verificar se está logado
        boolean isLoggedIn = plugin.getAuthManager().isLoggedIn(uuid);
        
        // Verificar se tem bypass
        boolean hasBypass = hasBypassPermission(uuid);
        
        // Verificar se tem sessão válida
        boolean hasValidSession = plugin.getSessionManager().hasValidSession(uuid);
        
        // Se está logado ou tem bypass ou tem sessão válida, não deve estar protegido
        if (isLoggedIn || hasBypass || hasValidSession) {
            removeProtectedPlayer(uuid);
        } else {
            // Se não está logado e não tem bypass nem sessão, deve estar protegido
            addProtectedPlayer(uuid);
        }
    }
}
