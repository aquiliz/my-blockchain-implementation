package com.aquiliz.blockchain;

import com.aquiliz.blockchain.exception.InvalidBlockException;
import com.aquiliz.blockchain.exception.InvalidTransactionException;
import com.aquiliz.blockchain.model.Block;
import com.aquiliz.blockchain.model.Transaction;
import com.aquiliz.blockchain.model.TransactionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BlockChainTest {

    private static final int MINING_DIFFICULTY = 3;
    private static BlockChain blockChain;
    private static Block genesisBlock;
    private static Block firstBlock;

    @BeforeAll
    public static void beforeAll() {
        blockChain = BlockChain.getInstance();
        genesisBlock = generateGenesisBlock();
        firstBlock = generateFirstBlock(genesisBlock.getHash());
        blockChain.addGenesisBlock(genesisBlock, MINING_DIFFICULTY);
        blockChain.addBlock(firstBlock, MINING_DIFFICULTY);
    }

    @Test
    void should_Correctly_Calculate_Balance_OfAddresses() {
        assertEquals(BigDecimal.valueOf(0.25), blockChain.getBalanceForAddress("Alice"));
        assertEquals(BigDecimal.valueOf(73.75), blockChain.getBalanceForAddress("Bob"));
        assertEquals(BigDecimal.valueOf(50), blockChain.getBalanceForAddress("Ken"));
        assertEquals(BigDecimal.valueOf(110), blockChain.getBalanceForAddress("Marie"));
        assertEquals(BigDecimal.valueOf(120), blockChain.getBalanceForAddress("Loki"));
        assertEquals(BigDecimal.valueOf(100), blockChain.getBalanceForAddress("Josh"));
        assertEquals(BigDecimal.valueOf(220), blockChain.getBalanceForAddress("Hellen"));
        //miner1 should have accumulated 40 coins from the rewards for two mined blocks
        assertEquals(BigDecimal.valueOf(40), blockChain.getBalanceForAddress("miner1"));
    }

    @Test
    void should_Get_Genesis_Block() {
        assertEquals(genesisBlock, blockChain.getGenesisBlock());
    }

    @Test
    void should_Get_Latest_Block() {
        assertEquals(firstBlock, blockChain.getLatestBlock());
    }

    @Test
    void should_Fail_On_Attempt_ToAddGenesisBlock_On_NonEmptyBlockChain() {
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> blockChain.addGenesisBlock(firstBlock, MINING_DIFFICULTY));
        assertTrue(thrown.getMessage().contains("Failed to add blockId"));
        assertTrue(thrown.getMessage().contains("because the blockchain is not empty"));
    }

    @Test
    void should_Fail_On_Attempt_ToAddBlock_With_Invalid_PreviousHash() {
        Block block = new Block(createDummyTransactions(), "fakeHash", Instant.now().toEpochMilli());
        block.mine(MINING_DIFFICULTY, "miner1");

        InvalidBlockException thrown = assertThrows(
                InvalidBlockException.class,
                () -> blockChain.addBlock(block, MINING_DIFFICULTY));
        assertTrue(thrown.getMessage().contains("has invalid hash of previous block"));
    }

    @Test
    void should_Fail_On_Attempt_ToAddBlock_With_Null_FromOrTo_Address() {
        Transaction transaction1 = new Transaction(null, null, BigDecimal.valueOf(20), TransactionType.TRANSFER);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        Block block = new Block(transactions, firstBlock.getHash(), Instant.now().toEpochMilli());
        block.mine(MINING_DIFFICULTY, "miner1");

        InvalidTransactionException thrown = assertThrows(
                InvalidTransactionException.class,
                () -> blockChain.addBlock(block, MINING_DIFFICULTY));
        assertTrue(thrown.getMessage().contains("has null 'from' or 'to' address"));
    }

    @Test
    void should_Fail_On_Attempt_ToAddBlock_With_Same_FromOrTo_Address() {
        Transaction transaction1 = new Transaction("Alice", "Alice", BigDecimal.valueOf(20), TransactionType.TRANSFER);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        Block block = new Block(transactions, firstBlock.getHash(), Instant.now().toEpochMilli());
        block.mine(MINING_DIFFICULTY, "miner1");

        InvalidTransactionException thrown = assertThrows(
                InvalidTransactionException.class,
                () -> blockChain.addBlock(block, MINING_DIFFICULTY));
        assertTrue(thrown.getMessage().contains("Detected transaction that has the same 'from' and 'to' address"));
    }

    @Test
    void should_Fail_On_Attempt_ToAddBlock_That_IsNot_Mined() {
        Transaction transaction1 = new Transaction("Alice", "Bob", BigDecimal.valueOf(20), TransactionType.TRANSFER);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        Block block = new Block(transactions, firstBlock.getHash(), Instant.now().toEpochMilli());
        InvalidBlockException thrown = assertThrows(
                InvalidBlockException.class,
                () -> blockChain.addBlock(block, MINING_DIFFICULTY));
        assertTrue(thrown.getMessage().contains("does not satisfy the miningDifficulty=" + MINING_DIFFICULTY + " and will" +
                " not be added to the blockchain."));
    }

    @Test
    void should_Fail_On_Attempt_ToAddBlock_Without_Transactions() {
        Block block = Mockito.mock(Block.class);
        Mockito.when(block.getPreviousHash()).thenReturn(firstBlock.getHash());
        Mockito.when(block.getHash()).thenReturn(firstBlock.getHash() + "123");
        Mockito.when(block.getTransactions()).thenReturn(new ArrayList<>());
        InvalidBlockException thrown = assertThrows(
                InvalidBlockException.class,
                () -> blockChain.addBlock(block, MINING_DIFFICULTY));
        assertTrue(thrown.getMessage().contains("has a null or empty list of transactions and will not be added to the" +
                " blockchain."));
    }

    private static Block generateGenesisBlock() {
        Transaction transaction1 = new Transaction("xxx", "Alice", BigDecimal.valueOf(24), TransactionType.TOP_UP);
        Transaction transaction2 = new Transaction("xxx", "Ken", BigDecimal.valueOf(200), TransactionType.TOP_UP);
        Transaction transaction3 = new Transaction("xxx", "Marie", BigDecimal.valueOf(300), TransactionType.TOP_UP);
        Transaction transaction4 = new Transaction("xxx", "Loki", BigDecimal.valueOf(500), TransactionType.TOP_UP);
        Transaction transaction5 = new Transaction("Alice", "Bob", BigDecimal.valueOf(23), TransactionType.TRANSFER);
        Transaction transaction6 = new Transaction("Ken", "Josh", BigDecimal.valueOf(100), TransactionType.TRANSFER);
        Transaction transaction7 = new Transaction("Marie", "Hellen", BigDecimal.valueOf(200), TransactionType.TRANSFER);
        Transaction transaction8 = new Transaction("Hellen", "xxx", BigDecimal.valueOf(190), TransactionType.FEE);
        Transaction transaction9 = new Transaction("Hellen", "Marie", BigDecimal.valueOf(10), TransactionType.TRANSFER);
        Transaction transaction10 = new Transaction("Loki", "xxx", BigDecimal.valueOf(100), TransactionType.FEE);
        Transaction transaction11 = new Transaction("xxx", "Loki", BigDecimal.valueOf(20), TransactionType.REWARD);
        Transaction transaction12 = new Transaction("Loki", "Hellen", BigDecimal.valueOf(200), TransactionType.TRANSFER);
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
        transactions.add(transaction12);
        Block block = new Block(transactions, null, Instant.now().toEpochMilli());
        block.mine(MINING_DIFFICULTY, "miner1");
        return block;
    }

    private static Block generateFirstBlock(String genesisBlockHash) {
        Transaction transaction1 = new Transaction("Loki", "Hellen", BigDecimal.valueOf(100), TransactionType.TRANSFER);
        Transaction transaction2 = new Transaction("Hellen", "xxx", BigDecimal.valueOf(100), TransactionType.FEE);
        Transaction transaction3 = new Transaction("xxx", "Hellen", BigDecimal.valueOf(20), TransactionType.REWARD);
        Transaction transaction4 = new Transaction("Ken", "Bob", BigDecimal.valueOf(50), TransactionType.TRANSFER);
        Transaction transaction5 = new Transaction("Alice", "Bob", BigDecimal.valueOf(0.75), TransactionType.TRANSFER);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        transactions.add(transaction4);
        transactions.add(transaction5);
        Block block = new Block(transactions, genesisBlockHash, Instant.now().toEpochMilli());
        block.mine(MINING_DIFFICULTY, "miner1");
        return block;
    }

    private List<Transaction> createDummyTransactions() {
        Transaction transaction1 = new Transaction("xxx", "Alice", BigDecimal.valueOf(24), TransactionType.TOP_UP);
        Transaction transaction2 = new Transaction("xxx", "Ken", BigDecimal.valueOf(200), TransactionType.TOP_UP);
        Transaction transaction3 = new Transaction("xxx", "Marie", BigDecimal.valueOf(300), TransactionType.TOP_UP);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        return transactions;
    }
}
