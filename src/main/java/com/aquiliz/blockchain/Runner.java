package com.aquiliz.blockchain;

import com.aquiliz.blockchain.model.Block;
import com.aquiliz.blockchain.model.Transaction;
import com.aquiliz.blockchain.model.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Runner {

    private static final int MINING_DIFFICULTY = 1;

    public static void main(String[] args) {
        BlockChain blockChain = BlockChain.getInstance();

        Miner miner1 = new Miner(MINING_DIFFICULTY, "miner1");
        Block genesisBlock = miner1.generateBlock(createDummyTransactions(), null);
        blockChain.addGenesisBlock(genesisBlock);

        Block block2 = miner1.generateBlock(createDummyTransactions2(), genesisBlock.getHash());
        blockChain.addBlock(block2);

        System.out.println(genesisBlock);
    }

    private static List<Transaction> createDummyTransactions() {
        Transaction transaction1 = new Transaction("xxx", "Alice", BigDecimal.valueOf(24), TransactionType.TOP_UP);
        Transaction transaction2 = new Transaction("xxx", "Ken", BigDecimal.valueOf(200), TransactionType.TOP_UP);
        Transaction transaction3 = new Transaction("xxx", "Marie", BigDecimal.valueOf(300), TransactionType.TOP_UP);
        Transaction transaction4 = new Transaction("Alice", "Bob", BigDecimal.valueOf(23), TransactionType.TRANSFER);
        Transaction transaction5 = new Transaction("Ken", "Josh", BigDecimal.valueOf(100), TransactionType.TRANSFER);
        Transaction transaction6 = new Transaction("Marie", "Hellen", BigDecimal.valueOf(200), TransactionType.TRANSFER);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        transactions.add(transaction4);
        transactions.add(transaction5);
        transactions.add(transaction6);
        return transactions;
    }

    private static List<Transaction> createDummyTransactions2() {
        Transaction transaction1 = new Transaction("Bob", "Toby", BigDecimal.valueOf(25), TransactionType.TRANSFER);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        return transactions;
    }
}
