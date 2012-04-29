package edu.cornell.jnutella.gnutella.routing.tables;

import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.gnutella.routing.IdentityHash;


public class QueryGUIDRoutingTable extends GUIDRoutingTable {
  
  protected ConcurrentHashMap<IdentityHash, Integer> uniqueQueryHitMap;
  
  public static interface Factory {
    QueryGUIDRoutingTable create(long lifetime);
  }
  
  /**
   * @param lifetime the lifetime in millis of a map. After this time is passed
   * the lastMap will be replaced by the currentMap.
   */
  @AssistedInject
  public QueryGUIDRoutingTable( @Assisted long lifetime ) {
    super(lifetime);
  }
  
  public Integer putInUniqueQueryHitMap(IdentityHash hash, Integer i){
    return uniqueQueryHitMap.put(hash, i);
  }
  
  public boolean hasQueryHit(IdentityHash hash){
    return uniqueQueryHitMap.containsKey(hash);
  }
  
  public static class QueryEntry extends Entry {
    protected int routedResultCount;
    
    public QueryEntry(){ }
    
    public QueryEntry(Integer hostId) {
      super(hostId);
      this.routedResultCount = 0;
    }

    public int getRoutedResultCount(){
      return this.routedResultCount;
    }
    
    public void augmentRoutedResultCount(int routedResultCount){
      this.routedResultCount += routedResultCount;
    }
  }
}

