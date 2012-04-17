package edu.cornell.jnutella.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;

public class JnutellaSocketAddress  {

  InetSocketAddress address;

  public JnutellaSocketAddress(byte[] addr, int port) throws DecodingException {

    int a = ByteUtils.ubyte2int(addr[0]);
    int b = ByteUtils.ubyte2int(addr[1]);
    int c = ByteUtils.ubyte2int(addr[2]);
    int d = ByteUtils.ubyte2int(addr[3]);
    String ip = (a + "." + b + "." + c + "." + d);
    
    InetAddress address;

    try {
      address = InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      throw new DecodingException("Host " + ip + " is unknown.");
    }

    this.address = new InetSocketAddress(address, port);

  }
  
  public JnutellaSocketAddress(InetAddress address, int port){
    this.address = new InetSocketAddress(address, port);
  }
  
  public InetAddress getAddress(){
    return address.getAddress();
  }
  
  public int getPort(){
    return address.getPort();
  }
  
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    JnutellaSocketAddress other = (JnutellaSocketAddress) obj;
    if (address == null) {
      if (other.address != null) return false;
    } else if (!address.equals(other.address)) return false;
    return true;
  }


}  
