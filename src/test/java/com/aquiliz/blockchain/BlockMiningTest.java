package com.aquiliz.blockchain;

import com.aquiliz.blockchain.model.Block;
import com.aquiliz.blockchain.model.Transaction;
import com.aquiliz.blockchain.model.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@Slf4j
public class BlockMiningTest {

    private Miner miner;
    private BlockChain blockChain;
    private static final int MINING_DIFFICULTY = 3;
    private static final int BLOCK_MINE_REWARD  = 20;

    @BeforeEach
    void beforeEach() {
        blockChain = Mockito.mock(BlockChain.class);
        miner = new Miner(blockChain, MINING_DIFFICULTY, "miner1");
    }

    @Test
    void should_Successfully_Mine_Block_With_Valid_Transactions() {
        List<Transaction> validTransactions = createValidTransactions();

        Block minedBlock = miner.generateBlock(validTransactions, "0001111111111");

        assertNotNull(minedBlock);
        assertNotNull(minedBlock.getHash());
        assertNotNull(minedBlock.getId());
        assertTrue(minedBlock.getHash().startsWith(generateZeroesString(MINING_DIFFICULTY)));
        assertEquals("0001111111111", minedBlock.getPreviousHash());

        //A reward transactions is expected
        Transaction rewardTransaction = new Transaction("blockchainSystemAddress", "miner1",
                BigDecimal.valueOf(BLOCK_MINE_REWARD), TransactionType.REWARD);
        validTransactions.add(rewardTransaction);
        assertEquals(validTransactions, minedBlock.getTransactions());
    }

    @Test
    void should_Skip_Transfer_Transactions_With_Insufficient_Funds() {
        withInitialBalance("Bob", 100);
        withInitialBalance("Alice", 150);
        withInitialBalance("Someone", 50);
        Transaction transaction1 = new Transaction("xxx", "Bob", BigDecimal.valueOf(20), TransactionType.REWARD);
        Transaction transaction2 = new Transaction("Alice", "Bob", BigDecimal.valueOf(100), TransactionType.TRANSFER);
        //Bob has 220 coins at this moment, but tries to transfer 250. This transaction is invalid and should be skipped
        Transaction invalidTransaction = new Transaction("Bob", "Someone", BigDecimal.valueOf(250), TransactionType.TRANSFER);
        //This one is ok and should be added to the block
        Transaction transaction3 = new Transaction("Bob", "Someone", BigDecimal.valueOf(210), TransactionType.TRANSFER);
        List<Transaction> transactions = toMutableList(transaction1, transaction2, invalidTransaction, transaction3);

        Block minedBlock = miner.generateBlock(transactions, "0001111111111");

        transactions.remove(invalidTransaction);
        assertEquals(transactions, minedBlock.getTransactions());
    }

    @Test
    void should_Skip_Fee_Transactions_With_Insufficient_Funds() {
        withInitialBalance("Bob", 100);
        withInitialBalance("Alice", 150);
        withInitialBalance("Someone", 50);
        Transaction transaction1 = new Transaction("xxx", "Bob", BigDecimal.valueOf(20), TransactionType.REWARD);
        Transaction transaction2 = new Transaction("Alice", "Bob", BigDecimal.valueOf(100), TransactionType.TRANSFER);
        //Bob has 220 coins at this moment, but tries to pay a fee of 250. This transaction is invalid and should be skipped
        Transaction invalidTransaction = new Transaction("Bob", "xxx", BigDecimal.valueOf(250), TransactionType.FEE);
        //This one is ok and should be added to the block
        Transaction transaction3 = new Transaction("Bob", "Someone", BigDecimal.valueOf(210), TransactionType.TRANSFER);
        List<Transaction> transactions = toMutableList(transaction1, transaction2, invalidTransaction, transaction3);

        Block minedBlock = miner.generateBlock(transactions, "0001111111111");

        transactions.remove(invalidTransaction);
        assertEquals(transactions, minedBlock.getTransactions());
    }

    private List<Transaction> toMutableList(Transaction... transactions) {
        List<Transaction> list = new ArrayList<>();
        Collections.addAll(list, transactions);
        return list;
    }

    private void withInitialBalance(String address, double balance) {
        when(blockChain.getBalanceForAddress(eq(address))).thenReturn(BigDecimal.valueOf(balance));
    }

    private List<Transaction> createValidTransactions() {
        withInitialBalance("Alice", 0);
        withInitialBalance("Ken", 0);
        withInitialBalance("Marie", 0);
        withInitialBalance("Bob", 0);
        withInitialBalance("Josh", 0);
        withInitialBalance("Hellen", 0);
        withInitialBalance("Loki", 100);
        Transaction transaction1 = new Transaction("xxx", "Alice", BigDecimal.valueOf(24), TransactionType.TOP_UP);
        Transaction transaction2 = new Transaction("xxx", "Ken", BigDecimal.valueOf(200), TransactionType.TOP_UP);
        Transaction transaction3 = new Transaction("xxx", "Marie", BigDecimal.valueOf(300), TransactionType.TOP_UP);
        Transaction transaction4 = new Transaction("Alice", "Bob", BigDecimal.valueOf(23), TransactionType.TRANSFER);
        Transaction transaction5 = new Transaction("Ken", "Josh", BigDecimal.valueOf(100), TransactionType.TRANSFER);
        Transaction transaction6 = new Transaction("Marie", "Hellen", BigDecimal.valueOf(200), TransactionType.TRANSFER);
        Transaction transaction7 = new Transaction("Hellen", "xxx", BigDecimal.valueOf(190), TransactionType.FEE);
        Transaction transaction8 = new Transaction("Hellen", "Marie", BigDecimal.valueOf(10), TransactionType.TRANSFER);
        Transaction transaction9 = new Transaction("Loki", "xxx", BigDecimal.valueOf(100), TransactionType.FEE);
        Transaction transaction10 = new Transaction("xxx", "Loki", BigDecimal.valueOf(20), TransactionType.REWARD);
        Transaction transaction11 = new Transaction("Loki", "Hellen", BigDecimal.valueOf(20), TransactionType.TRANSFER);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        transactions.add(transaction4);
        transactions.add(transaction5);
        transactions.add(transaction6);
        transactions.add(transaction7);
        transactions.add(transaction8);
        transactions.add(transaction9);
        transactions.add(transaction10);
        transactions.add(transaction11);
        return transactions;
    }

    private String generateZeroesString(int zeroesCount) {
        return "0".repeat(Math.max(0, zeroesCount));
    }
}
