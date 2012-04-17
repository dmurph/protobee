package edu.cornell.jnutella.gnutella.messages;

import java.net.InetAddress;
import java.util.Arrays;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.util.GUID;
import edu.cornell.jnutella.util.JnutellaSocketAddress;
import edu.cornell.jnutella.util.VendorCode;

public class QueryHitBody implements MessageBody {

  public static final int XML_MAX_SIZE = 32768;

  private byte numHits;
  private JnutellaSocketAddress socketAddress;
  private long speed;
  private ResponseBody[] hitList;
  private EQHDBody eqhd;
  private byte[] privateArea1;
  private GGEP ggep;
  private byte[] xmlBytes;
  private byte[] privateArea2;
  private GUID servantID;

  // xmlBytes should be stored in GGEP - set to a constant for now
  @AssistedInject
  public QueryHitBody( @Assisted JnutellaSocketAddress socketAddress, @Assisted long speed,
                       @Nullable @Assisted ResponseBody[] hitList, @Assisted VendorCode vendorCode, @Assisted("flags") byte flags, 
                       @Assisted("controls") byte controls, @Assisted("privateArea1") byte[] privateArea1, 
                       @Nullable @Assisted GGEP ggep, @Assisted("xmlBytes") byte[] xmlBytes, 
                       @Assisted("privateArea2") byte[] privateArea2, @Assisted GUID servantID) {

    Preconditions.checkArgument((speed & 0xFFFFFFFF00000000l) == 0);
    if (hitList == null){
      hitList = new ResponseBody[0];
    }
    Preconditions.checkArgument(hitList.length < 256);

    // if ggep will print out zero bytes, put concatenate private areas 1 and 2
    if ((ggep == null || ggep.getHeaders().size() == 0 ) && privateArea2.length > 0){
      this.privateArea1 = new byte[privateArea1.length + privateArea2.length];
      System.arraycopy(privateArea1, 0, this.privateArea1, 0, privateArea1.length);
      System.arraycopy(privateArea2, 0, this.privateArea1, privateArea1.length, privateArea2.length);
      this.privateArea2 = new byte[0];
    }
    else{
      this.privateArea1 = privateArea1;
      this.privateArea2 = privateArea2;
    }
    
    this.numHits = (byte) hitList.length;
    this.socketAddress = socketAddress;
    this.speed = speed;
    this.hitList = hitList;
    this.xmlBytes = Arrays.copyOfRange(xmlBytes, 0, XML_MAX_SIZE-1); // truncate xml if required
    this.xmlBytes = new byte[4]; // filler for now until ggep is settled
    this.eqhd = new EQHDBody(vendorCode, (short) this.xmlBytes.length, flags, controls); // length guaranteed to be short due to xml_max_size
    this.ggep = ggep;
    this.servantID = servantID;

  }

  public static int getXmlMaxSize() {
    return XML_MAX_SIZE;
  }

  public byte getNumHits() {
    return numHits;
  }
  
  public JnutellaSocketAddress getSocketAddress(){
    return socketAddress;
  }

  public int getPort() {
    return socketAddress.getPort();
  }

  public InetAddress getAddress() {
    return socketAddress.getAddress();
  }

  public long getSpeed() {
    return speed;
  }

  public ResponseBody[] getHitList() {
    return hitList;
  }

  public EQHDBody getEqhd() {
    return eqhd;
  }

  public byte[] getPrivateArea1() {
    return privateArea1;
  }

  public GGEP getGgep() {
    return ggep;
  }

  public byte[] getXmlBytes() {
    return xmlBytes;
  }

  public byte[] getPrivateArea2() {
    return privateArea2;
  }

  public GUID getServantID() {
    return servantID;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((eqhd == null) ? 0 : eqhd.hashCode());
    result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
    result = prime * result + Arrays.hashCode(hitList);
    result = prime * result + numHits;
    result = prime * result + Arrays.hashCode(privateArea1);
    result = prime * result + Arrays.hashCode(privateArea2);
    result = prime * result + ((servantID == null) ? 0 : servantID.hashCode());
    result = prime * result + ((socketAddress == null) ? 0 : socketAddress.hashCode());
    result = prime * result + (int) (speed ^ (speed >>> 32));
    result = prime * result + Arrays.hashCode(xmlBytes);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    QueryHitBody other = (QueryHitBody) obj;
    if (eqhd == null) {
      if (other.eqhd != null) return false;
    } else if (!eqhd.equals(other.eqhd)) return false;
    if (ggep == null) {
      if (other.ggep != null) return false;
    } else if (!ggep.equals(other.ggep)) return false;
    if (!Arrays.equals(hitList, other.hitList)) return false;
    if (numHits != other.numHits) return false;
    if (!Arrays.equals(privateArea1, other.privateArea1)) return false;
    if (!Arrays.equals(privateArea2, other.privateArea2)) return false;
    if (servantID == null) {
      if (other.servantID != null) return false;
    } else if (!servantID.equals(other.servantID)) return false;
    if (socketAddress == null) {
      if (other.socketAddress != null) return false;
    } else if (!socketAddress.equals(other.socketAddress)) return false;
    if (speed != other.speed) return false;
    if (!Arrays.equals(xmlBytes, other.xmlBytes)) return false;
    return true;
  }

}
