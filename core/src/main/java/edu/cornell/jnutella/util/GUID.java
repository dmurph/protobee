package edu.cornell.jnutella.util;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.gnutella.routing.InvalidMessageException;

/**
 * <p><u>G</u>lobaly <u>U</u>nique <u>ID</u>entifier.</p>
 *
 * <p>GUIDs uniquely (we hope) identifie a gnutella node or message message.
 * It is composed from 16 bytes. The GUID of a phex host is randomly generated
 * by using the java.util.Random class and setting bytes 7 and 15 to OxFF and
 * Ox00 respectively as per
 * <a href="http://groups.yahoo.com/group/the_gdf/message/1397">
 * http://groups.yahoo.com/group/the_gdf/message/1397</a>.
 * </p>
 */
public class GUID {
    private static final int DATA_LENGTH = 16;
    private static final Random randomizer;

    static
    {
        long time = System.currentTimeMillis();
        int ipValue;
        try
        {
            ipValue = IOUtils.deserializeIntLE(
                InetAddress.getLocalHost().getAddress(), 0 );
        }
        catch (Exception e)
        {
            ipValue = 0;
        }
        int shift = IOUtils.determineBitCount( ipValue );
        long seed = time << shift;
        seed = seed + ipValue;
        randomizer = new Random( seed );
   
    }

    private byte[] bytes;

    /**
     * Create a random GUID for this server.
     */
    public GUID()
    {
        bytes = new byte[DATA_LENGTH];
        randomizer.nextBytes( bytes );

        // to meet current protocol standard set byte 9 to 0xFF and byte 16 to 0
        // see http://groups.yahoo.com/group/the_gdf/message/1397
        bytes[8] = (byte) 0xFF;
        bytes[15] = 0;
    }
    
    public GUID(byte[] bytes) throws InvalidMessageException {
       // to meet current protocol standard set byte 9 to 0xFF and byte 16 to 0
        // see http://groups.yahoo.com/group/the_gdf/message/1397
        if (bytes[8] != (byte) 0xFF){
          throw new InvalidMessageException("to meet current protocol standard, byte 8 must be 0xFF but is "+bytes[8]);
        }
        if (bytes[15] != 0){
          throw new InvalidMessageException("to meet current protocol standard, byte 8 must be 0xFF but is "+bytes[8]);
        }
        this.bytes = bytes;
    }
    
    /**
     * <p>Factory method to create a new GUID from a hexadecimal string
     * image.</p>
     *
     * @param hexValue  a String representing a 16 byte hexadecimal value.
     * @return  a new GUID with a byte image taken from hexValue
     */
    public GUID( String hexValue ) throws DecodingException {
      byte[] b = HexConverter.toBytes( hexValue );
      if (b.length!=DATA_LENGTH){
        throw new DecodingException("Hex String provided to GUID is not 16 bytes");
      }
      if (b[8] != (byte) 0xFF){
        throw new DecodingException("Hex String does not meet current protocol standard set. Byte 9 is not set to 0xFF.");
      }
      if (b[15] != 0){
        throw new DecodingException("Hex String does not meet current protocol standard set. Byte 16 is not set to 0.");
      }
      bytes = b;
    }

    public byte[] getBytes(){
      return bytes;
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(bytes);
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      GUID other = (GUID) obj;
      if (!Arrays.equals(bytes, other.bytes)) return false;
      return true;
    }

    @Override 
    public String toString(){
      return HexConverter.toHexString( bytes );
    }

    public static class GUIDComparator implements Comparator<GUID>
    {
        public int compare( GUID g1, GUID g2 )
        {
            int diff;
            for ( int i = 0; i < DATA_LENGTH; i++ )
            {
                diff = g1.bytes[i] - g2.bytes[i];
                if ( diff != 0 )
                {
                    return diff;
                }
            }
            return 0;
        }
    }

}