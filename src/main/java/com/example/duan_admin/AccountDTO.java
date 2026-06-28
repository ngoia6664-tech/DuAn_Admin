package com.example.duan_admin;

import javafx.beans.property.*;

public class AccountDTO {
    private final LongProperty   id       = new SimpleLongProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty email    = new SimpleStringProperty();
    private final StringProperty role     = new SimpleStringProperty();
    private final DoubleProperty balance  = new SimpleDoubleProperty();

    public AccountDTO(Long id, String username, String email, String role, Double balance) {
        this.id.set(id);
        this.username.set(username);
        this.email.set(email);
        this.role.set(role);
        this.balance.set(balance);
    }

    // Getters (PropertyValueFactory yêu cầu đúng tên)
    public Long   getId()       { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getEmail()    { return email.get(); }
    public String getRole()     { return role.get(); }
    public Double getBalance()  { return balance.get(); }

    // Property accessors (optional, dùng khi bind)
    public LongProperty   idProperty()       { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty emailProperty()    { return email; }
    public StringProperty roleProperty()     { return role; }
    public DoubleProperty balanceProperty()  { return balance; }
}