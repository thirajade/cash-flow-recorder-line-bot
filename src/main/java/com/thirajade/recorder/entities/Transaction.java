package com.thirajade.recorder.entities;

public class Transaction {
    private String[] parts;

    public Transaction(String transaction) {
        this.parts = transaction.split(" ");
    }

    public String getPart(Integer part) {
        if (part > parts.length) {
            return null;
        }

        return this.parts[part];
    }

}
