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
}
