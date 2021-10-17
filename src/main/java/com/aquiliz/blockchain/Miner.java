package com.aquiliz.blockchain;

import com.aquiliz.blockchain.model.Block;
import com.aquiliz.blockchain.model.Transaction;

import java.time.Instant;
import java.util.List;

public class Miner {

    private final int miningDifficulty;
    private final String minerAddress;

    public Miner(int miningDifficulty, String minerAddress) {
        this.miningDifficulty = miningDifficulty;
        this.minerAddress = minerAddress;
    }

    public Block generateBlock(List<Transaction> transactions, String hashOfPreviousBlock) {
        Block block = new Block(transactions, hashOfPreviousBlock, Instant.now().toEpochMilli());
        block.mine(miningDifficulty, minerAddress);
        return block;
    }
}
