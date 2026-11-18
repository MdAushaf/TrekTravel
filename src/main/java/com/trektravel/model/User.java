package com.trektravel.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model representing a user in the system (Trekker, Trek Organizer, or Admin).
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String salt;
    private String phone;
    private String role;   // 'TREKKER', 'ORGANIZER', 'ADMIN'
    private String status; // 'ACTIVE', 'BLOCKED'
    private Timestamp createdAt;

    public User() {}

    public User(int id, String name, String email, String passwordHash, String salt, String phone, String role, String status, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Role and status convenience helpers
    public boolean isTrekker() { return "TREKKER".equalsIgnoreCase(role); }
    public boolean isOrganizer() { return "ORGANIZER".equalsIgnoreCase(role); }
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(status); }
    public boolean isBlocked() { return "BLOCKED".equalsIgnoreCase(status); }
}
