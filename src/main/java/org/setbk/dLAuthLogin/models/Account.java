package org.setbk.dLAuthLogin.models;

import java.sql.Timestamp;
import java.util.UUID;

public class Account {
    
    private UUID uuid;
    private String username;
    private String passwordHash;
    private Timestamp registrationDate;
    private Timestamp lastLogin;
    private int loginAttempts;
    private Timestamp lockedUntil;
    private String ipAddress;
    private String sessionToken;
    private Timestamp sessionExpires;
    
    public Account() {
        this.loginAttempts = 0;
    }
    
    public Account(UUID uuid, String username, String passwordHash) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.registrationDate = new Timestamp(System.currentTimeMillis());
        this.loginAttempts = 0;
    }
    
    // Getters e Setters
    public UUID getUuid() {
        return uuid;
    }
    
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public Timestamp getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public Timestamp getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public int getLoginAttempts() {
        return loginAttempts;
    }
    
    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }
    
    public Timestamp getLockedUntil() {
        return lockedUntil;
    }
    
    public void setLockedUntil(Timestamp lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getSessionToken() {
        return sessionToken;
    }
    
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    public Timestamp getSessionExpires() {
        return sessionExpires;
    }
    
    public void setSessionExpires(Timestamp sessionExpires) {
        this.sessionExpires = sessionExpires;
    }
    
    // Métodos utilitários
    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }
        return System.currentTimeMillis() < lockedUntil.getTime();
    }
    
    public boolean hasValidSession() {
        if (sessionToken == null || sessionExpires == null) {
            return false;
        }
        return System.currentTimeMillis() < sessionExpires.getTime();
    }
    
    public void incrementLoginAttempts() {
        this.loginAttempts++;
    }
    
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.lockedUntil = null;
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", registrationDate=" + registrationDate +
                ", lastLogin=" + lastLogin +
                ", loginAttempts=" + loginAttempts +
                ", locked=" + isLocked() +
                ", hasValidSession=" + hasValidSession() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Account account = (Account) o;
        return uuid.equals(account.uuid);
    }
    
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
