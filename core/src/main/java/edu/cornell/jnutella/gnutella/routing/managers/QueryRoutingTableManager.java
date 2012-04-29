package edu.cornell.jnutella.gnutella.routing.managers;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.gnutella.routing.IdentityHash;
import edu.cornell.jnutella.gnutella.routing.QueryGUIDRoutingPair;
import edu.cornell.jnutella.gnutella.routing.tables.QueryGUIDRoutingTable;
import edu.cornell.jnutella.gnutella.routing.tables.QueryGUIDRoutingTable.QueryEntry;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.util.GUID;

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

  @Override
  protected QueryEntry createNewEntry() {
    return new QueryEntry();
  }

  @Override
  protected QueryEntry createNewEntry(Integer idForHost) {
    return new QueryEntry(idForHost);
  }
}
