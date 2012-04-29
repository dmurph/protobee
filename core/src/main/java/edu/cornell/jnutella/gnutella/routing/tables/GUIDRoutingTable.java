package edu.cornell.jnutella.gnutella.routing.tables;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.identity.NetworkIdentity;

/**
 * This GUIDRoutingTable is used to route replies coming from the GNet
 * back to the requester. This is done by using the GUID of the reply
 * which matches the GUID of the request to identify the correct 
 * route back to the requester.
 */
public class GUIDRoutingTable {
  /**
   * The max number of entries in a single route table. This could lead to have
   * a total of 2 * MAX_ROUTE_TABLE_SIZE entries in total.
   */
  public final int MAX_ROUTE_TABLE_SIZE = 50000;

  /**
   * Maps a GUID to a integer that represents a id. The id can be used to
   * retrieve the host from the idToHostMap. This extra layer is used to solve
   * three problems:<br>
   * - When deleting a host because of disconnection we can delete its entry very
   *   fast and performant from the hostToIdMap and idToHostMap without the
   *   need to iterate over the GUID mappings.<br>
   * - We are still able to identify duplicate query GUIDs even though the host
   *   has already disconnected.<br>
   * - We are able to freeing up the Host object for garbage collections, since
   *   we are not holding it in the GUID mappings.<br>
   * <br>
   * To implement some kind of FIFO behavior for the GUID mappings we are using
   * to sets, currentMap and lastMap. After a certain time passed by we replace
   * the lastMap with the currentMap and create a new fresh currentMap. This
   * allows us to accomplish a very fast and efficient FIFO behavior that stores
   * at least n to 2n seconds of GUID mappings.
   */
  protected ConcurrentMap<byte[], Entry> currentMap ;
  protected ConcurrentMap<byte[], Entry> lastMap;

  protected ConcurrentMap<Integer, NetworkIdentity> idToIdentityMap;
  protected ConcurrentMap<NetworkIdentity, Integer> identityToIdMap;

  private long lifetime;            /** The lifetime of a map in millis */
  private long nextReplaceTime;     /** The time when the lastMap will be replaced by the currentMap. */
  private int nextId;               /** The next id to return for a host. */

  public static interface Factory {
    GUIDRoutingTable create(long lifetime);
  }
  
  /**
   * The routing table will store at least lifetime to 2 * lifetime of GUID mappings.
   * @param lifetime the lifetime in millis of a map. After this time is passed
   * the lastMap will be replaced by the currentMap.
   */ 
  @AssistedInject
  public GUIDRoutingTable( @Assisted long lifetime ) {
    this.lifetime = lifetime;
    nextId = 0;
    
    // TODO making it a guice constant w/ an annotation 
    currentMap = (new MapMaker().concurrencyLevel(4).makeMap());
    lastMap = (new MapMaker().concurrencyLevel(4).makeMap());
    idToIdentityMap = (new MapMaker().concurrencyLevel(4).makeMap());
    identityToIdMap = (new MapMaker().concurrencyLevel(4).makeMap());
    
  }

  public ConcurrentMap<byte[], Entry> getCurrentMap() {
    return currentMap;
  }

  public ConcurrentMap<byte[], Entry> getLastMap() {
    return lastMap;
  }

  public ConcurrentMap<Integer, NetworkIdentity> getIdToIdentityMap() {
    return idToIdentityMap;
  }

  public ConcurrentMap<NetworkIdentity, Integer> getIdentityToIdMap() {
    return identityToIdMap;
  }

  public long getLifetime() {
    return lifetime;
  }

  public long getNextReplaceTime() {
    return nextReplaceTime;
  }

  public int getNextId() {
    return nextId;
  }

  public void setCurrentMap(ConcurrentMap<byte[], Entry> currentMap) {
    if (lastMap == null){
      return;
    }
    this.currentMap = currentMap;
  }

  public void setLastMap(ConcurrentMap<byte[], Entry> lastMap) {
    if (lastMap == null){
      return;
    }
    this.lastMap = lastMap;
  }

  public void setIdToIdentityMap(ConcurrentMap<Integer, NetworkIdentity> idToIdentityMap) {
    if (lastMap == null){
      return;
    }
    this.idToIdentityMap = idToIdentityMap;
  }

  public void setIdentityToIdMap(ConcurrentMap<NetworkIdentity, Integer> identityToIdMap) {
    if (lastMap == null){
      return;
    }
    this.identityToIdMap = identityToIdMap;
  }

  public void setLifetime(long lifetime) {
    this.lifetime = lifetime;
  }

  public void setNextReplaceTime(long nextReplaceTime) {
    this.nextReplaceTime = nextReplaceTime;
  }

  public void setNextId(int nextId) {
    this.nextId = nextId;
  }

  // alter maps
  public Entry removefromCurrentMap(byte[] guid){
    return this.currentMap.remove(guid);
  }

  public Entry removefromLasttMap(byte[] guid){
    return this.lastMap.remove(guid);
  }

  public Integer removefromIdentitytoIdMap(NetworkIdentity id){
    return this.identityToIdMap.remove(id);
  }

  public NetworkIdentity removefromIdtoIdentityMap(Integer id){
    return this.idToIdentityMap.remove(id);
  }

  public void putInCurrentMap(byte[] guid, Entry entry){
    this.currentMap.put(guid, entry);
  }

  public void putInLasttMap(byte[] guid, Entry entry){
    this.lastMap.put(guid, entry);
  }

  public void putInIdToIdentitytMap(Integer id, NetworkIdentity identity){
    this.idToIdentityMap.put(id, identity);
  }

  public void putInIdentityToIdMap(NetworkIdentity identity, Integer id){
    this.identityToIdMap.put(identity, id);
  }

  public static class Entry {
    protected Integer hostId;
    
    public Entry(){ }
    
    public Entry(Integer hostId){
      this.hostId = hostId;
    }

    public Integer getHostId(){
      return hostId;
    }

    public void setHostId(Integer hostId){
      this.hostId = hostId;
    }
  }
}