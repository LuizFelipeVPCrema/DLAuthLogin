package org.setbk.dLAuthLogin.managers;

import org.setbk.dLAuthLogin.DLAuthLogin;
import org.setbk.dLAuthLogin.models.Account;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    
    private final DLAuthLogin plugin;
    private Connection connection;
    private final Map<UUID, Account> accounts = new HashMap<>();
    
    public DatabaseManager(DLAuthLogin plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        try {
            String databaseType = plugin.getConfigManager().getDatabaseType();
            
            if ("H2".equalsIgnoreCase(databaseType)) {
                initializeH2();
            } else {
                plugin.getLogger().severe("Tipo de banco de dados não suportado: " + databaseType);
                throw new RuntimeException("Tipo de banco de dados não suportado: " + databaseType);
            }
            
            if (connection == null) {
                throw new RuntimeException("Falha ao estabelecer conexão com o banco de dados");
            }
            
            createTables();
            plugin.getLogger().info("Banco de dados inicializado com sucesso!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha crítica ao inicializar banco de dados", e);
        }
    }
    
    private void initializeH2() throws SQLException {
        try {
            // Carregar o driver H2 explicitamente
            Class.forName("org.h2.Driver");
            plugin.getLogger().info("Driver H2 carregado com sucesso!");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Driver H2 não encontrado! Verifique se as dependências estão corretas.");
            throw new SQLException("Driver H2 não disponível", e);
        }
        
        // Garantir que o diretório do plugin existe
        File pluginDataFolder = plugin.getDataFolder();
        if (!pluginDataFolder.exists()) {
            if (!pluginDataFolder.mkdirs()) {
                throw new SQLException("Não foi possível criar o diretório do plugin: " + pluginDataFolder.getAbsolutePath());
            }
        }
        
        // Usar caminho simples para o banco de dados
        String h2File = plugin.getConfigManager().getH2File();
        
        // URL mais simples possível do H2
        String url = "jdbc:h2:" + h2File;
        plugin.getLogger().info("Conectando ao banco H2: " + url);
        
        connection = DriverManager.getConnection(url, "sa", "");
        
        if (connection == null) {
            throw new SQLException("Falha ao estabelecer conexão com o banco H2");
        }
        
        plugin.getLogger().info("Conexão com H2 estabelecida com sucesso!");
    }
    
    private void createTables() throws SQLException {
        if (connection == null) {
            throw new SQLException("Conexão nula ao tentar criar tabelas");
        }
        
        // Usar sintaxe SQL padrão do H2
        String createAccountsTable = """
            CREATE TABLE IF NOT EXISTS accounts (
                uuid VARCHAR(36) PRIMARY KEY,
                username VARCHAR(16) NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_login TIMESTAMP,
                login_attempts INT DEFAULT 0,
                locked_until TIMESTAMP,
                ip_address VARCHAR(45),
                session_token VARCHAR(255),
                session_expires TIMESTAMP
            )
            """;
        
        String createSessionsTable = """
            CREATE TABLE IF NOT EXISTS sessions (
                uuid VARCHAR(36) PRIMARY KEY,
                session_token VARCHAR(255) NOT NULL,
                ip_address VARCHAR(45),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP NOT NULL
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            plugin.getLogger().info("Criando tabela 'accounts'...");
            stmt.execute(createAccountsTable);
            plugin.getLogger().info("Criando tabela 'sessions'...");
            stmt.execute(createSessionsTable);
            plugin.getLogger().info("Tabelas criadas com sucesso!");
        }
    }
    
    public void loadAccounts() {
        if (connection == null) {
            plugin.getLogger().severe("Tentativa de carregar contas com conexão nula!");
            return;
        }
        
        accounts.clear();
        
        String sql = "SELECT * FROM accounts";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Account account = new Account();
                account.setUuid(UUID.fromString(rs.getString("uuid")));
                account.setUsername(rs.getString("username"));
                account.setPasswordHash(rs.getString("password_hash"));
                account.setRegistrationDate(rs.getTimestamp("registration_date"));
                account.setLastLogin(rs.getTimestamp("last_login"));
                account.setLoginAttempts(rs.getInt("login_attempts"));
                account.setLockedUntil(rs.getTimestamp("locked_until"));
                account.setIpAddress(rs.getString("ip_address"));
                account.setSessionToken(rs.getString("session_token"));
                account.setSessionExpires(rs.getTimestamp("session_expires"));
                
                accounts.put(account.getUuid(), account);
            }
            
            plugin.getLogger().info("Carregadas " + accounts.size() + " contas do banco de dados.");
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao carregar contas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean createAccount(UUID uuid, String username, String passwordHash, String ipAddress) {
        String sql = """
            INSERT INTO accounts (uuid, username, password_hash, ip_address)
            VALUES (?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, passwordHash);
            stmt.setString(4, ipAddress);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                Account account = new Account();
                account.setUuid(uuid);
                account.setUsername(username);
                account.setPasswordHash(passwordHash);
                account.setIpAddress(ipAddress);
                account.setRegistrationDate(new Timestamp(System.currentTimeMillis()));
                
                accounts.put(uuid, account);
                return true;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao criar conta: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean updatePassword(UUID uuid, String newPasswordHash) {
        String sql = "UPDATE accounts SET password_hash = ? WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, uuid.toString());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                Account account = accounts.get(uuid);
                if (account != null) {
                    account.setPasswordHash(newPasswordHash);
                }
                return true;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao atualizar senha: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean deleteAccount(UUID uuid) {
        String sql = "DELETE FROM accounts WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                accounts.remove(uuid);
                return true;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao deletar conta: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean updateLoginAttempts(UUID uuid, int attempts, Timestamp lockedUntil) {
        String sql = "UPDATE accounts SET login_attempts = ?, locked_until = ? WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, attempts);
            stmt.setTimestamp(2, lockedUntil);
            stmt.setString(3, uuid.toString());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                Account account = accounts.get(uuid);
                if (account != null) {
                    account.setLoginAttempts(attempts);
                    account.setLockedUntil(lockedUntil);
                }
                return true;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao atualizar tentativas de login: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean updateLastLogin(UUID uuid, String ipAddress) {
        String sql = "UPDATE accounts SET last_login = CURRENT_TIMESTAMP, ip_address = ? WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ipAddress);
            stmt.setString(2, uuid.toString());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                Account account = accounts.get(uuid);
                if (account != null) {
                    account.setLastLogin(new Timestamp(System.currentTimeMillis()));
                    account.setIpAddress(ipAddress);
                }
                return true;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao atualizar último login: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean updateSession(UUID uuid, String sessionToken, Timestamp expires) {
        String sql = "UPDATE accounts SET session_token = ?, session_expires = ? WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sessionToken);
            stmt.setTimestamp(2, expires);
            stmt.setString(3, uuid.toString());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                Account account = accounts.get(uuid);
                if (account != null) {
                    account.setSessionToken(sessionToken);
                    account.setSessionExpires(expires);
                }
                return true;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao atualizar sessão: " + e.getMessage());
        }
        
        return false;
    }
    
    public Account getAccount(UUID uuid) {
        return accounts.get(uuid);
    }
    
    public Account getAccountByUsername(String username) {
        for (Account account : accounts.values()) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return account;
            }
        }
        return null;
    }
    
    public boolean accountExists(UUID uuid) {
        return accounts.containsKey(uuid);
    }
    
    public boolean accountExistsByUsername(String username) {
        return getAccountByUsername(username) != null;
    }
    
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().warning("Erro ao fechar conexão com banco de dados: " + e.getMessage());
            }
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public Map<UUID, Account> getAccounts() {
        return accounts;
    }
}
