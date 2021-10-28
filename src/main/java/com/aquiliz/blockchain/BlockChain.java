package com.aquiliz.blockchain;

import com.aquiliz.blockchain.exception.InvalidBlockException;
import com.aquiliz.blockchain.exception.InvalidTransactionException;
import com.aquiliz.blockchain.model.Block;
import com.aquiliz.blockchain.model.Transaction;
import com.aquiliz.blockchain.model.TransactionType;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BlockChain {

    private static BlockChain instance;
    private final List<Block> blocks = new ArrayList<>();

    private BlockChain() {
        //disable instantiation
    }

    public static synchronized BlockChain getInstance() {
        if (instance == null) {
            instance = new BlockChain();
        }
        return instance;
    }

    public synchronized void addGenesisBlock(@NonNull Block block, int miningDifficulty) {
        if (!blocks.isEmpty()) {
            throw new IllegalStateException("Failed to add blockId=" + block.getId() + " as a genesis block," +
                    " because the blockchain is not empty.");
        }
        validateBlock(block, miningDifficulty);
        blocks.add(block);
    }

    public synchronized void addBlock(@NonNull Block block, int miningDifficulty) {
        if (!previousHashCorrect(block)) {
            throw new InvalidBlockException("Block with id='" + block.getId() + "' has invalid hash of previous block: "
                    + block.getPreviousHash());
        }
        validateBlock(block, miningDifficulty);
        blocks.add(block);
    }

    private void validateBlock(Block block, int miningDifficulty) {
        if (!block.getHash().startsWith(generateZeroesString(miningDifficulty))) {
            throw new InvalidBlockException("Hash of block with id=" + block.getId() + " does not satisfy the miningDifficulty=" +
                    miningDifficulty + " and will not be added to the blockchain.");
        }
        if (block.getTransactions() == null || block.getTransactions().isEmpty()) {
            throw new InvalidBlockException("Block with id=" + block.getId() + " has a null or empty list of transactions" +
                    " and will not be added to the blockchain.");
        }
        block.getTransactions().forEach(this::validateTransaction);
    }

    public synchronized Block getLatestBlock() {
        return blocks.isEmpty() ? null : blocks.get(blocks.size() - 1);
    }

    public Block getGenesisBlock() {
        return blocks.isEmpty() ? null : blocks.get(0);
    }

    public BigDecimal getBalanceForAddress(@NonNull String address) {
        BigDecimal balance = new BigDecimal(0);
        for (Block block : blocks) {
            for (Transaction transaction : block.getTransactions()) {
                if (transaction.getFromAddress().equals(address) && isSpending(transaction)) {
                    balance = balance.subtract(transaction.getAmount());
                } else if (transaction.getToAddress().equals(address) && isIncome(transaction)) {
                    balance = balance.add(transaction.getAmount());
                }
            }
        }
        return balance;
    }

    private boolean isIncome(Transaction transaction) {
        return transaction.getType() == TransactionType.REWARD || transaction.getType() == TransactionType.TRANSFER ||
                transaction.getType() == TransactionType.TOP_UP;
    }

    private boolean isSpending(Transaction transaction) {
        return transaction.getType() == TransactionType.FEE || transaction.getType() == TransactionType.TRANSFER;
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction.getFromAddress() == null || transaction.getToAddress() == null) {
            throw new InvalidTransactionException("Transaction id=" + transaction.getId() + " has null 'from' or 'to'" +
                    " address");
        }
        if (transaction.getFromAddress().equals(transaction.getToAddress())) {
            throw new InvalidTransactionException("Detected transaction that has the same 'from' and 'to' address: " +
                    transaction.getFromAddress() + " , transactionId=" + transaction.getId());
        }
    }

    private String generateZeroesString(int zeroesCount) {
        return "0".repeat(Math.max(0, zeroesCount));
    }

    private boolean previousHashCorrect(Block block) {
        return block.getPreviousHash() != null && block.getPreviousHash().equals(blocks.get(blocks.size() - 1).getHash());
    }
}
