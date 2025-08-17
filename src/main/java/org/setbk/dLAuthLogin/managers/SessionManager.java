package org.setbk.dLAuthLogin.managers;

import org.setbk.dLAuthLogin.DLAuthLogin;
import org.setbk.dLAuthLogin.models.Account;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    
    private final DLAuthLogin plugin;
    private final Map<UUID, Long> sessionExpirations = new HashMap<>();
    
    public SessionManager(DLAuthLogin plugin) {
        this.plugin = plugin;
    }
    
    public void loadSessions() {
        // Carregar sessões válidas do banco de dados
        for (Account account : plugin.getDatabaseManager().getAccounts().values()) {
            if (account.hasValidSession()) {
                long expirationTime = account.getSessionExpires().getTime();
                sessionExpirations.put(account.getUuid(), expirationTime);
            }
        }
        
        plugin.getLogger().info("Carregadas " + sessionExpirations.size() + " sessões ativas.");
    }
    
    public boolean hasValidSession(UUID uuid) {
        Long expirationTime = sessionExpirations.get(uuid);
        if (expirationTime == null) {
            return false;
        }
        
        boolean valid = System.currentTimeMillis() < expirationTime;
        
        if (!valid) {
            // Sessão expirou, remover
            sessionExpirations.remove(uuid);
            plugin.getDatabaseManager().updateSession(uuid, null, null);
        }
        
        return valid;
    }
    
    public void createSession(UUID uuid, String sessionToken, long durationMinutes) {
        long expirationTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L);
        sessionExpirations.put(uuid, expirationTime);
        
        Timestamp expires = new Timestamp(expirationTime);
        plugin.getDatabaseManager().updateSession(uuid, sessionToken, expires);
        
        plugin.getLogManager().logSessionEvent("created", 
                plugin.getDatabaseManager().getAccount(uuid).getUsername(), 
                "Expires: " + expires);
    }
    
    public void extendSession(UUID uuid, long additionalMinutes) {
        Long currentExpiration = sessionExpirations.get(uuid);
        if (currentExpiration != null) {
            long newExpiration = currentExpiration + (additionalMinutes * 60 * 1000L);
            sessionExpirations.put(uuid, newExpiration);
            
            Timestamp expires = new Timestamp(newExpiration);
            Account account = plugin.getDatabaseManager().getAccount(uuid);
            if (account != null) {
                plugin.getDatabaseManager().updateSession(uuid, account.getSessionToken(), expires);
            }
            
            plugin.getLogManager().logSessionEvent("extended", 
                    account != null ? account.getUsername() : uuid.toString(), 
                    "New expiration: " + expires);
        }
    }
    
    public void invalidateSession(UUID uuid) {
        sessionExpirations.remove(uuid);
        plugin.getDatabaseManager().updateSession(uuid, null, null);
        
        Account account = plugin.getDatabaseManager().getAccount(uuid);
        if (account != null) {
            plugin.getLogManager().logSessionEvent("invalidated", 
                    account.getUsername(), "Manual invalidation");
        }
    }
    
    public void clearExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        sessionExpirations.entrySet().removeIf(entry -> {
            if (entry.getValue() < currentTime) {
                // Sessão expirou
                plugin.getDatabaseManager().updateSession(entry.getKey(), null, null);
                
                Account account = plugin.getDatabaseManager().getAccount(entry.getKey());
                if (account != null) {
                    plugin.getLogManager().logSessionEvent("expired", 
                            account.getUsername(), "Automatic cleanup");
                }
                
                return true;
            }
            return false;
        });
    }
    
    public void clearAllSessions() {
        for (UUID uuid : sessionExpirations.keySet()) {
            plugin.getDatabaseManager().updateSession(uuid, null, null);
        }
        sessionExpirations.clear();
        
        plugin.getLogManager().logSessionEvent("cleared", "ALL", "All sessions cleared");
    }
    
    public long getSessionExpiration(UUID uuid) {
        return sessionExpirations.getOrDefault(uuid, 0L);
    }
    
    public long getRemainingSessionTime(UUID uuid) {
        Long expirationTime = sessionExpirations.get(uuid);
        if (expirationTime == null) {
            return 0;
        }
        
        long remaining = expirationTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    public int getActiveSessionCount() {
        clearExpiredSessions(); // Limpar sessões expiradas primeiro
        return sessionExpirations.size();
    }
    
    public Map<UUID, Long> getSessionExpirations() {
        return new HashMap<>(sessionExpirations);
    }
    
    public boolean isSessionEnabled() {
        return plugin.getConfigManager().isKeepSession();
    }
    
    public int getSessionTimeout() {
        return plugin.getConfigManager().getSessionTimeout();
    }
    
    public void startSessionCleanupTask() {
        // Tarefa para limpar sessões expiradas periodicamente
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            clearExpiredSessions();
        }, 20 * 60 * 5, 20 * 60 * 5); // A cada 5 minutos
    }
}
