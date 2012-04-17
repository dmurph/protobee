package edu.cornell.jnutella.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;

import com.google.inject.Singleton;

@Singleton
public class IOUtils {

  /**
   * A utility method to close ServerSockets
   */
  public void close(ServerSocket s) {
    if (s != null) {
      try {
        s.close();
      } catch (IOException ignored) {}
    }
  }

  /**
   * Deflates (compresses) the data.
   */
  public byte[] deflate(byte[] data) {
    OutputStream dos = null;
    Deflater def = null;
    try {
      def = new Deflater();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      dos = new DeflaterOutputStream(baos, def);
      dos.write(data, 0, data.length);
      dos.close(); // flushes bytes
      return baos.toByteArray();
    } catch (IOException impossible) {
      throw new RuntimeException(impossible);
    } finally {
      if (dos != null) {
        try {
          dos.close();
        } catch (IOException e) {}
      }
      if (def != null) {
        def.end();
      }
    }
  }

  /**
   * Inflates (uncompresses) the data.
   */
  public byte[] inflate(byte[] data) throws IOException {
    InputStream in = null;
    Inflater inf = null;
    try {
      inf = new Inflater();
      in = new InflaterInputStream(new ByteArrayInputStream(data), inf);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[64];
      while (true) {
        int read = in.read(buf, 0, buf.length);
        if (read == -1) break;
        out.write(buf, 0, read);
      }
      return out.toByteArray();
    } catch (OutOfMemoryError oome) {
      throw new IOException(oome.getMessage());
    } finally {
      if (in != null) {
        in.close();
      }
      if (inf != null) {
        inf.end();
      }
    }
  }

  public byte[] inflate(ChannelBuffer buffer, int length) throws IOException {
    InputStream in = null;
    Inflater inf = null;
    try {
      inf = new Inflater();
      in = new InflaterInputStream(new ChannelBufferInputStream(buffer, length));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[64];
      while (true) {
        int read = in.read(buf, 0, buf.length);
        if (read == -1) break;
        out.write(buf, 0, read);
      }
      return out.toByteArray();
    } catch (OutOfMemoryError oome) {
      throw new IOException(oome.getMessage());
    } finally {
      if (in != null) {
        in.close();
      }
      if (inf != null) {
        inf.end();
      }
    }
  }

  public byte[] readFully(InputStream in) throws IOException {
    ByteArrayOutputStream bos = null;
    try {
      bos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        bos.write(buffer, 0, read);
      }
      return bos.toByteArray();
    } finally {
      if (bos != null) {
        bos.close();
      }
      if (in != null) {
        in.close();
      }
    }
  }
  
  public static int deserializeIntLE(byte[] inbuf, int offset)
  {
      return  (inbuf[offset + 3]      ) << 24 |
              (inbuf[offset + 2] &0xff) << 16 |
              (inbuf[offset + 1] &0xff) <<  8 |
              (inbuf[offset]     &0xff);
  }
  
  /**
   * Returns the number of significant bits of num.
   * @param num the number to determine the significant number of bits from.
   */
  public static int determineBitCount( int num )
  {
      if ( num < 0 )
      {
          return 32;
      }

      if      (num <             0x10000)
          if      (num <           0x100)
              if      (num <        0x10)
                  if      (num <     0x4)
                      if  (num <     0x2)
                          if ( num == 0x0 ) return  0; else return  1;
                      else                return 2;
                  else if (num <     0x8) return  3; else return  4;
              else if (num <        0x40)
                  if      (num <    0x20) return  5; else return  6;
              else if (num <        0x80) return  7; else return  8;
          else if (num <          0x1000)
              if      (num <       0x400)
                  if      (num <   0x200) return  9; else return  10;
              else if (num <       0x800) return 11; else return 12;
          else if (num <          0x4000)
              if       (num <     0x2000) return 13; else return 14;
          else if (num <          0x8000) return 15; else return 16;
      else if (num <           0x1000000)
          if      (num <        0x100000)
              if      (num <     0x40000)
                  if      (num < 0x20000) return 17; else return 18;
              else if (num <     0x80000) return 19; else return 20;
          else if (num <        0x400000)
              if      (num <    0x200000) return 21; else return 22;
          else if (num <        0x800000) return 23; else return 24;
      else if (num <          0x10000000)
          if      (num <       0x4000000)
              if      (num <   0x2000000) return 25; else return 26;
          else if (num <       0x8000000) return 27; else return 28;
      else if (num <          0x40000000)
          if      (num <      0x20000000) return 29; else return 30;
      else                                return 31;
  }
}



