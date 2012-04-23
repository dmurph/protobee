package edu.cornell.jnutella.gnutella.routing;

import java.util.Arrays;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class PatchBody extends RoutingBody {

  public static final byte COMPRESSOR_NONE = 0x00;
  public static final byte COMPRESSOR_ZLIB = 0x01;
  public static final int MAX_MESSAGE_DATA_SIZE = 4 * 1024; // 4KB
  
  private byte sequenceNum;
  private byte sequenceSize;
  private byte compressor;
  private byte entryBits;
  private byte[] data;
  
  @AssistedInject
  public PatchBody(@Assisted("sequenceNum") byte sequenceNum,
                   @Assisted("sequenceSize") byte sequenceSize,
                   @Assisted("compressor") byte compressor,
                   @Assisted("entryBits") byte entryBits,
                   @Assisted byte[] data) {
    super(PATCH_TABLE_VARIANT);
    this.sequenceNum = sequenceNum;
    this.sequenceSize = sequenceSize;
    this.compressor = compressor;
    this.entryBits = entryBits;
    this.data = data;
  }

  public byte getVariant() {
    return variant;
  }

  public byte getSequenceNum() {
    return sequenceNum;
  }

  public byte getSequenceSize() {
    return sequenceSize;
  }

  public byte getCompressor() {
    return compressor;
  }

  public byte getEntryBits() {
    return entryBits;
  }

  public byte[] getData() {
    return data;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + compressor;
    result = prime * result + Arrays.hashCode(data);
    result = prime * result + entryBits;
    result = prime * result + sequenceNum;
    result = prime * result + sequenceSize;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    PatchBody other = (PatchBody) obj;
    if (compressor != other.compressor) return false;
    if (!Arrays.equals(data, other.data)) return false;
    if (entryBits != other.entryBits) return false;
    if (sequenceNum != other.sequenceNum) return false;
    if (sequenceSize != other.sequenceSize) return false;
    return true;
  }
  
}
