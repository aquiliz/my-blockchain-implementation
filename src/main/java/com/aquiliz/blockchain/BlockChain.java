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
public final class BlockChain {

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

    public synchronized void addBlock(Block block) {
        if (!previousHashCorrect(block)) {
            throw new InvalidBlockException("Block with id='" + block.getId() + "' has invalid hash of previous block: "
                    + block.getPreviousHash());
        }
        //TODO check if hash starts with X zeroes ?
        blocks.add(block);
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
                validateTransaction(transaction);
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
            throw new InvalidTransactionException("Detected transaction that has null 'from' or 'to' address");
        }
        if (transaction.getFromAddress().equals(transaction.getToAddress())) {
            throw new InvalidTransactionException("Detected transaction that has the same 'from' and 'to' address: " +
                    transaction.getFromAddress());
        }
    }

    private boolean previousHashCorrect(Block block) {
        return block.getPreviousHash() != null && block.getPreviousHash().equals(blocks.get(blocks.size() - 1).getHash());
    }
}
