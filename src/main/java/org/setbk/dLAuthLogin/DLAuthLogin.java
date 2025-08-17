package org.setbk.dLAuthLogin;

import org.bukkit.plugin.java.JavaPlugin;
import org.setbk.dLAuthLogin.commands.*;
import org.setbk.dLAuthLogin.listeners.*;
import org.setbk.dLAuthLogin.managers.*;

public final class DLAuthLogin extends JavaPlugin {
    
    private static DLAuthLogin instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private AuthManager authManager;
    private SessionManager sessionManager;
    private ProtectionManager protectionManager;
    private MessageManager messageManager;
    private LogManager logManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Inicializar managers
        initializeManagers();
        
        // Registrar comandos
        registerCommands();
        
        // Registrar listeners
        registerListeners();
        
        // Carregar dados
        loadData();
        
        getLogger().info("DLAuthLogin habilitado com sucesso!");
    }
    
    @Override
    public void onDisable() {
        // Salvar dados
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        // Limpar sessões
        if (sessionManager != null) {
            sessionManager.clearAllSessions();
        }
        
        getLogger().info("DLAuthLogin desabilitado!");
    }
    
    private void initializeManagers() {
        try {
            // Config Manager
            configManager = new ConfigManager(this);
            configManager.loadConfig();
            getLogger().info("ConfigManager inicializado com sucesso!");
            
            // Message Manager
            messageManager = new MessageManager(this);
            messageManager.loadMessages();
            getLogger().info("MessageManager inicializado com sucesso!");
            
            // Database Manager
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            getLogger().info("DatabaseManager inicializado com sucesso!");
            
            // Auth Manager
            authManager = new AuthManager(this);
            getLogger().info("AuthManager inicializado com sucesso!");
            
            // Session Manager
            sessionManager = new SessionManager(this);
            getLogger().info("SessionManager inicializado com sucesso!");
            
            // Protection Manager
            protectionManager = new ProtectionManager(this);
            getLogger().info("ProtectionManager inicializado com sucesso!");
            
            // Log Manager
            logManager = new LogManager(this);
            getLogger().info("LogManager inicializado com sucesso!");
            
        } catch (Exception e) {
            getLogger().severe("Erro crítico ao inicializar managers: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha na inicialização dos managers", e);
        }
    }
    
    private void registerCommands() {
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("changepassword").setExecutor(new ChangePasswordCommand(this));
        getCommand("unregister").setExecutor(new UnregisterCommand(this));
        getCommand("reload").setExecutor(new ReloadCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        
        // Iniciar verificação periódica de timeout
        startTimeoutChecker();
    }
    
    private void loadData() {
        try {
            // Carregar contas do banco de dados
            if (databaseManager != null) {
                databaseManager.loadAccounts();
            } else {
                getLogger().severe("DatabaseManager é nulo! Não foi possível carregar contas.");
            }
            
            // Carregar sessões ativas
            if (sessionManager != null) {
                sessionManager.loadSessions();
            } else {
                getLogger().severe("SessionManager é nulo! Não foi possível carregar sessões.");
            }
            
        } catch (Exception e) {
            getLogger().severe("Erro ao carregar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters para os managers
    public static DLAuthLogin getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public AuthManager getAuthManager() {
        return authManager;
    }
    
    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public LogManager getLogManager() {
        return logManager;
    }
    
    private void startTimeoutChecker() {
        // Verificar timeouts a cada 5 segundos
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (authManager != null) {
                authManager.checkAndKickTimeoutPlayers();
            }
        }, 20L, 100L); // 20 ticks = 1 segundo, 100 ticks = 5 segundos
        
        getLogger().info("Sistema de verificação de timeout iniciado!");
    }
}
