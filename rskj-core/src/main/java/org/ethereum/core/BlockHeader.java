/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 * (derived from ethereumJ library, Copyright (c) 2016 <ether.camp>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.core;

import com.google.common.collect.Lists;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;
import org.spongycastle.pqc.math.linearalgebra.ByteUtils;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.util.ByteUtil.toHexString;

/**
 * Block header is a value object containing
 * the basic information of a block
 */

// TODO review implements SerializableObject
public class BlockHeader implements SerializableObject {


    /* The SHA3 256-bit hash of the parent block, in its entirety */
    private byte[] parentHash;
    /* The SHA3 256-bit hash of the uncles list portion of this block */
    private byte[] unclesHash;
    /* The 160-bit address to which all fees collected from the
     * successful mining of this block be transferred; formally */
    private byte[] coinbase;
    /* The SHA3 256-bit hash of the root node of the state trie,
     * after all transactions are executed and finalisations applied */
    private byte[] stateRoot;
    /* The SHA3 256-bit hash of the root node of the trie structure
     * populated with each transaction in the transaction
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_recipe)]
     * of the block */
    private byte[] txTrieRoot;
    /* The SHA3 256-bit hash of the root node of the trie structure
     * populated with each transaction recipe in the transaction recipes
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_recipe)]
     * of the block */
    private byte[] receiptTrieRoot;
    /* The bloom filter for the logs of the block */
    private byte[] logsBloom;
    /* A scalar value corresponding to the difficulty level of this block.
     * This can be calculated from the previous block’s difficulty level
     * and the timestamp */
    private byte[] difficulty;
    /* A scalar value equalBytes to the reasonable output of Unix's time()
     * at this block's inception */
    private long timestamp;
    /* A scalar value equalBytes to the number of ancestor blocks.
     * The genesis block has a number of zero */
    private long number;
    /* A scalar value equalBytes to the current limit of gas expenditure per block */
    private byte[] gasLimit;
    /* A scalar value equalBytes to the total gas used in transactions in this block */
    private long gasUsed;
    /* A scalar value equalBytes to the total paid fees in transactions in this block */
    private long paidFees;

     /* An arbitrary byte array containing data relevant to this block.
     * With the exception of the genesis block, this must be 32 bytes or fewer */
    private byte[] extraData;

    /* The 81-byte bitcoin block header for merged mining */
    private byte[] bitcoinMergedMiningHeader;
    /* The bitcoin merkle proof of coinbase tx for merged mining */
    private byte[] bitcoinMergedMiningMerkleProof;
    /* The bitcoin protobuf serialized coinbase tx for merged mining */
    private byte[] bitcoinMergedMiningCoinbaseTransaction;
    /*The mgp for a tx to be included in the block*/
    private byte[] minimumGasPrice;
    private int uncleCount;

    public BlockHeader(byte[] encoded) {
        this((RLPList) RLP.decode2(encoded).get(0));
    }

    public BlockHeader(RLPList rlpHeader) {
        this.parentHash = rlpHeader.get(0).getRLPData();
        this.unclesHash = rlpHeader.get(1).getRLPData();
        this.coinbase = rlpHeader.get(2).getRLPData();
        this.stateRoot = rlpHeader.get(3).getRLPData();
        if (this.stateRoot == null)
            this.stateRoot = EMPTY_TRIE_HASH;

        this.txTrieRoot = rlpHeader.get(4).getRLPData();
        if (this.txTrieRoot == null)
            this.txTrieRoot = EMPTY_TRIE_HASH;

        this.receiptTrieRoot = rlpHeader.get(5).getRLPData();
        if (this.receiptTrieRoot == null)
            this.receiptTrieRoot = EMPTY_TRIE_HASH;

        this.logsBloom = rlpHeader.get(6).getRLPData();
        this.difficulty = rlpHeader.get(7).getRLPData();

        byte[] nrBytes = rlpHeader.get(8).getRLPData();
        byte[] glBytes = rlpHeader.get(9).getRLPData();
        byte[] guBytes = rlpHeader.get(10).getRLPData();
        byte[] tsBytes = rlpHeader.get(11).getRLPData();

        this.number = nrBytes == null ? 0 : (new BigInteger(1, nrBytes)).longValue();

        this.gasLimit = glBytes;
        this.gasUsed = guBytes == null ? 0 : (new BigInteger(1, guBytes)).longValue();
        this.timestamp = tsBytes == null ? 0 : (new BigInteger(1, tsBytes)).longValue();

        this.extraData = rlpHeader.get(12).getRLPData();

        byte[] pfBytes = rlpHeader.get(13).getRLPData();
        this.paidFees = pfBytes == null ? 0 : (new BigInteger(1, pfBytes)).longValue();
        this.minimumGasPrice = rlpHeader.get(14).getRLPData();
        int r=15;
        if ((rlpHeader.size() == 19) || (rlpHeader.size() == 16)) {
            byte[] ucBytes = rlpHeader.get(r++).getRLPData();
            this.uncleCount = ucBytes == null ? 0 : (new BigInteger(1, ucBytes)).intValue();
        }
        if (rlpHeader.size() > r) {
            this.bitcoinMergedMiningHeader = rlpHeader.get(r++).getRLPData();
            this.bitcoinMergedMiningMerkleProof = rlpHeader.get(r++).getRLPData();
            this.bitcoinMergedMiningCoinbaseTransaction = rlpHeader.get(r++).getRLPData();

        }
    }

    public BlockHeader(byte[] parentHash, byte[] unclesHash, byte[] coinbase,
                       byte[] logsBloom, byte[] difficulty, long number,
                       byte[] gasLimit, long gasUsed, long timestamp,
                       byte[] extraData,
                       byte[] minimumGasPrice,
                       int uncleCount) {
        this.parentHash = parentHash;
        this.unclesHash = unclesHash;
        this.coinbase = coinbase;
        this.logsBloom = logsBloom;
        this.difficulty = difficulty;
        this.number = number;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.timestamp = timestamp;
        this.extraData = extraData;
        this.stateRoot = ByteUtils.clone(EMPTY_TRIE_HASH);
        this.minimumGasPrice = minimumGasPrice;
        this.receiptTrieRoot = ByteUtils.clone(EMPTY_TRIE_HASH);
        this.uncleCount = uncleCount;
    }

    public BlockHeader(byte[] parentHash, byte[] unclesHash, byte[] coinbase,
                       byte[] logsBloom, byte[] difficulty, long number,
                       byte[] gasLimit, long gasUsed, long timestamp,
                       byte[] extraData,
                       byte[] bitcoinMergedMiningHeader, byte[] bitcoinMergedMiningMerkleProof,
                       byte[] bitcoinMergedMiningCoinbaseTransaction,
                       byte[] minimumGasPrice,
                       int uncleCount) {
        this.parentHash = parentHash;
        this.unclesHash = unclesHash;
        this.coinbase = coinbase;
        this.logsBloom = logsBloom;
        this.difficulty = difficulty;
        this.number = number;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.timestamp = timestamp;
        this.extraData = extraData;
        this.stateRoot = ByteUtils.clone(EMPTY_TRIE_HASH);
        this.bitcoinMergedMiningHeader = bitcoinMergedMiningHeader;
        this.bitcoinMergedMiningMerkleProof = bitcoinMergedMiningMerkleProof;
        this.bitcoinMergedMiningCoinbaseTransaction = bitcoinMergedMiningCoinbaseTransaction;
        this.minimumGasPrice = minimumGasPrice;
        this.receiptTrieRoot = ByteUtils.clone(EMPTY_TRIE_HASH);
        this.uncleCount = uncleCount;
    }

    public boolean isGenesis() {
        return this.getNumber() == Genesis.NUMBER;
    }

    public byte[] getParentHash() {
        return parentHash;
    }

    public int getUncleCount() {
        return uncleCount;
    }

    public void setUncleCount(int uCount) {
        uncleCount = uCount;
    }

    public byte[] getUnclesHash() {
        return unclesHash;
    }

    public void setUnclesHash(byte[] unclesHash) {
        this.unclesHash = unclesHash;
    }

    public byte[] getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(byte[] coinbase) {
        this.coinbase = coinbase;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }

    public byte[] getTxTrieRoot() {
        return txTrieRoot;
    }

    public void setReceiptsRoot(byte[] receiptTrieRoot) {
        this.receiptTrieRoot = receiptTrieRoot;
    }

    public byte[] getReceiptsRoot() {
        return receiptTrieRoot;
    }

    public void setTransactionsRoot(byte[] stateRoot) {
        this.txTrieRoot = stateRoot;
    }


    public byte[] getLogsBloom() {
        return logsBloom;
    }

    public byte[] getDifficulty() {
        return difficulty;
    }

    public BigInteger getDifficultyBI() {
        return new BigInteger(1, difficulty);
    }


    public void setDifficulty(byte[] difficulty) {
        this.difficulty = difficulty;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public byte[] getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(byte[] gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setPaidFees(long paidFees) {
        this.paidFees = paidFees;
    }

    public long getPaidFees() {
        return this.paidFees;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public byte[] getExtraData() {
        return extraData;
    }

    public void setLogsBloom(byte[] logsBloom) {
        this.logsBloom = logsBloom;
    }

    public void setExtraData(byte[] extraData) {
        this.extraData = extraData;
    }

    public byte[] getHash() {
        return HashUtil.sha3(getEncoded());
    }

    public byte[] getEncoded() {
        return this.getEncoded(true); // with nonce
    }

    public byte[] getEncodedWithoutNonceMergedMiningFields() {
        return this.getEncoded(false);
    }

    public byte[] getMinimumGasPrice() {
        return this.minimumGasPrice;
    }

    public void setMinimumGasPrice(byte[] minimumGasPrice) {
        this.minimumGasPrice = minimumGasPrice;
    }

    public byte[] getEncoded(boolean withMergedMiningFields) {
        byte[] parentHash = RLP.encodeElement(this.parentHash);

        byte[] unclesHash = RLP.encodeElement(this.unclesHash);
        byte[] coinbase = RLP.encodeElement(this.coinbase);

        byte[] stateRoot = RLP.encodeElement(this.stateRoot);

        if (txTrieRoot == null) this.txTrieRoot = EMPTY_TRIE_HASH;
        byte[] txTrieRoot = RLP.encodeElement(this.txTrieRoot);

        if (receiptTrieRoot == null) this.receiptTrieRoot = EMPTY_TRIE_HASH;
        byte[] receiptTrieRoot = RLP.encodeElement(this.receiptTrieRoot);

        byte[] logsBloom = RLP.encodeElement(this.logsBloom);
        byte[] difficulty = RLP.encodeElement(this.difficulty);
        byte[] number = RLP.encodeBigInteger(BigInteger.valueOf(this.number));
        byte[] gasLimit = RLP.encodeElement(this.gasLimit);
        byte[] gasUsed = RLP.encodeBigInteger(BigInteger.valueOf(this.gasUsed));
        byte[] timestamp = RLP.encodeBigInteger(BigInteger.valueOf(this.timestamp));
        byte[] extraData = RLP.encodeElement(this.extraData);
        byte[] paidFees = RLP.encodeBigInteger(BigInteger.valueOf(this.paidFees));
        byte[] mgp = RLP.encodeElement(this.minimumGasPrice);
        List<byte[]> fieldToEncodeList = Lists.newArrayList(parentHash, unclesHash, coinbase,
                stateRoot, txTrieRoot, receiptTrieRoot, logsBloom, difficulty, number,
                gasLimit, gasUsed, timestamp, extraData, paidFees, mgp);

        byte[] uncleCount = RLP.encodeBigInteger(BigInteger.valueOf(this.uncleCount));
        fieldToEncodeList.add(uncleCount);

        if (withMergedMiningFields && hasMiningFields()) {
            byte[] bitcoinMergedMiningHeader = RLP.encodeElement(this.bitcoinMergedMiningHeader);
            fieldToEncodeList.add(bitcoinMergedMiningHeader);
            byte[] bitcoinMergedMiningMerkleProof = RLP.encodeElement(this.bitcoinMergedMiningMerkleProof);
            fieldToEncodeList.add(bitcoinMergedMiningMerkleProof);
            byte[] bitcoinMergedMiningCoinbaseTransaction = RLP.encodeElement(this.bitcoinMergedMiningCoinbaseTransaction);
            fieldToEncodeList.add(bitcoinMergedMiningCoinbaseTransaction);
        }


        return RLP.encodeList(fieldToEncodeList.toArray(new byte[][]{}));
    }

    // Warining: This method does not uses the object's attributes
    public static byte[] getUnclesEncodedEx(List<BlockHeader> uncleList) {

        byte[][] unclesEncoded = new byte[uncleList.size()][];
        int i = 0;
        for (BlockHeader uncle : uncleList) {
            unclesEncoded[i] = uncle.getEncoded();
            ++i;
        }
        return RLP.encodeList(unclesEncoded);
    }

    public boolean hasMiningFields() {
        if (this.bitcoinMergedMiningCoinbaseTransaction != null && this.bitcoinMergedMiningCoinbaseTransaction.length > 0)
            return true;

        if (this.bitcoinMergedMiningHeader != null && this.bitcoinMergedMiningHeader.length > 0)
            return true;

        if (this.bitcoinMergedMiningMerkleProof != null && this.bitcoinMergedMiningMerkleProof.length > 0)
            return true;

        return false;
    }

    public static byte[] getUnclesEncoded(List<BlockHeader> uncleList) {

        byte[][] unclesEncoded = new byte[uncleList.size()][];
        int i = 0;
        for (BlockHeader uncle : uncleList) {
            unclesEncoded[i] = uncle.getEncoded();
            ++i;
        }
        return RLP.encodeList(unclesEncoded);
    }

    public byte[] getPowBoundary() {
        return BigIntegers.asUnsignedByteArray(32, BigInteger.ONE.shiftLeft(256).divide(getDifficultyBI()));
    }


    public BigInteger calcDifficulty(BlockHeader parent) {
        return SystemProperties.CONFIG.getBlockchainConfig().getConfigForBlock(getNumber()).
                calcDifficulty(this, parent);
    }

    public String toString() {
        return toStringWithSuffix("\n");
    }

    private String toStringWithSuffix(final String suffix) {
        StringBuilder toStringBuff = new StringBuilder();
        toStringBuff.append("  parentHash=").append(toHexString(parentHash)).append(suffix);
        toStringBuff.append("  unclesHash=").append(toHexString(unclesHash)).append(suffix);
        toStringBuff.append("  coinbase=").append(toHexString(coinbase)).append(suffix);
        toStringBuff.append("  stateRoot=").append(toHexString(stateRoot)).append(suffix);
        toStringBuff.append("  txTrieHash=").append(toHexString(txTrieRoot)).append(suffix);
        toStringBuff.append("  receiptsTrieHash=").append(toHexString(receiptTrieRoot)).append(suffix);
        toStringBuff.append("  difficulty=").append(toHexString(difficulty)).append(suffix);
        toStringBuff.append("  number=").append(number).append(suffix);
        toStringBuff.append("  gasLimit=").append(toHexString(gasLimit)).append(suffix);
        toStringBuff.append("  gasUsed=").append(gasUsed).append(suffix);
        toStringBuff.append("  timestamp=").append(timestamp).append(" (").append(Utils.longToDateTime(timestamp)).append(")").append(suffix);
        toStringBuff.append("  extraData=").append(toHexString(extraData)).append(suffix);
        toStringBuff.append("  minGasPrice=").append(toHexString(minimumGasPrice)).append(suffix);

        return toStringBuff.toString();
    }

    public String toFlatString() {
        return toStringWithSuffix("");
    }

    // TODO added to comply with SerializableObject
    public byte[] getRawHash() {
        return getHash();
    }

    // TODO added to comply with SerializableObject
    public byte[] getEncodedRaw() {
        return getEncoded();
    }

    public byte[] getBitcoinMergedMiningHeader() {
        return bitcoinMergedMiningHeader;
    }

    public void setBitcoinMergedMiningHeader(byte[] bitcoinMergedMiningHeader) {
        this.bitcoinMergedMiningHeader = bitcoinMergedMiningHeader;
    }

    public byte[] getBitcoinMergedMiningMerkleProof() {
        return bitcoinMergedMiningMerkleProof;
    }

    public void setBitcoinMergedMiningMerkleProof(byte[] bitcoinMergedMiningMerkleProof) {
        this.bitcoinMergedMiningMerkleProof = bitcoinMergedMiningMerkleProof;
    }

    public byte[] getBitcoinMergedMiningCoinbaseTransaction() {
        return bitcoinMergedMiningCoinbaseTransaction;
    }

    public void setBitcoinMergedMiningCoinbaseTransaction(byte[] bitcoinMergedMiningCoinbaseTransaction) {
        this.bitcoinMergedMiningCoinbaseTransaction = bitcoinMergedMiningCoinbaseTransaction;
    }

    public String getShortHashForMergedMining() {
        return Hex.toHexString(getHashForMergedMining()).substring(0, 6);
    }

    public byte[] getHashForMergedMining() {
        return HashUtil.sha3(getEncoded(false));
    }
}
