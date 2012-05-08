package org.protobee.gnutella;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class GnutellaHttpRequestEncoder extends HttpMessageEncoder {
  // space ' '
  static final byte SP = 32;

  /**
   * Carriage return
   */
  static final byte CR = 13;

  /**
   * Line feed character
   */
  static final byte LF = 10;

  public GnutellaHttpRequestEncoder() {
    super();
  }

  @Override
  protected void encodeInitialLine(ChannelBuffer buf, HttpMessage message) throws Exception {
    HttpRequest request = (HttpRequest) message;
    buf.writeBytes(request.getProtocolVersion().getProtocolName().toString().getBytes("ASCII"));
    buf.writeByte(SP);
    buf.writeBytes(request.getMethod().toString().getBytes("ASCII"));
    HttpVersion version = request.getProtocolVersion();
    String versionWithoutName = "/" + version.getMajorVersion() + "." + version.getMinorVersion();
    buf.writeBytes(versionWithoutName.getBytes("ASCII"));
    buf.writeByte(CR);
    buf.writeByte(LF);
  }
}
