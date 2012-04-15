package edu.cornell.jnutella.gnutella.messages.decoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.extension.BadGGEPBlockException;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.extension.GGEP.NeedsCompression;
import edu.cornell.jnutella.util.ByteUtils;
import edu.cornell.jnutella.util.IOUtils;

public class GGEPDecoder implements PartDecoder<GGEP> {

  @InjectLogger
  private Logger log;
  private final IOUtils ioUtils;

  @Inject
  public GGEPDecoder(IOUtils ioUtils) {
    this.ioUtils = ioUtils;
  }

  @Override
  public GGEP decode(ChannelBuffer buffer) throws DecodingException {

    try {
      if (buffer.readableBytes() < 4) {
        throw new BadGGEPBlockException();
      }

      byte ggepMagic = buffer.readByte();
      if (ggepMagic != GGEP.GGEP_PREFIX_MAGIC_NUMBER) {
        throw new BadGGEPBlockException();
      }

      Map<String, Object> properties = Maps.newHashMap();
      boolean onLastExtension = false;
      while (!onLastExtension) {

        // process extension header flags
        // bit order is interpreted as 76543210
        byte currByte = buffer.readByte();
        try {
          sanityCheck(currByte);
        } catch (ArrayIndexOutOfBoundsException malformedInput) {
          throw new BadGGEPBlockException();
        }
        onLastExtension = isLastExtension(currByte);
        boolean encoded = isEncoded(currByte);
        boolean compressed = isCompressed(currByte);
        int headerLen = deriveHeaderLength(currByte);

        // get the extension header

        String extensionHeader =
            buffer.toString(buffer.readerIndex(), headerLen, Charset.forName("UTF-8"));
        buffer.readerIndex(buffer.readerIndex() + headerLen);

        final int dataLength = deriveDataLength(buffer);

        byte[] extensionData = null;

        if (dataLength > 0) {
          // ok, data is present, get it....

          byte[] data = null;

          if (encoded) {
            try {
              data = cobsDecode(buffer, dataLength);
            } catch (IOException badCobsEncoding) {
              throw new BadGGEPBlockException("Bad COBS Encoding");
            }
          }

          if (compressed) {
            try {
              data = data != null ? ioUtils.inflate(data) : ioUtils.inflate(buffer, dataLength);
            } catch (IOException badData) {
              throw new BadGGEPBlockException("Bad compressed data");
            }
          }

          if (data == null) {
            data = new byte[dataLength];
            buffer.readBytes(data);
          }

          extensionData = data;
        }

        if (compressed)
          properties.put(extensionHeader, new NeedsCompression(extensionData));
        else
          properties.put(extensionHeader, extensionData);
      }
      return new GGEP(properties);
    } catch (BadGGEPBlockException e) {
      log.error("Bad ggep block");
      throw new DecodingException(e);
    }
  }

  private void sanityCheck(byte headerFlags) throws BadGGEPBlockException {
    // the 4th bit in the header's first byte must be 0.
    if ((headerFlags & 0x10) != 0) throw new BadGGEPBlockException();
  }

  private boolean isLastExtension(byte headerFlags) {
    boolean retBool = false;
    // the 8th bit in the header's first byte, when set, indicates that
    // this header is the last....
    if ((headerFlags & 0x80) != 0) retBool = true;
    return retBool;
  }


  private boolean isEncoded(byte headerFlags) {
    boolean retBool = false;
    // the 7th bit in the header's first byte, when set, indicates that
    // this header is the encoded with COBS
    if ((headerFlags & 0x40) != 0) retBool = true;
    return retBool;
  }


  private boolean isCompressed(byte headerFlags) {
    boolean retBool = false;
    // the 6th bit in the header's first byte, when set, indicates that
    // this header is the compressed with deflate
    if ((headerFlags & 0x20) != 0) retBool = true;
    return retBool;
  }


  private int deriveHeaderLength(byte headerFlags) throws BadGGEPBlockException {
    int retInt = 0;
    // bits 0-3 give the length of the extension header (1-15)
    retInt = headerFlags & 0x0F;
    if (retInt == 0) throw new BadGGEPBlockException();
    return retInt;
  }

  private int deriveDataLength(ChannelBuffer buffer) throws BadGGEPBlockException {
    int length = 0, iterations = 0;
    // the length is stored in at most 3 bytes....
    final int MAX_ITERATIONS = 3;
    byte currByte;
    do {
      currByte = buffer.readByte();
      length = (length << 6) | (currByte & 0x3f);
      if (++iterations > MAX_ITERATIONS) throw new BadGGEPBlockException();
    } while (0x40 != (currByte & 0x40));
    return length;
  }

  /*
   * COBS implementation.... For implementation details, please see:
   * http://www.acm.org/sigcomm/sigcomm97/papers/p062.pdf
   */

  /**
   * Decode a COBS-encoded byte array. The non-allowable byte value is 0.
   * 
   * @return the original COBS decoded string
   */
  static byte[] cobsDecode(ChannelBuffer buffer, int length) throws IOException {
    int currIndex = 0;
    int code = 0;
    ByteArrayOutputStream sink = new ByteArrayOutputStream();

    while (currIndex < length) {
      code = ByteUtils.ubyte2int(buffer.readByte());
      currIndex++;
      if ((currIndex + (code - 2)) >= length) throw new IOException();
      for (int i = 1; i < code; i++) {
        sink.write(buffer.readByte());
        currIndex++;
      }
      if (currIndex < length) // don't write this last one, it isn't used
        if (code < 0xFF) sink.write(0);
    }

    return sink.toByteArray();
  }
}
