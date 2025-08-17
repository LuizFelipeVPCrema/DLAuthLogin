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
                plugin.getMessageManager().sendBoxMessage(sender, "admin.reload_success");
                
            } catch (Exception e) {
                sender.sendMessage("§cErro ao recarregar configuração: " + e.getMessage());
                plugin.getLogger().severe("Erro ao recarregar configuração: " + e.getMessage());
            }
        } else if (args[0].equalsIgnoreCase("status")) {
            // Mostrar status do sistema
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage("§6|  Status do DLAuthLogin");
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage("§e|  Jogadores logados: §a" + plugin.getAuthManager().getLoggedInPlayers().size());
            sender.sendMessage("§e|  Jogadores protegidos: §c" + plugin.getProtectionManager().getProtectedPlayerCount());
            sender.sendMessage("§e|  Sessões ativas: §a" + plugin.getSessionManager().getActiveSessionCount());
            sender.sendMessage("§6+----------------------------------------+");
            
            // Listar jogadores online e seus status
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage("§6|  Jogadores Online");
            sender.sendMessage("§6+----------------------------------------+");
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                java.util.UUID uuid = player.getUniqueId();
                String status = "§cNao autenticado";
                
                if (plugin.getProtectionManager().hasBypassPermission(uuid)) {
                    status = "§dBypass";
                } else if (plugin.getAuthManager().isLoggedIn(uuid)) {
                    status = "§aLogado";
                } else if (plugin.getAuthManager().isRegistered(uuid)) {
                    status = "§eRegistrado mas nao logado";
                } else {
                    status = "§cNao registrado";
                }
                
                sender.sendMessage("§7|  " + player.getName() + ": " + status);
            }
            sender.sendMessage("§6+----------------------------------------+");
            
        } else if (args[0].equalsIgnoreCase("clearsessions")) {
            // Limpar todas as sessões
            plugin.getAuthManager().clearAllSessions();
            plugin.getProtectionManager().clearAllProtections();
            plugin.getProtectionManager().updateAllPlayerProtections();
            
            sender.sendMessage("§a+----------------------------------------+");
            sender.sendMessage("§a|  Todas as sessoes foram limpas!");
            sender.sendMessage("§a+----------------------------------------+");
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
            
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage("§6|  Teste de Forca de Senha");
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage("§e|  Senha: §7" + testPassword.replaceAll(".", "*"));
            sender.sendMessage("§e|  Forca atual: §a" + strength + "/6");
            sender.sendMessage("§e|  Forca necessaria: §c" + requiredStrength + "/6");
            sender.sendMessage("§e|  Status: " + (isStrong ? "§aAceita" : "§cRejeitada"));
            sender.sendMessage("§6+----------------------------------------+");
            
            // Detalhes da força
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage("§6|  Detalhes");
            sender.sendMessage("§6+----------------------------------------+");
            boolean hasLetter = testPassword.matches(".*[a-zA-Z].*");
            boolean hasDigit = testPassword.matches(".*\\d.*");
            boolean hasUpper = testPassword.matches(".*[A-Z].*");
            boolean hasLower = testPassword.matches(".*[a-z].*");
            boolean hasSpecial = testPassword.matches(".*[^a-zA-Z0-9].*");
            
            sender.sendMessage("§7|  Letras: " + (hasLetter ? "§aOK" : "§cFALHA"));
            sender.sendMessage("§7|  Numeros: " + (hasDigit ? "§aOK" : "§cFALHA"));
            sender.sendMessage("§7|  8+ caracteres: " + (testPassword.length() >= 8 ? "§aOK" : "§cFALHA"));
            sender.sendMessage("§7|  Maiusculas + Minusculas: " + (hasUpper && hasLower ? "§aOK" : "§cFALHA"));
            sender.sendMessage("§7|  Caracteres especiais: " + (hasSpecial ? "§aOK" : "§cFALHA"));
            sender.sendMessage("§7|  12+ caracteres: " + (testPassword.length() >= 12 ? "§aOK" : "§cFALHA"));
            sender.sendMessage("§6+----------------------------------------+");
        } else if (args[0].equalsIgnoreCase("debug")) {
            if (args.length < 2) {
                sender.sendMessage("§cUso: /dlauthlogin debug <jogador>");
                return true;
            }
            
            String playerName = args[1];
            org.bukkit.entity.Player targetPlayer = plugin.getServer().getPlayer(playerName);
            
            if (targetPlayer == null) {
                plugin.getMessageManager().sendBoxMessage(sender, "admin.player_not_found");
                return true;
            }
            
            java.util.UUID uuid = targetPlayer.getUniqueId();
            
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage("§6|  Debug do Jogador: " + targetPlayer.getName());
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage("§e|  UUID: §7" + uuid);
            sender.sendMessage("§e|  Online: §a" + targetPlayer.isOnline());
            sender.sendMessage("§e|  Bypass: §d" + plugin.getProtectionManager().hasBypassPermission(uuid));
            sender.sendMessage("§e|  Registrado: §a" + plugin.getAuthManager().isRegistered(uuid));
            sender.sendMessage("§e|  Logado: §a" + plugin.getAuthManager().isLoggedIn(uuid));
            sender.sendMessage("§e|  Sessao valida: §a" + plugin.getSessionManager().hasValidSession(uuid));
            sender.sendMessage("§e|  Protegido: §c" + plugin.getProtectionManager().isProtected(uuid));
            sender.sendMessage("§e|  Precisa registro: §e" + plugin.getProtectionManager().needsRegistration(uuid));
            sender.sendMessage("§e|  Precisa login: §e" + plugin.getProtectionManager().needsLogin(uuid));
            sender.sendMessage("§6+----------------------------------------+");
            
            // Forçar atualização
            plugin.getProtectionManager().updatePlayerProtection(uuid);
            sender.sendMessage("§aStatus de protecao atualizado!");
        } else if (args[0].equalsIgnoreCase("testcolors")) {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("§cEste comando so pode ser usado por jogadores!");
                return true;
            }
            
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage("§6|  Teste de Cores");
            sender.sendMessage("§6+----------------------------------------+");
            
            // Testar cores diretas
            sender.sendMessage("§a|  Teste verde direto");
            sender.sendMessage("§c|  Teste vermelho direto");
            sender.sendMessage("§e|  Teste amarelo direto");
            sender.sendMessage("§6+----------------------------------------+");
            
            // Testar cores do MessageManager
            sender.sendMessage("§6|  Mensagens do MessageManager");
            sender.sendMessage("§6+----------------------------------------+");
            plugin.getMessageManager().sendBoxMessage((org.bukkit.entity.Player) sender, "login.success");
            plugin.getMessageManager().sendBoxMessage((org.bukkit.entity.Player) sender, "login.failed");
            plugin.getMessageManager().sendBoxMessage((org.bukkit.entity.Player) sender, "register.success");
            
            // Testar cores com placeholders
            java.util.Map<String, String> placeholders = new java.util.HashMap<>();
            placeholders.put("attempt", "1");
            placeholders.put("max_attempts", "3");
            plugin.getMessageManager().sendBoxMessage((org.bukkit.entity.Player) sender, "login.failed", placeholders);
            
            // Testar método alternativo
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage("§6|  Teste Metodo Alternativo");
            sender.sendMessage("§6+----------------------------------------+");
            sender.sendMessage(plugin.getMessageManager().getColoredMessage("login.success"));
            sender.sendMessage(plugin.getMessageManager().getColoredMessage("login.failed"));
            sender.sendMessage(plugin.getMessageManager().getColoredMessage("register.success"));
            
            sender.sendMessage("§a+----------------------------------------+");
            sender.sendMessage("§a|  Teste de cores concluido!");
            sender.sendMessage("§a+----------------------------------------+");
        }
        
        return true;
    }
}
