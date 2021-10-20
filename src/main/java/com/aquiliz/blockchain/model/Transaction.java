package com.aquiliz.blockchain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
public class Transaction {
    private final String id = UUID.randomUUID().toString();
    private final String fromAddress;
    private final String toAddress;
    private final BigDecimal amount;
    private final TransactionType type;

    public Transaction(String fromAddress, String toAddress, BigDecimal amount, TransactionType type) {
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.type = type;
    }
}
