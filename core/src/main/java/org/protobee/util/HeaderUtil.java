package org.protobee.util;

import java.util.List;

import org.jboss.netty.handler.codec.http.HttpMessage;

import com.google.common.collect.ImmutableMap;

public class HeaderUtil {

  /**
   * Concatenates duplicate headers
   * 
   * @param message
   * @return
   */
  public static ImmutableMap<String, String> mergedGnutellaHeaders(HttpMessage message) {
    ImmutableMap.Builder<String, String> headerBuilder = ImmutableMap.builder();
    for (String name : message.getHeaderNames()) {
      List<String> headers = message.getHeaders(name);
      if (headers.size() == 0) {
        headerBuilder.put(name, "");
        continue;
      } else if (headers.size() == 1) {
        headerBuilder.put(name, headers.get(0));
        continue;
      }
      StringBuilder builder = new StringBuilder();
      boolean first = true;
      for (String string : headers) {
        if (first) {
          first = false;
          builder.append(string);
          continue;
        }
        builder.append(",").append(string);
      }
      headerBuilder.put(name, builder.toString());
    }
    return headerBuilder.build();
  }
}
