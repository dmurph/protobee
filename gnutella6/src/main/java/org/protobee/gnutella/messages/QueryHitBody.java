package org.protobee.gnutella.messages;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.util.URN;
import org.protobee.gnutella.util.VendorCode;

import com.google.common.base.Preconditions;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


public class QueryHitBody implements MessageBody {

  public static final int XML_MAX_SIZE = 32768;

  private byte numHits;
  private InetSocketAddress socketAddress;
  private long speed;
  private ResponseBody[] hitList;
  private EQHDBody eqhd;
  private byte[] privateArea1;
  private GGEP ggep;
  private byte[] xmlBytes;
  private byte[] privateArea2;
  private byte[] servantID;

  public QueryHitBody(){ }
  
  // xmlBytes should be stored in GGEP - set to a constant for now
  @AssistedInject
  public QueryHitBody( @Assisted InetSocketAddress socketAddress, @Assisted long speed,
                       @Nullable @Assisted ResponseBody[] hitList, @Assisted VendorCode vendorCode, @Assisted("flags") byte flags, 
                       @Assisted("controls") byte controls, @Assisted("privateArea1") byte[] privateArea1, 
                       @Nullable @Assisted GGEP ggep,  @Assisted("xmlBytes") byte[] xmlBytes, 
                       @Assisted("privateArea2") byte[] privateArea2, @Assisted("servantID") byte[] servantID) {

    Preconditions.checkArgument((speed & 0xFFFFFFFF00000000l) == 0, "Speed "+speed+" exceeds maximum value");
    if (hitList == null){
      hitList = new ResponseBody[0];
    }
    Preconditions.checkArgument(hitList.length < 256, "Histlist has too many hits. numHits="+hitList.length+". Only 255 are allowed.");

    this.ggep = (ggep == null || ggep.isEmpty()) ? null : ggep;
    
    // if ggep and huge will print out zero bytes, put concatenate private areas 1 and 2
    if ( this.ggep == null  && privateArea2.length > 0 ){
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
    // TODO: xmlBytes is filler for now until ggep is settled
    this.xmlBytes = new byte[4]; 
    this.eqhd = new EQHDBody(vendorCode, (short) this.xmlBytes.length, flags, controls); // length guaranteed to be short due to xml_max_size
    this.servantID = servantID;

  }
  
  public static int getXmlMaxSize() {
    return XML_MAX_SIZE;
  }

  public byte getNumHits() {
    return numHits;
  }
  
  public InetSocketAddress getSocketAddress(){
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

  public byte[] getServantID() {
    return servantID;
  }
  
  public URN[] getUrns(){
    List<URN> urns = new ArrayList<URN>();
    for (ResponseBody repsonse : hitList){
      for (URN urn : repsonse.getHUGE().getUrns()){
        urns.add(urn);
      }
    } 
    return urns.toArray(new URN[urns.size()]);
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
    result = prime * result + Arrays.hashCode(servantID);
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
    if (!Arrays.equals(servantID, other.servantID)) return false;
    if (socketAddress == null) {
      if (other.socketAddress != null) return false;
    } else if (!socketAddress.equals(other.socketAddress)) return false;
    if (speed != other.speed) return false;
    if (!Arrays.equals(xmlBytes, other.xmlBytes)) return false;
    return true;
  }

}
