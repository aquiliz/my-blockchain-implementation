package com.aquiliz.blockchain;

import com.aquiliz.blockchain.model.Block;
import com.aquiliz.blockchain.model.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Runner {

    private static final int MINING_DIFFICULTY = 3;

    public static void main(String[] args) {
        Miner miner1 = new Miner(MINING_DIFFICULTY, "miner1");
        Block genesisBlock = miner1.generateBlock(createDummyTransactions(), null);
        System.out.println(genesisBlock);
    }

    private static List<Transaction> createDummyTransactions() {
        Transaction transaction1 = new Transaction("Alice", "Bob", BigDecimal.valueOf(5.1618));
        Transaction transaction2 = new Transaction("Ken", "Josh", BigDecimal.valueOf(100));
        Transaction transaction3 = new Transaction("Marie", "Hellen", BigDecimal.valueOf(200));
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        return transactions;
    }
}
