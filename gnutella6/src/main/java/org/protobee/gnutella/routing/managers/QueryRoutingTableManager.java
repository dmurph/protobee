package org.protobee.gnutella.routing.managers;

import org.protobee.annotation.InjectLogger;
import org.protobee.gnutella.routing.IdentityHash;
import org.protobee.gnutella.routing.QueryGUIDRoutingPair;
import org.protobee.gnutella.routing.tables.QueryGUIDRoutingTable;
import org.protobee.gnutella.routing.tables.QueryGUIDRoutingTable.QueryEntry;
import org.protobee.gnutella.util.GUID;
import org.protobee.identity.NetworkIdentity;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class QueryRoutingTableManager extends GUIDRoutingTableManager {

  // holds from 5-10 minutes of query GUIDs
  private static long DEFAULT_LIFETIME = 5 * 60 * 1000;

  @InjectLogger
  private Logger log;

  @Inject
  public QueryRoutingTableManager(QueryGUIDRoutingTable.Factory factory) {
    super(factory.create(DEFAULT_LIFETIME));
  }

  @Override
  public NetworkIdentity findRouting( byte[] guid ) {
    log.error("Use findRoutingForQuerys().");
    throw new UnsupportedOperationException( "Use findRoutingForQuerys()." );
  }

  /**
   * return true if not double 
   * return false if identity hash is already in table
   */
  public boolean hasQueryHit(IdentityHash hash){
    return ((QueryGUIDRoutingTable) this.grtable).hasQueryHit(hash);
  }

  public void addQueryHit(IdentityHash hash){
    ((QueryGUIDRoutingTable) this.grtable).putInUniqueQueryHitMap(hash, 1);
  }

  /**
   * Returns the query routing pair with host for the given GUID or null
   * if no push routing is available or the host is not anymore
   * connected.
   * 
   * @param guid the GUID of the query reply route to find.
   * @param resultCount the number of results routed together with the query reply of
   *        this query GUID.
   * @return the QueryGUIDRoutingPair that contains the host and routed result count to 
   *      route the reply or null.
   */

  public QueryGUIDRoutingPair findRoutingForQuerys( GUID guid, int resultCount ) {

    QueryEntry entry = (QueryEntry) ((QueryGUIDRoutingTable) this.grtable).getCurrentMap().get(guid);
    entry = (entry == null) ? (QueryEntry) ((QueryGUIDRoutingTable) this.grtable).getLastMap().get(guid) : entry;

    if (entry == null){
      return null;
    }

    NetworkIdentity host = ((QueryGUIDRoutingTable) this.grtable).getIdToIdentityMap().get(entry.getHostId());
    if ( host == null ) {
      return null;
    }
    QueryGUIDRoutingPair returnPair = new QueryGUIDRoutingPair( host, entry.getRoutedResultCount());
    // raise entries routed result count
    entry.augmentRoutedResultCount(resultCount);
    return returnPair;

  }
  
  public boolean hasRoutingForQuerys( GUID guid, int resultCount ) {

    QueryEntry entry = (QueryEntry) ((QueryGUIDRoutingTable) this.grtable).getCurrentMap().get(guid);
    entry = (entry == null) ? (QueryEntry) ((QueryGUIDRoutingTable) this.grtable).getLastMap().get(guid) : entry;

    if (entry == null){
      return false;
    }

    NetworkIdentity host = ((QueryGUIDRoutingTable) this.grtable).getIdToIdentityMap().get(entry.getHostId());
    if ( host == null ) {
      return false;
    }
    
    return true;

  }

  @Override
  protected QueryEntry createNewEntry() {
    return new QueryEntry();
  }

  @Override
  protected QueryEntry createNewEntry(Integer idForHost) {
    return new QueryEntry(idForHost);
  }
}
