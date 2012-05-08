package org.protobee.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.channel.local.LocalClientChannelFactory;
import org.protobee.guice.netty.ClientFactory;
import org.protobee.guice.scopes.ProtocolScope;

import com.google.common.net.InetAddresses;
import com.google.inject.Inject;

@ProtocolScope
public class SocketAddressUtils {

  private final boolean isLocal;

  @Inject
  public SocketAddressUtils(@ClientFactory ChannelFactory clientFactory) {
    isLocal = clientFactory instanceof LocalClientChannelFactory;
  }

  public static int getIPFromAddress(SocketAddress address) {
    if (address instanceof LocalAddress) {
      LocalAddress local = (LocalAddress) address;
      String id = local.getId();
      String ip = id.split(":")[0];
      return ByteUtils.beb2int(InetAddresses.forString(ip).getAddress(), 0);
    } else if (address instanceof InetSocketAddress) {
      InetSocketAddress socketAddr = (InetSocketAddress) address;
      return ByteUtils.beb2int(socketAddr.getAddress().getAddress(), 0);
    }
    throw new IllegalArgumentException("Unknown address type");
  }

  public static int getPortFromAddress(SocketAddress address) {
    if (address instanceof LocalAddress) {
      LocalAddress local = (LocalAddress) address;
      String id = local.getId();
      return Integer.parseInt(id.split(":")[1]);
    } else if (address instanceof InetSocketAddress) {
      InetSocketAddress socketAddr = (InetSocketAddress) address;
      return socketAddr.getPort();
    }
    throw new IllegalArgumentException("Unknown address type");
  }

  public SocketAddress getAddress(int ip, int port) {
    if (isLocal) {
      byte[] bytes = new byte[4];
      ByteUtils.int2beb(ip, bytes, 0);
      String ipStr = Arrays.toString(bytes).replace(", ", ".");
      return new LocalAddress(ipStr + ":" + port);
    } else {
      byte[] bytes = new byte[4];
      ByteUtils.int2beb(ip, bytes, 0);
      try {
        return new InetSocketAddress(InetAddress.getByAddress(bytes), port);
      } catch (UnknownHostException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
