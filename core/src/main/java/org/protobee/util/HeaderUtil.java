package org.protobee.util;

import java.util.Collection;
import java.util.List;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.protobee.compatability.CompatabilityHeader;
import org.protobee.compatability.Headers;
import org.protobee.compatability.VersionRange;
import org.protobee.modules.ProtocolModule;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public class HeaderUtil {

  /**
   * Concatenates duplicate headers
   * 
   * @param message
   * @return
   */
  public static ImmutableMap<String, String> mergeDuplicatesWithComma(HttpMessage message) {
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

  public static Headers[] getHeadersFromModules(Collection<ProtocolModule> modules) {
    return Iterables.toArray(Iterables.transform(modules, new Function<ProtocolModule, Headers>() {
      @Override
      public Headers apply(ProtocolModule input) {
        Headers headers = input.getClass().getAnnotation(Headers.class);
        Preconditions.checkArgument(headers != null,
            "All protocol modules must have a headers annotation");
        return headers;
      }
    }), Headers.class);
  }

  public static HashMultimap<String, VersionRange> getRequiredVersions(Headers[] headersArray) {
    HashMultimap<String, VersionRange> tempMap = HashMultimap.create();
    for (Headers headers : headersArray) {
      for (CompatabilityHeader header : headers.required()) {
        tempMap.put(header.name(), new VersionRange(header));
      }
    }
    return tempMap;
  }
  
  public static HashMultimap<String, VersionRange> getRequestedVersions(Headers[] headersArray) {
    HashMultimap<String, VersionRange> tempMap = HashMultimap.create();
    for (Headers headers : headersArray) {
      for (CompatabilityHeader header : headers.requested()) {
        tempMap.put(header.name(), new VersionRange(header));
      }
    }
    return tempMap;
  }
  
  public static HashMultimap<String, VersionRange> getExcludingVersions(Headers[] headersArray) {
    HashMultimap<String, VersionRange> tempMap = HashMultimap.create();
    for (Headers headers : headersArray) {
      for (CompatabilityHeader header : headers.excluding()) {
        tempMap.put(header.name(), new VersionRange(header));
      }
    }
    return tempMap;
  }
}
