package org.setbk.dLAuthLogin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.setbk.dLAuthLogin.DLAuthLogin;

public class ReloadCommand implements CommandExecutor {
    
    private final DLAuthLogin plugin;
    
    public ReloadCommand(DLAuthLogin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar permissão
        if (!sender.hasPermission("dlauthlogin.admin")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("general.no_permission"));
            return true;
        }
        
        // Verificar argumentos
        if (args.length == 0) {
            sender.sendMessage("§eUso: /dlauthlogin reload|status|clearsessions|testpassword <senha>|debug <jogador>|testcolors");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            try {
                // Recarregar configuração
                plugin.getConfigManager().reloadConfig();
                
                // Recarregar mensagens
                plugin.getMessageManager().reloadMessages();
                
                // Log da ação
                plugin.getLogManager().logConfigReload(sender.getName());
                
                // Mensagem de sucesso
                sender.sendMessage(plugin.getMessageManager().getMessage("admin.reload_success"));
                
            } catch (Exception e) {
                sender.sendMessage("§cErro ao recarregar configuração: " + e.getMessage());
                plugin.getLogger().severe("Erro ao recarregar configuração: " + e.getMessage());
            }
        } else if (args[0].equalsIgnoreCase("status")) {
            // Mostrar status do sistema
            sender.sendMessage("§6=== Status do DLAuthLogin ===");
            sender.sendMessage("§eJogadores logados: §a" + plugin.getAuthManager().getLoggedInPlayers().size());
            sender.sendMessage("§eJogadores protegidos: §c" + plugin.getProtectionManager().getProtectedPlayerCount());
            sender.sendMessage("§eSessões ativas: §a" + plugin.getSessionManager().getActiveSessionCount());
            
            // Listar jogadores online e seus status
            sender.sendMessage("§6=== Jogadores Online ===");
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                java.util.UUID uuid = player.getUniqueId();
                String status = "§c❌ Não autenticado";
                
                if (plugin.getProtectionManager().hasBypassPermission(uuid)) {
                    status = "§d🔓 Bypass";
                } else if (plugin.getAuthManager().isLoggedIn(uuid)) {
                    status = "§a✅ Logado";
                } else if (plugin.getAuthManager().isRegistered(uuid)) {
                    status = "§e⚠️ Registrado mas não logado";
                } else {
                    status = "§c❌ Não registrado";
                }
                
                sender.sendMessage("§7- " + player.getName() + ": " + status);
            }
            
        } else if (args[0].equalsIgnoreCase("clearsessions")) {
            // Limpar todas as sessões
            plugin.getAuthManager().clearAllSessions();
            plugin.getProtectionManager().clearAllProtections();
            plugin.getProtectionManager().updateAllPlayerProtections();
            
            sender.sendMessage("§aTodas as sessões foram limpas!");
            plugin.getLogManager().logAdminAction("clear_sessions", sender.getName(), "Sessões limpas");
        } else if (args[0].equalsIgnoreCase("testpassword")) {
            if (args.length < 2) {
                sender.sendMessage("§cUso: /dlauthlogin testpassword <senha>");
                return true;
            }
            
            String testPassword = args[1];
            int strength = org.setbk.dLAuthLogin.utils.PasswordUtils.getPasswordStrength(testPassword);
            int requiredStrength = plugin.getConfigManager().getPasswordStrength();
            boolean isStrong = org.setbk.dLAuthLogin.utils.PasswordUtils.isPasswordStrong(testPassword, requiredStrength);
            
            sender.sendMessage("§6=== Teste de Força de Senha ===");
            sender.sendMessage("§eSenha: §7" + testPassword.replaceAll(".", "*"));
            sender.sendMessage("§eForça atual: §a" + strength + "/6");
            sender.sendMessage("§eForça necessária: §c" + requiredStrength + "/6");
            sender.sendMessage("§eStatus: " + (isStrong ? "§a✅ Aceita" : "§c❌ Rejeitada"));
            
            // Detalhes da força
            sender.sendMessage("§6=== Detalhes ===");
            boolean hasLetter = testPassword.matches(".*[a-zA-Z].*");
            boolean hasDigit = testPassword.matches(".*\\d.*");
            boolean hasUpper = testPassword.matches(".*[A-Z].*");
            boolean hasLower = testPassword.matches(".*[a-z].*");
            boolean hasSpecial = testPassword.matches(".*[^a-zA-Z0-9].*");
            
            sender.sendMessage("§7- Letras: " + (hasLetter ? "§a✅" : "§c❌"));
            sender.sendMessage("§7- Números: " + (hasDigit ? "§a✅" : "§c❌"));
            sender.sendMessage("§7- 8+ caracteres: " + (testPassword.length() >= 8 ? "§a✅" : "§c❌"));
            sender.sendMessage("§7- Maiúsculas + Minúsculas: " + (hasUpper && hasLower ? "§a✅" : "§c❌"));
            sender.sendMessage("§7- Caracteres especiais: " + (hasSpecial ? "§a✅" : "§c❌"));
            sender.sendMessage("§7- 12+ caracteres: " + (testPassword.length() >= 12 ? "§a✅" : "§c❌"));
        } else if (args[0].equalsIgnoreCase("debug")) {
            if (args.length < 2) {
                sender.sendMessage("§cUso: /dlauthlogin debug <jogador>");
                return true;
            }
            
            String playerName = args[1];
            org.bukkit.entity.Player targetPlayer = plugin.getServer().getPlayer(playerName);
            
            if (targetPlayer == null) {
                sender.sendMessage("§cJogador não encontrado!");
                return true;
            }
            
            java.util.UUID uuid = targetPlayer.getUniqueId();
            
            sender.sendMessage("§6=== Debug do Jogador: " + targetPlayer.getName() + " ===");
            sender.sendMessage("§eUUID: §7" + uuid);
            sender.sendMessage("§eOnline: §a" + targetPlayer.isOnline());
            sender.sendMessage("§eBypass: §d" + plugin.getProtectionManager().hasBypassPermission(uuid));
            sender.sendMessage("§eRegistrado: §a" + plugin.getAuthManager().isRegistered(uuid));
            sender.sendMessage("§eLogado: §a" + plugin.getAuthManager().isLoggedIn(uuid));
            sender.sendMessage("§eSessão válida: §a" + plugin.getSessionManager().hasValidSession(uuid));
            sender.sendMessage("§eProtegido: §c" + plugin.getProtectionManager().isProtected(uuid));
            sender.sendMessage("§ePrecisa registro: §e" + plugin.getProtectionManager().needsRegistration(uuid));
            sender.sendMessage("§ePrecisa login: §e" + plugin.getProtectionManager().needsLogin(uuid));
            
            // Forçar atualização
            plugin.getProtectionManager().updatePlayerProtection(uuid);
            sender.sendMessage("§aStatus de proteção atualizado!");
        } else if (args[0].equalsIgnoreCase("testcolors")) {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
                return true;
            }
            
            sender.sendMessage("§6=== Teste de Cores ===");
            
            // Testar cores diretas
            sender.sendMessage("§aTeste verde direto");
            sender.sendMessage("§cTeste vermelho direto");
            sender.sendMessage("§eTeste amarelo direto");
            
            // Testar cores do MessageManager
            sender.sendMessage(plugin.getMessageManager().getMessage("login.success"));
            sender.sendMessage(plugin.getMessageManager().getMessage("login.failed"));
            sender.sendMessage(plugin.getMessageManager().getMessage("register.success"));
            
            // Testar cores com placeholders
            java.util.Map<String, String> placeholders = new java.util.HashMap<>();
            placeholders.put("attempt", "1");
            placeholders.put("max_attempts", "3");
            sender.sendMessage(plugin.getMessageManager().getMessage("login.failed", placeholders));
            
            // Testar método alternativo
            sender.sendMessage("§6=== Teste Método Alternativo ===");
            sender.sendMessage(plugin.getMessageManager().getColoredMessage("login.success"));
            sender.sendMessage(plugin.getMessageManager().getColoredMessage("login.failed"));
            sender.sendMessage(plugin.getMessageManager().getColoredMessage("register.success"));
            
            sender.sendMessage("§aTeste de cores concluído!");
        }
        
        return true;
    }
}
