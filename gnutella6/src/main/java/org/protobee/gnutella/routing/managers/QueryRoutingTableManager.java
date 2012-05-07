package org.protobee.gnutella.routing.managers;

import org.protobee.annotation.InjectLogger;
import org.protobee.gnutella.routing.IdentityHash;
import org.protobee.gnutella.routing.QueryGUIDRoutingPair;
import org.protobee.gnutella.routing.tables.QueryGUIDRoutingTable;
import org.protobee.gnutella.routing.tables.QueryGUIDRoutingTable.QueryEntry;
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

  @Override
  public boolean isRouted(byte[] messageGUID){
    log.error("Use isRoutedForQuerys().");
    throw new UnsupportedOperationException( "Use isRoutedForQuerys()." );
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
   * if no routing is available or the host is not anymore
   * connected. Adds result count to table.
   * 
   * @param guid the GUID of the query reply route to find.
   * @param resultCount the number of results routed together with the query reply of
   *        this query GUID.
   * @return the QueryGUIDRoutingPair that contains the host and routed result count to 
   *      route the reply or null.
   */

  public QueryGUIDRoutingPair findRoutingForQuerys( byte[] guid, int resultCount ) {

    QueryEntry entry = (QueryEntry) grtable.getCurrentMap().get( guid );
    if ( entry == null ) {
      entry = (QueryEntry) grtable.getLastMap().get( guid );
      if ( entry == null ) { return null; }
      else{ grtable.removefromLasttMap(guid); }
    }

    entry.augmentRoutedResultCount(resultCount);
    grtable.putInCurrentMap(guid, entry);

    // returns null if there is no host for the id anymore.
    NetworkIdentity identity = grtable.getIdToIdentityMap().get( entry.getHostId() );
    if (identity == null){ return null; }
    QueryGUIDRoutingPair qgrpair = new QueryGUIDRoutingPair(identity, entry.getRoutedResultCount());
    return qgrpair;

  }

  // puts guid in current table
  // returns true if is ready, false if routing must be added
  public boolean isRoutedForQuerys(byte[] messageGUID){
    checkForSwitch();

    boolean inCurrentTable = (grtable.getCurrentMap().get(messageGUID) != null);
    boolean inLastTable = (grtable.getLastMap().get(messageGUID) != null);

    if (inCurrentTable){
      grtable.removefromLasttMap(messageGUID);
      return true;
    }

    if (inLastTable){
      QueryEntry entry = (QueryEntry) grtable.getLastMap().get(messageGUID);
      grtable.removefromLasttMap(messageGUID);
      grtable.putInCurrentMap(messageGUID, entry);
      return true;
    }

    return false;

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
