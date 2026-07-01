package com.example.Ai;

import jakarta.persistence.*;

@Entity
@Table(name = "\"Statements\"")
public class StatementData {

    @Id
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "statement_data", columnDefinition = "TEXT")
    private String statementData;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getStatementData() {
        return statementData;
    }
}