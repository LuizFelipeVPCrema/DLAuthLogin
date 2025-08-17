package org.setbk.dLAuthLogin.managers;

import org.bukkit.entity.Player;
import org.setbk.dLAuthLogin.DLAuthLogin;
import org.setbk.dLAuthLogin.models.Account;
import org.setbk.dLAuthLogin.utils.PasswordUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthManager {
    
    private final DLAuthLogin plugin;
    private final Map<UUID, Boolean> loggedInPlayers = new HashMap<>();
    private final Map<UUID, Long> loginTimeouts = new HashMap<>();
    
    public AuthManager(DLAuthLogin plugin) {
        this.plugin = plugin;
    }
    
    public boolean registerPlayer(Player player, String password, String confirmPassword) {
        UUID uuid = player.getUniqueId();
        String username = player.getName();
        String ipAddress = player.getAddress().getAddress().getHostAddress();
        
        // Verificar se já está registrado
        if (plugin.getDatabaseManager().accountExists(uuid)) {
            return false;
        }
        
        // Verificar se as senhas coincidem
        if (!password.equals(confirmPassword)) {
            return false;
        }
        
        // Verificar força da senha
        if (!PasswordUtils.isPasswordStrong(password, plugin.getConfigManager().getPasswordStrength())) {
            return false;
        }
        
        // Criptografar senha
        String passwordHash = PasswordUtils.hashPassword(password, plugin.getConfigManager().useBCrypt(), 
                                                       plugin.getConfigManager().getBCryptRounds());
        
        // Criar conta no banco de dados
        boolean success = plugin.getDatabaseManager().createAccount(uuid, username, passwordHash, ipAddress);
        
        if (success) {
            // Log do registro
            if (plugin.getConfigManager().isLogRegistrations()) {
                plugin.getLogManager().logRegistration(player);
            }
        }
        
        return success;
    }
    
    public boolean loginPlayer(Player player, String password) {
        UUID uuid = player.getUniqueId();
        String ipAddress = player.getAddress().getAddress().getHostAddress();
        
        // Verificar se já está logado
        if (isLoggedIn(uuid)) {
            // Se está logado mas ainda está protegido, remover proteção
            plugin.getProtectionManager().removeProtectedPlayer(uuid);
            return false;
        }
        
        // Buscar conta
        Account account = plugin.getDatabaseManager().getAccount(uuid);
        if (account == null) {
            return false;
        }
        
        // Verificar se a conta está bloqueada
        if (account.isLocked()) {
            return false;
        }
        
        // Verificar senha
        boolean passwordCorrect = PasswordUtils.verifyPassword(password, account.getPasswordHash(), 
                                                              plugin.getConfigManager().useBCrypt());
        
        if (passwordCorrect) {
            // Login bem-sucedido
            loggedInPlayers.put(uuid, true);
            loginTimeouts.remove(uuid);
            
            // Remover proteção
            plugin.getProtectionManager().removeProtectedPlayer(uuid);
            
            // Resetar tentativas de login
            account.resetLoginAttempts();
            plugin.getDatabaseManager().updateLoginAttempts(uuid, 0, null);
            
            // Atualizar último login
            plugin.getDatabaseManager().updateLastLogin(uuid, ipAddress);
            
            // Criar sessão se habilitado
            if (plugin.getConfigManager().isKeepSession()) {
                createSession(account, ipAddress);
            }
            
            // Log do login
            if (plugin.getConfigManager().isLogLoginAttempts()) {
                plugin.getLogManager().logLoginSuccess(player);
            }
            
            return true;
        } else {
            // Login falhou
            account.incrementLoginAttempts();
            
            // Verificar se deve bloquear a conta
            if (account.getLoginAttempts() >= plugin.getConfigManager().getMaxLoginAttempts()) {
                long lockoutTime = System.currentTimeMillis() + (plugin.getConfigManager().getLockoutDuration() * 1000L);
                account.setLockedUntil(new Timestamp(lockoutTime));
            }
            
            plugin.getDatabaseManager().updateLoginAttempts(uuid, account.getLoginAttempts(), account.getLockedUntil());
            
            // Log da tentativa falhada
            if (plugin.getConfigManager().isLogLoginAttempts()) {
                plugin.getLogManager().logLoginFailed(player);
            }
            
            return false;
        }
    }
    
    public boolean changePassword(Player player, String currentPassword, String newPassword, String confirmNewPassword) {
        UUID uuid = player.getUniqueId();
        
        // Verificar se está logado
        if (!isLoggedIn(uuid)) {
            return false;
        }
        
        // Buscar conta
        Account account = plugin.getDatabaseManager().getAccount(uuid);
        if (account == null) {
            return false;
        }
        
        // Verificar senha atual
        boolean currentPasswordCorrect = PasswordUtils.verifyPassword(currentPassword, account.getPasswordHash(), 
                                                                     plugin.getConfigManager().useBCrypt());
        if (!currentPasswordCorrect) {
            return false;
        }
        
        // Verificar se as novas senhas coincidem
        if (!newPassword.equals(confirmNewPassword)) {
            return false;
        }
        
        // Verificar se a nova senha é diferente da atual
        if (currentPassword.equals(newPassword)) {
            return false;
        }
        
        // Verificar força da nova senha
        if (!PasswordUtils.isPasswordStrong(newPassword, plugin.getConfigManager().getPasswordStrength())) {
            return false;
        }
        
        // Criptografar nova senha
        String newPasswordHash = PasswordUtils.hashPassword(newPassword, plugin.getConfigManager().useBCrypt(), 
                                                          plugin.getConfigManager().getBCryptRounds());
        
        // Atualizar senha no banco de dados
        boolean success = plugin.getDatabaseManager().updatePassword(uuid, newPasswordHash);
        
        if (success) {
            // Log da alteração de senha
            if (plugin.getConfigManager().isLogPasswordChanges()) {
                plugin.getLogManager().logPasswordChange(player);
            }
        }
        
        return success;
    }
    
    public boolean unregisterPlayer(Player player, String password) {
        UUID uuid = player.getUniqueId();
        
        // Buscar conta
        Account account = plugin.getDatabaseManager().getAccount(uuid);
        if (account == null) {
            return false;
        }
        
        // Verificar senha
        boolean passwordCorrect = PasswordUtils.verifyPassword(password, account.getPasswordHash(), 
                                                              plugin.getConfigManager().useBCrypt());
        if (!passwordCorrect) {
            return false;
        }
        
        // Deletar conta
        boolean success = plugin.getDatabaseManager().deleteAccount(uuid);
        
        if (success) {
            // Fazer logout
            logoutPlayer(uuid);
            
            // Log da remoção de conta
            if (plugin.getConfigManager().isLogUnregistrations()) {
                plugin.getLogManager().logUnregister(player);
            }
        }
        
        return success;
    }
    
    public void logoutPlayer(UUID uuid) {
        loggedInPlayers.remove(uuid);
        loginTimeouts.remove(uuid);
        
        // Limpar sessão (sempre, independente da configuração)
        // Isso garante que o estado seja completamente limpo
        plugin.getDatabaseManager().updateSession(uuid, null, null);
        
        // Remover da proteção também
        plugin.getProtectionManager().removeProtectedPlayer(uuid);
    }
    
    public boolean isLoggedIn(UUID uuid) {
        // Verificar se o jogador está na lista de logados
        boolean inLoggedInList = loggedInPlayers.containsKey(uuid) && loggedInPlayers.get(uuid);
        
        // Verificar se o jogador está online (dupla verificação)
        Player player = plugin.getServer().getPlayer(uuid);
        boolean isOnline = player != null && player.isOnline();
        
        // Se está na lista mas não está online, limpar o estado
        if (inLoggedInList && !isOnline) {
            logoutPlayer(uuid);
            return false;
        }
        
        return inLoggedInList && isOnline;
    }
    
    public boolean isRegistered(UUID uuid) {
        return plugin.getDatabaseManager().accountExists(uuid);
    }
    
    public void setLoginTimeout(UUID uuid) {
        long timeout = System.currentTimeMillis() + (plugin.getConfigManager().getLoginTimeout() * 1000L);
        loginTimeouts.put(uuid, timeout);
    }
    
    public boolean isLoginTimeout(UUID uuid) {
        Long timeout = loginTimeouts.get(uuid);
        if (timeout == null) {
            return false;
        }
        return System.currentTimeMillis() > timeout;
    }
    
    public void removeLoginTimeout(UUID uuid) {
        loginTimeouts.remove(uuid);
    }
    
    public boolean hasValidSession(UUID uuid) {
        Account account = plugin.getDatabaseManager().getAccount(uuid);
        if (account == null) {
            return false;
        }
        return account.hasValidSession();
    }
    
    private void createSession(Account account, String ipAddress) {
        String sessionToken = PasswordUtils.generateSessionToken();
        long sessionTimeout = System.currentTimeMillis() + (plugin.getConfigManager().getSessionTimeout() * 60 * 1000L);
        
        account.setSessionToken(sessionToken);
        account.setSessionExpires(new Timestamp(sessionTimeout));
        
        plugin.getDatabaseManager().updateSession(account.getUuid(), sessionToken, account.getSessionExpires());
    }
    
    public void clearAllSessions() {
        loggedInPlayers.clear();
        loginTimeouts.clear();
    }
    
    public void forceCleanupPlayerState(UUID uuid) {
        // Força a limpeza completa do estado do jogador
        loggedInPlayers.remove(uuid);
        loginTimeouts.remove(uuid);
        plugin.getProtectionManager().removeProtectedPlayer(uuid);
        plugin.getDatabaseManager().updateSession(uuid, null, null);
        
        // Log da limpeza forçada
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            plugin.getLogManager().logSecurityEvent("state_cleanup", player.getName());
        }
    }
    
    public Map<UUID, Boolean> getLoggedInPlayers() {
        return loggedInPlayers;
    }
    
    public void setLoggedIn(UUID uuid) {
        loggedInPlayers.put(uuid, true);
        loginTimeouts.remove(uuid);
        
        // Remover proteção explicitamente
        plugin.getProtectionManager().removeProtectedPlayer(uuid);
        
        // Log do login automático
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            plugin.getLogManager().logLoginSuccess(player);
        }
    }
    
    public void checkAndKickTimeoutPlayers() {
        for (Map.Entry<UUID, Long> entry : loginTimeouts.entrySet()) {
            UUID uuid = entry.getKey();
            Long timeout = entry.getValue();
            
            if (timeout != null && System.currentTimeMillis() > timeout) {
                // Jogador excedeu o timeout, expulsar
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    String kickMessage = plugin.getMessageManager().getMessage("error.login_timeout");
                    player.kickPlayer(kickMessage);
                    
                    // Log da expulsão por timeout
                    plugin.getLogManager().logSecurityEvent("timeout_kick", player.getName());
                }
                
                // Limpar dados do jogador
                loginTimeouts.remove(uuid);
                loggedInPlayers.remove(uuid);
                plugin.getProtectionManager().removeProtectedPlayer(uuid);
            }
        }
    }
}
