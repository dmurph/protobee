package edu.cornell.jnutella.messages;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.util.GUID;

public class QueryHitBody implements MessageBody {
  
  private final boolean local;
  private byte numHits;
  private int port;
  private InetAddress address;
  private long speed;
  private List<ResponseBody> hitList = new ArrayList<ResponseBody>();
  private byte[] EQHD = new byte[4];
  private GGEP ggep;
  private GUID servantID;
  
  @AssistedInject
  public QueryHitBody(@Assisted byte numHits, @Assisted int port,
                  @Assisted InetAddress address, @Assisted long speed,
                  @Assisted List<ResponseBody> hitList, @Assisted byte[] EQHD,
                  @Nullable @Assisted GGEP ggep, @Assisted GUID servantID)  {
    this.numHits = numHits;
    this.port = port;
    this.address = address;
    this.speed = speed;
    this.hitList = hitList;
    this.EQHD = EQHD;
    this.ggep = ggep;
    this.servantID = servantID;
    this.local = false;
  }

  public byte getNumHits() {
    return numHits;
  }

  public int getPort() {
    return port;
  }

  public InetAddress getAddress() {
    return address;
  }

  public long getSpeed() {
    return speed;
  }

  public List<ResponseBody> getHitList() {
    return hitList;
  }

  public byte[] getEQHD() {
    return EQHD;
  }

  public GGEP getGgep() {
    return ggep;
  }

  public GUID getServantID() {
    return servantID;
  }
  
    

}
