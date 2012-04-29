package edu.cornell.jnutella.gnutella.routing.tables;

import java.util.BitSet;

import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.util.ByteUtils;

/** Query routing table implementation for the QRP. */
public class CoreRoutingTable {
  /** The default table TTL representing INFINITY. */
  public static final byte DEFAULT_INFINITY_TTL = 0x07;

  /** The default table size of the QR table (64KB). */
  public static final int DEFAULT_TABLE_SIZE = 64 * 1024;

  /** The minimum table size of the QR table (16KB). */
  public static final int MIN_TABLE_SIZE = 16 * 1024;

  /** The maximum table size of the QR table (1024KB). */
  public static final int MAX_TABLE_SIZE = 1024 * 1024;

  /** Maximum fill ratio of the QRT (5%). */
  public static final int MAX_FILL_RATIO = 5;

  public static final String FILE_DELIMITERS = " -,._+/*()[]\\";

  public static final int A_INT = 0x4F1BBCDC;

  public static interface Factory {
    CoreRoutingTable create();
  }
  
  /**
   * The query routing table. A BitSet is used since the GDF decided that
   * QRP is only used between Leaf and Ultrapeers. The aggregated QRT
   * exchanged between UPs represents the last hop Leaf and UP as a single unit.
   */
  private BitSet qrTable;
  private long tableSize;       /** The size of the table;  */
  private byte tableBits;       /** This is the number of bits the hashing result is allowed to have. It is the base 2 log of tableSize. */
  private byte infinity;        /** The infinity value to use. */
  private int entryCount;       /** The number of marked slots in the qrTable. */
  private byte sequenceSize;    /** The total sequence size of the update messages. */
  private byte sequenceNumber;  /** The current sequence number of the update messages. */
  private int patchPosition;    /** The position on which to apply the next patch.*/

  /**
   * The resized QRT is during aggregation when cases the original QRT table
   * size does not match the size of the QRT to aggregate to.
   * We cache this resized QRT since we might need to use it again for
   * aggregation. The original QRT is not replaced by the resized version since
   * we need the original version for accurate query routing.
   */
  private BitSet resizedQRTable;

  @AssistedInject
  public CoreRoutingTable( ) {
    this.qrTable = new BitSet( DEFAULT_TABLE_SIZE );
    this.tableSize = DEFAULT_TABLE_SIZE;
    this.infinity = DEFAULT_INFINITY_TTL;
    this.tableBits = ByteUtils.calculateLog2( DEFAULT_TABLE_SIZE );
    entryCount = 0;
    sequenceSize = 0;
    sequenceNumber = 0;
    patchPosition = 0;
    resizedQRTable = null;
  }
  
  public CoreRoutingTable( int tableSize ) {
    this.qrTable = new BitSet( tableSize );
    this.tableSize = tableSize;
    this.infinity = DEFAULT_INFINITY_TTL;
    this.tableBits = ByteUtils.calculateLog2( tableSize );
    entryCount = 0;
    sequenceSize = 0;
    sequenceNumber = 0;
    patchPosition = 0;
    resizedQRTable = null;
  }

  public double getFillRatio() {
    return ( (double) qrTable.cardinality() / (double) tableSize ) * 100.0;
  }

  public BitSet getQrTable() {
    return qrTable;
  }

  public long getTableSize() {
    return tableSize;
  }

  public byte getTableBits() {
    return tableBits;
  }

  public byte getInfinity() {
    return infinity;
  }

  public int getEntryCount() {
    return entryCount;
  }

  public byte getSequenceSize() {
    return sequenceSize;
  }

  public byte getSequenceNumber() {
    return sequenceNumber;
  }

  public int getPatchPosition() {
    return patchPosition;
  }

  public BitSet getResizedQRTable() {
    return resizedQRTable;
  }

  public void setQrTable(BitSet qrTable) {
    this.qrTable = qrTable;
  }

  public void setTableSize(long tableSize) {
    this.tableSize = tableSize;
  }

  public void setTableBits(byte tableBits) {
    this.tableBits = tableBits;
  }

  public void setInfinity(byte infinity) {
    this.infinity = infinity;
  }

  public void setEntryCount(int entryCount) {
    this.entryCount = entryCount;
  }

  public void setSequenceSize(byte sequenceSize) {
    this.sequenceSize = sequenceSize;
  }

  public void setSequenceNumber(byte sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public void setPatchPosition(int patchPosition) {
    this.patchPosition = patchPosition;
  }

  public void setResizedQRTable(BitSet resizedQRTable) {
    this.resizedQRTable = resizedQRTable;
  }

  public void augmentPatchPosition(){
    this.patchPosition++;
  }

  public void augmentEntryCount(){
    this.entryCount++;
  }

  public void augmentSequenceNumber(){
    this.sequenceNumber++;
  }

  public void decrementEntryCount(){
    this.entryCount--;
  }

  public void set(int bitIndex){
    this.qrTable.set(bitIndex);
  }
  
  // shortcut to bitset functions
  
  public void clear(int bitIndex){
    this.qrTable.clear(bitIndex);
  }

  public void or(BitSet bitSetToAggregate){
    this.qrTable.or(bitSetToAggregate);
  }
  
  public boolean get(int bitIndex){
    return this.qrTable.get(bitIndex);
  }

}