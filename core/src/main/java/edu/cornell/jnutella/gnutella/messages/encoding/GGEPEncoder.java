package edu.cornell.jnutella.gnutella.messages.encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.inject.Inject;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.util.IOUtils;

public class GGEPEncoder implements PartEncoder<GGEPEncoder.EncoderInput> {

  private final IOUtils ioUtils;

  @Inject
  public GGEPEncoder(IOUtils ioUtils) {
    this.ioUtils = ioUtils;
  }

  public static class EncoderInput {
    private final GGEP ggep;
    private final boolean useCobs;

    public EncoderInput(GGEP ggep, boolean useCobs) {
      this.ggep = ggep;
      this.useCobs = useCobs;
    }
    
    public GGEP getGgep() {
      return ggep;
    }

    public boolean isUseCobs() {
      return useCobs;
    }
  }

  /**
   * Writes this GGEP instance as a properly formatted GGEP Block.
   * 
   * @param channel This ChannelBuffer instance is used....
   * @param toEncode This EncoderInput instance is written to channel.
   * @exception IOException Thrown if had error writing to channel.
   */
  @Override
  public void encode(ChannelBuffer channel, EncoderInput toEncode) {
    GGEP ggep = toEncode.getGgep();
    Set<String> headers = ggep.getHeaders();

    if (headers.size() > 0) {
      toEncode.getGgep();
      // start with the magic prefix -- out.write(GGEP_PREFIX_MAGIC_NUMBER);
      channel.writeByte(GGEP.GGEP_PREFIX_MAGIC_NUMBER);

      Iterator<String> headersItr = headers.iterator();
      // for each header, write the GGEP header and data
      while (headersItr.hasNext()) {
        String currHeader = headersItr.next();
        byte[] currData = ggep.get(currHeader);
        int dataLen = 0;
        boolean shouldEncode = shouldCOBSEncode(currData, toEncode.isUseCobs());
        boolean shouldCompress = ggep.isCompressed(currHeader);
        if (currData != null) {
          if (shouldCompress) {
            currData = ioUtils.deflate(currData);
            if (currData.length > GGEP.MAX_VALUE_SIZE_IN_BYTES)
              throw new IllegalArgumentException("value for [" + currHeader
                  + "] too large after compression");
          }
          if (shouldEncode) currData = cobsEncode(currData);
          dataLen = currData.length;
        }
        writeHeader(currHeader, dataLen, !headersItr.hasNext(), channel, shouldEncode,
            shouldCompress);
        if (dataLen > 0) channel.writeBytes(currData);
      }
    }

  }

  private void writeHeader(String header, final int dataLen, boolean isLast, ChannelBuffer channel,
      boolean isEncoded, boolean isCompressed) {

    // 1. WRITE THE HEADER FLAGS
    int flags = 0x00;
    if (isLast) flags |= 0x80;
    if (isEncoded) flags |= 0x40;
    if (isCompressed) flags |= 0x20;
    flags |= header.getBytes().length;
    channel.writeByte(flags);

    // 2. WRITE THE HEADER
    channel.writeBytes(header.getBytes());

    // 3. WRITE THE DATA LEN
    // possibly 3 bytes
    int toWrite;
    int begin = dataLen & 0x3F000;
    if (dataLen > 0x00000fff) {
      begin = begin >> 12; // relevant bytes at the bottom now...
      toWrite = 0x80 | begin;
      channel.writeByte(toWrite);
    }
    int middle = dataLen & 0xFC0;
    if (dataLen > 0x0000003f) {
      middle = middle >> 6; // relevant bytes at the bottom now...
      toWrite = 0x80 | middle;
      channel.writeByte(toWrite);
    }
    int end = dataLen & 0x3F; // shut off everything except last 6 bits...
    toWrite = 0x40 | end;
    channel.writeByte(toWrite);
  }

  private final boolean shouldCOBSEncode(byte[] data, boolean useCOBS) {
    // if nulls are allowed from construction time and if nulls are present
    // in the data...
    return (useCOBS && containsNull(data));
  }

  private boolean containsNull(byte[] bytes) {
    if (bytes != null) {
      for (int i = 0; i < bytes.length; i++)
        if (bytes[i] == 0x0) return true;
    }
    return false;
  }

  static byte[] cobsEncode(byte[] src) {
    final int srcLen = src.length;
    int code = 1;
    int currIndex = 0;
    // COBS encoding adds no more than one byte of overhead for every 254
    // bytes of packet data
    final int maxEncodingLen = src.length + ((src.length + 1) / 254) + 1;
    ByteArrayOutputStream sink = new ByteArrayOutputStream(maxEncodingLen);
    int writeStartIndex = -1;

    while (currIndex < srcLen) {
      if (src[currIndex] == 0) {
        // currIndex was incremented so take 1 less
        code = cobsFinishBlock(code, sink, src, writeStartIndex, (currIndex - 1));
        writeStartIndex = -1;
      } else {
        if (writeStartIndex < 0) writeStartIndex = currIndex;
        code++;
        if (code == 0xFF) {
          code = cobsFinishBlock(code, sink, src, writeStartIndex, currIndex);
          writeStartIndex = -1;
        }
      }
      currIndex++;
    }

    // currIndex was incremented so take 1 less
    cobsFinishBlock(code, sink, src, writeStartIndex, (currIndex - 1));
    return sink.toByteArray();
  }

  static int cobsFinishBlock(int code, ByteArrayOutputStream sink, byte[] src, int begin, int end) {
    sink.write(code);
    if (begin > -1) sink.write(src, begin, (end - begin) + 1);
    return (byte) 0x01;
  }

}
