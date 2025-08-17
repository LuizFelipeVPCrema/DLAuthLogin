package org.setbk.dLAuthLogin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.setbk.dLAuthLogin.DLAuthLogin;

import java.util.UUID;

public class ProtectionListener implements Listener {
    
    private final DLAuthLogin plugin;
    
    public ProtectionListener(DLAuthLogin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getProtectionManager().shouldBlockMovement()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (plugin.getProtectionManager().isProtected(uuid)) {
            // Verificar se o jogador realmente se moveu
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                
                event.setCancelled(true);
                sendProtectionMessage(player, uuid);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getProtectionManager().shouldBlockBlockInteraction()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (plugin.getProtectionManager().isProtected(uuid)) {
            event.setCancelled(true);
            sendProtectionMessage(player, uuid);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getProtectionManager().shouldBlockBlockInteraction()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (plugin.getProtectionManager().isProtected(uuid)) {
            event.setCancelled(true);
            sendProtectionMessage(player, uuid);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!plugin.getProtectionManager().shouldBlockEntityInteraction()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (plugin.getProtectionManager().isProtected(uuid)) {
            event.setCancelled(true);
            sendProtectionMessage(player, uuid);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getProtectionManager().shouldBlockItemUse()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (plugin.getProtectionManager().isProtected(uuid)) {
            event.setCancelled(true);
            sendProtectionMessage(player, uuid);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getProtectionManager().shouldBlockChat()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (plugin.getProtectionManager().isProtected(uuid)) {
            event.setCancelled(true);
            sendProtectionMessage(player, uuid);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getProtectionManager().shouldBlockCommands()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (plugin.getProtectionManager().isProtected(uuid)) {
            String command = event.getMessage();
            
            if (!plugin.getProtectionManager().isCommandAllowed(command)) {
                event.setCancelled(true);
                sendProtectionMessage(player, uuid);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!plugin.getProtectionManager().shouldBlockDamage()) {
            return;
        }
        
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID uuid = player.getUniqueId();
            
            if (plugin.getProtectionManager().isProtected(uuid)) {
                event.setCancelled(true);
                sendProtectionMessage(player, uuid);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!plugin.getProtectionManager().shouldBlockDamage()) {
            return;
        }
        
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            UUID uuid = player.getUniqueId();
            
            if (plugin.getProtectionManager().isProtected(uuid)) {
                event.setCancelled(true);
                sendProtectionMessage(player, uuid);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!plugin.getProtectionManager().shouldBlockTeleport()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (plugin.getProtectionManager().isProtected(uuid)) {
            event.setCancelled(true);
            sendProtectionMessage(player, uuid);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (plugin.getProtectionManager().isProtected(uuid)) {
            event.setCancelled(true);
            sendProtectionMessage(player, uuid);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID uuid = player.getUniqueId();
            
            if (plugin.getProtectionManager().isProtected(uuid)) {
                event.setCancelled(true);
                sendProtectionMessage(player, uuid);
            }
        }
    }
    
    private void sendProtectionMessage(Player player, UUID uuid) {
        // Verificar se o jogador tem bypass
        if (plugin.getProtectionManager().hasBypassPermission(uuid)) {
            return; // Não enviar mensagem para jogadores com bypass
        }
        
        // Verificar se está logado (verificação dupla)
        if (plugin.getAuthManager().isLoggedIn(uuid)) {
            // Se está logado mas ainda está protegido, remover proteção
            plugin.getProtectionManager().removeProtectedPlayer(uuid);
            return; // Não enviar mensagem para jogadores logados
        }
        
        // Verificar se precisa se registrar
        if (plugin.getProtectionManager().needsRegistration(uuid)) {
            plugin.getMessageManager().sendBoxMessage(player, "info.register_required");
            plugin.getMessageManager().sendBoxMessage(player, "register.usage");
        } 
        // Verificar se precisa fazer login
        else if (plugin.getProtectionManager().needsLogin(uuid)) {
            plugin.getMessageManager().sendBoxMessage(player, "info.login_required");
            plugin.getMessageManager().sendBoxMessage(player, "login.usage");
        } 
        // Caso genérico
        else {
            plugin.getMessageManager().sendBoxMessage(player, "protection.interaction_blocked");
        }
    }
}
