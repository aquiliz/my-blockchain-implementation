package com.aquiliz.blockchain.model;

import lombok.Getter;

@Getter
public enum TransactionType {
    TOP_UP("Top up wallet"), TRANSFER("Transfer"), REWARD("Reward"), FEE("Fee");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }
}
