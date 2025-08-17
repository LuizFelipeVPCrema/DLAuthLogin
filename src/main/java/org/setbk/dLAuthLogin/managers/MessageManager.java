package org.setbk.dLAuthLogin.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.setbk.dLAuthLogin.DLAuthLogin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    
    private final DLAuthLogin plugin;
    private final Map<String, FileConfiguration> messages = new HashMap<>();
    private String defaultLanguage;
    
    public MessageManager(DLAuthLogin plugin) {
        this.plugin = plugin;
    }
    
    public void loadMessages() {
        defaultLanguage = plugin.getConfigManager().getDefaultLanguage();
        
        // Carregar idioma padrão
        loadLanguage(defaultLanguage);
        
        // Carregar outros idiomas disponíveis
        loadLanguage("en_US");
        loadLanguage("pt_BR");
    }
    
    private void loadLanguage(String language) {
        try {
            // Primeiro, tentar carregar do arquivo do plugin
            File messageFile = new File(plugin.getDataFolder(), "messages_" + language + ".yml");
            
            if (!messageFile.exists()) {
                // Se não existir, copiar do recurso
                plugin.saveResource("messages_" + language + ".yml", false);
            }
            
            FileConfiguration config = YamlConfiguration.loadConfiguration(messageFile);
            
            // Carregar recursos padrão como fallback
            InputStream resourceStream = plugin.getResource("messages_" + language + ".yml");
            if (resourceStream != null) {
                FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(resourceStream, StandardCharsets.UTF_8)
                );
                config.setDefaults(defaultConfig);
            }
            
            messages.put(language, config);
            
            plugin.getLogger().info("Mensagens carregadas para " + language + " com sucesso!");
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao carregar mensagens para " + language + ": " + e.getMessage());
        }
    }
    
    public String getMessage(String path) {
        return getMessage(path, defaultLanguage);
    }
    
    public String getMessage(String path, String language) {
        FileConfiguration config = messages.get(language);
        if (config == null) {
            config = messages.get(defaultLanguage);
        }
        
        if (config == null) {
            return ChatColor.translateAlternateColorCodes('&', "§cMensagem não encontrada: " + path);
        }
        
        String message = config.getString(path);
        if (message == null) {
            // Tentar com idioma padrão
            FileConfiguration defaultConfig = messages.get(defaultLanguage);
            if (defaultConfig != null) {
                message = defaultConfig.getString(path);
            }
        }
        
        if (message == null) {
            return ChatColor.translateAlternateColorCodes('&', "§cMensagem não encontrada: " + path);
        }
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String getMessage(String path, String language, Map<String, String> placeholders) {
        String message = getMessage(path, language);
        
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message; // As cores já foram processadas no getMessage(path, language)
    }
    
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path, defaultLanguage);
        
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message; // As cores já foram processadas no getMessage(path, defaultLanguage)
    }
    
    public String getPrefix() {
        return getMessage("general.prefix");
    }
    
    public String getPrefix(String language) {
        return getMessage("general.prefix", language);
    }
    
    public void reloadMessages() {
        messages.clear();
        loadMessages();
    }
    
    public String getDefaultLanguage() {
        return defaultLanguage;
    }
    
    public void setDefaultLanguage(String language) {
        this.defaultLanguage = language;
    }
    
    public boolean isLanguageSupported(String language) {
        return messages.containsKey(language);
    }
    
    public Map<String, FileConfiguration> getMessages() {
        return messages;
    }
    
    /**
     * Envia uma mensagem colorida para um jogador
     * @param player O jogador que receberá a mensagem
     * @param path O caminho da mensagem no arquivo de configuração
     */
    public void sendMessage(org.bukkit.entity.Player player, String path) {
        String message = getMessage(path);
        player.sendMessage(message);
    }
    
    /**
     * Envia uma mensagem colorida para um jogador com placeholders
     * @param player O jogador que receberá a mensagem
     * @param path O caminho da mensagem no arquivo de configuração
     * @param placeholders Os placeholders para substituir na mensagem
     */
    public void sendMessage(org.bukkit.entity.Player player, String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        player.sendMessage(message);
    }
    
    /**
     * Envia uma mensagem colorida para um jogador em um idioma específico
     * @param player O jogador que receberá a mensagem
     * @param path O caminho da mensagem no arquivo de configuração
     * @param language O idioma da mensagem
     */
    public void sendMessage(org.bukkit.entity.Player player, String path, String language) {
        String message = getMessage(path, language);
        player.sendMessage(message);
    }
    
    /**
     * Envia uma mensagem colorida para um jogador em um idioma específico com placeholders
     * @param player O jogador que receberá a mensagem
     * @param path O caminho da mensagem no arquivo de configuração
     * @param language O idioma da mensagem
     * @param placeholders Os placeholders para substituir na mensagem
     */
    public void sendMessage(org.bukkit.entity.Player player, String path, String language, Map<String, String> placeholders) {
        String message = getMessage(path, language, placeholders);
        player.sendMessage(message);
    }
    
    /**
     * Testa se as cores estão sendo processadas corretamente
     * @param player O jogador para testar
     */
    public void testColors(org.bukkit.entity.Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTeste verde"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTeste vermelho"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eTeste amarelo"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bTeste azul claro"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dTeste rosa"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&fTeste branco"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&0Teste preto"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&lTeste negrito"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&nTeste sublinhado"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&oTeste itálico"));
    }
    
    /**
     * Processa cores de forma mais robusta
     * @param message A mensagem a ser processada
     * @return A mensagem com cores processadas
     */
    public String processColors(String message) {
        if (message == null) {
            return "";
        }
        
        // Primeiro, converter & para §
        message = message.replace("&", "§");
        
        // Depois, usar ChatColor para processar
        return ChatColor.translateAlternateColorCodes('§', message);
    }
    
    /**
     * Obtém uma mensagem com cores processadas de forma robusta
     * @param path O caminho da mensagem
     * @return A mensagem com cores processadas
     */
    public String getColoredMessage(String path) {
        String message = getMessage(path);
        return processColors(message);
    }
    
    /**
     * Envia uma mensagem em caixa ASCII para um jogador
     * @param player O jogador que receberá a mensagem
     * @param basePath O caminho base da mensagem (ex: "login.success")
     */
    public void sendBoxMessage(org.bukkit.entity.Player player, String basePath) {
        sendBoxMessage(player, basePath, null);
    }
    
    /**
     * Envia uma mensagem em caixa ASCII para um jogador com placeholders
     * @param player O jogador que receberá a mensagem
     * @param basePath O caminho base da mensagem (ex: "login.success")
     * @param placeholders Os placeholders para substituir na mensagem
     */
    public void sendBoxMessage(org.bukkit.entity.Player player, String basePath, Map<String, String> placeholders) {
        // Enviar a linha superior da caixa
        String topLine = getMessage(basePath);
        if (topLine != null && !topLine.contains("Mensagem não encontrada")) {
            player.sendMessage(topLine);
        }
        
        // Enviar o conteúdo da caixa
        String content = getMessage(basePath + "_content");
        if (content != null && !content.contains("Mensagem não encontrada")) {
            if (placeholders != null) {
                content = replacePlaceholders(content, placeholders);
            }
            player.sendMessage(content);
        }
        
        // Enviar linhas adicionais do conteúdo
        String[] additionalLines = {
            "_welcome", "_sub", "_next", "_usage", "_attempt", "_time", "_reason",
            "_action", "_strength", "_required", "_min", "_max", "_chars"
        };
        
        for (String line : additionalLines) {
            String additionalContent = getMessage(basePath + line);
            if (additionalContent != null && !additionalContent.contains("Mensagem não encontrada")) {
                if (placeholders != null) {
                    additionalContent = replacePlaceholders(additionalContent, placeholders);
                }
                player.sendMessage(additionalContent);
            }
        }
        
        // Enviar a linha inferior da caixa
        String bottomLine = getMessage(basePath + "_end");
        if (bottomLine != null && !bottomLine.contains("Mensagem não encontrada")) {
            player.sendMessage(bottomLine);
        }
    }
    
    /**
     * Envia uma mensagem em caixa ASCII para um CommandSender
     * @param sender O sender que receberá a mensagem
     * @param basePath O caminho base da mensagem (ex: "login.success")
     */
    public void sendBoxMessage(org.bukkit.command.CommandSender sender, String basePath) {
        sendBoxMessage(sender, basePath, null);
    }
    
    /**
     * Envia uma mensagem em caixa ASCII para um CommandSender com placeholders
     * @param sender O sender que receberá a mensagem
     * @param basePath O caminho base da mensagem (ex: "login.success")
     * @param placeholders Os placeholders para substituir na mensagem
     */
    public void sendBoxMessage(org.bukkit.command.CommandSender sender, String basePath, Map<String, String> placeholders) {
        // Enviar a linha superior da caixa
        String topLine = getMessage(basePath);
        if (topLine != null && !topLine.contains("Mensagem não encontrada")) {
            sender.sendMessage(topLine);
        }
        
        // Enviar o conteúdo da caixa
        String content = getMessage(basePath + "_content");
        if (content != null && !content.contains("Mensagem não encontrada")) {
            if (placeholders != null) {
                content = replacePlaceholders(content, placeholders);
            }
            sender.sendMessage(content);
        }
        
        // Enviar linhas adicionais do conteúdo
        String[] additionalLines = {
            "_welcome", "_sub", "_next", "_usage", "_attempt", "_time", "_reason",
            "_action", "_strength", "_required", "_min", "_max", "_chars"
        };
        
        for (String line : additionalLines) {
            String additionalContent = getMessage(basePath + line);
            if (additionalContent != null && !additionalContent.contains("Mensagem não encontrada")) {
                if (placeholders != null) {
                    additionalContent = replacePlaceholders(additionalContent, placeholders);
                }
                sender.sendMessage(additionalContent);
            }
        }
        
        // Enviar a linha inferior da caixa
        String bottomLine = getMessage(basePath + "_end");
        if (bottomLine != null && !bottomLine.contains("Mensagem não encontrada")) {
            sender.sendMessage(bottomLine);
        }
    }
    
    /**
     * Substitui placeholders em uma mensagem
     * @param message A mensagem original
     * @param placeholders Os placeholders para substituir
     * @return A mensagem com placeholders substituídos
     */
    private String replacePlaceholders(String message, Map<String, String> placeholders) {
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return message;
    }
    
    /**
     * Envia uma mensagem simples (sem caixa) para um jogador
     * @param player O jogador que receberá a mensagem
     * @param path O caminho da mensagem
     */
    public void sendSimpleMessage(org.bukkit.entity.Player player, String path) {
        String message = getMessage(path);
        if (message != null && !message.contains("Mensagem não encontrada")) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Envia uma mensagem simples (sem caixa) para um CommandSender
     * @param sender O sender que receberá a mensagem
     * @param path O caminho da mensagem
     */
    public void sendSimpleMessage(org.bukkit.command.CommandSender sender, String path) {
        String message = getMessage(path);
        if (message != null && !message.contains("Mensagem não encontrada")) {
            sender.sendMessage(message);
        }
    }
}
