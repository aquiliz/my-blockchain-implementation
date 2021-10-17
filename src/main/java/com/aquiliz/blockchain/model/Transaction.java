package com.aquiliz.blockchain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Transaction {
    private String fromAddress;
    private String toAddress;
    private BigDecimal amount;
}
