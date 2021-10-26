package com.aquiliz.blockchain;

import com.aquiliz.blockchain.exception.InsufficientFundsException;
import com.aquiliz.blockchain.model.Block;
import com.aquiliz.blockchain.model.Transaction;
import com.aquiliz.blockchain.model.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Miner {

    private final int miningDifficulty;
    private final String minerAddress;
    private final BlockChain blockChain = BlockChain.getInstance();

    public Miner(int miningDifficulty, String minerAddress) {
        this.miningDifficulty = miningDifficulty;
        this.minerAddress = minerAddress;
    }

    public Block generateBlock(List<Transaction> transactions, String hashOfPreviousBlock) {
        Block block = new Block(transactions, hashOfPreviousBlock, Instant.now().toEpochMilli());
        validateBlockTransactions(block);
        block.mine(miningDifficulty, minerAddress);
        return block;
    }

    private void validateBlockTransactions(Block block) {
        Map<String, BigDecimal> balances = new HashMap<>();
        block.getTransactions().forEach(transaction -> {
            try {
                validateTransaction(balances, transaction);
            } catch (InsufficientFundsException e) {
                log.info(e.getMessage());
                block.getTransactions().remove(transaction);
                log.debug("Removed invalid transaction id={}", transaction.getId());
            }

        });
        log.info("All {} transaction(s) were successfully validated for block id={}", block.getTransactions().size(),
                block.getId());
    }

    private void validateTransaction(Map<String, BigDecimal> balances, Transaction transaction) {
        String fromAddress = transaction.getFromAddress();
        String toAddress = transaction.getToAddress();
        if (transaction.getType() == TransactionType.TRANSFER) {
            validateTransfer(balances, transaction, fromAddress, toAddress);
        } else if (transaction.getType() == TransactionType.FEE) {
            validateFee(balances, transaction, fromAddress);
        } else if (transaction.getType() == TransactionType.REWARD ||
                transaction.getType() == TransactionType.TOP_UP) {
            validateIncomeTransaction(balances, transaction, toAddress);
        }
    }

    private void validateTransfer(Map<String, BigDecimal> balances, Transaction transaction, String fromAddress, String toAddress) {
        BigDecimal openingBalanceFromAddress = blockChain.getBalanceForAddress(fromAddress);
        BigDecimal openingBalanceToAddress = blockChain.getBalanceForAddress(toAddress);
        if (!balances.containsKey(fromAddress)) {
            balances.put(fromAddress, openingBalanceFromAddress);
        }
        if (!balances.containsKey(toAddress)) {
            balances.put(toAddress, openingBalanceToAddress);
        }
        if (transaction.getAmount().compareTo(balances.get(fromAddress)) == 1) {
            throw new InsufficientFundsException("Transaction id='" + transaction.getId() + "' declined. Address '"
                    + fromAddress + "' does not possess the amount of " + transaction.getAmount() + " to send to: '" + toAddress + "'");
        } else {
            log.trace("Transfer transaction id={} successfully checked as valid.", transaction.getId());
            balances.put(fromAddress, balances.get(fromAddress).subtract(transaction.getAmount()));
            balances.put(toAddress, balances.get(toAddress).add(transaction.getAmount()));
        }
    }

    private void validateFee(Map<String, BigDecimal> balances, Transaction transaction, String fromAddress) {
        BigDecimal openingBalanceFromAddress = blockChain.getBalanceForAddress(fromAddress);
        if (!balances.containsKey(fromAddress)) {
            balances.put(fromAddress, openingBalanceFromAddress);
        }
        if (transaction.getAmount().compareTo(balances.get(fromAddress)) == 1) {
            throw new InsufficientFundsException("Transaction id='" + transaction.getId() + "' declined. Address '"
                    + fromAddress + "' does not possess the amount of " + transaction.getAmount() + " to pay fee.");
        } else {
            log.trace("Fee transaction id={} successfully checked as valid.", transaction.getId());
            balances.put(fromAddress, balances.get(fromAddress).subtract(transaction.getAmount()));
        }
    }

    private void validateIncomeTransaction(Map<String, BigDecimal> balances, Transaction transaction, String toAddress) {
        BigDecimal openingBalanceToAddress = blockChain.getBalanceForAddress(toAddress);
        if (!balances.containsKey(toAddress)) {
            balances.put(toAddress, openingBalanceToAddress);
        }
        log.trace("Income transaction id={} successfully checked as valid.", transaction.getId());
        balances.put(toAddress, balances.get(toAddress).add(transaction.getAmount()));
    }
}
