package edu.cornell.jnutella.gnutella;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class GnutellaHttpRequestDecoder extends HttpMessageDecoder {

  public GnutellaHttpRequestDecoder() {
  }

  public GnutellaHttpRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
    super(maxInitialLineLength, maxHeaderSize, maxChunkSize);
  }


  @Override
  protected boolean isDecodingRequest() {
    return true;
  }

  @Override
  protected HttpMessage createMessage(String[] initialLine) throws Exception {
    String protocol = initialLine[0];
    String[] methodVersion = initialLine[1].split("/");
    String method = methodVersion[0];
    String version = methodVersion[1];
    return new DefaultHttpRequest(new HttpVersion(protocol + "/" + version, true),
        HttpMethod.valueOf(method), "");
  }
}
