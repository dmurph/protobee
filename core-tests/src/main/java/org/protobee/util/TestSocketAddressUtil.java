package org.protobee.util;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import com.google.common.net.InetAddresses;

public class TestSocketAddressUtil {

  @Test
  public void testMatch() throws UnknownHostException {
    SocketAddressUtils util = new SocketAddressUtils(false);
    InetSocketAddress address = new InetSocketAddress(InetAddress.getByName("1.235.4.1"), 15);
    assertEquals(address, util.getAddress(util.getIPFromAddress(address), util.getPortFromAddress(address)));
  }
}
