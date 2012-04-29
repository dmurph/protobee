package org.protobee.gnutella.routing.managers;

import java.util.concurrent.ConcurrentMap;

import org.protobee.gnutella.routing.tables.GUIDRoutingTable;
import org.protobee.gnutella.routing.tables.GUIDRoutingTable.Entry;
import org.protobee.identity.NetworkIdentity;

import com.google.inject.Inject;


public abstract class GUIDRoutingTableManager {
  protected GUIDRoutingTable grtable;
  
  @Inject
  public GUIDRoutingTableManager(GUIDRoutingTable grtable) {
    this.grtable = grtable;
  }

  /**
   * Adds a routing to the routing table.
   * @param guid the GUID to route for
   * @param host the route destination.
   * @precondition guid is not in table (isRouted returned false)
   */
  public void addRouting( byte[] messageGuid, NetworkIdentity identity ) {
    checkForSwitch();
    grtable.putInCurrentMap(messageGuid, createNewEntry(getIdForHost( identity )));
  }

  // keeps guid in current table
  // returns true if is ready, false if routing must be added
  public boolean isRouted(byte[] messageGUID){

    boolean inCurrentTable = (grtable.getCurrentMap().get(messageGUID) != null);
    boolean inLastTable = (grtable.getLastMap().get(messageGUID) != null);

    if (inCurrentTable){
      grtable.removefromLasttMap(messageGUID);
      return true;
    }

    if (inLastTable){
      Entry entry = grtable.getLastMap().get(messageGUID);
      grtable.removefromLasttMap(messageGUID);
      grtable.putInCurrentMap(messageGUID, entry);
      return true;
    }

    return false;

  }

  /**
   * Tries to find the reply route for the given GUID.
   * @param guid the GUID for the reply route to find.
   * @return the Host to route the reply for.
   */
  public NetworkIdentity findRouting( byte[] guid ) {
    Entry entry = grtable.getCurrentMap().get( guid );
    if ( entry == null ) {
      entry = grtable.getLastMap().get( guid );
    }
    if ( entry != null ) {
      // returns null if there is no host for the id anymore.
      return grtable.getIdToIdentityMap().get( entry.getHostId() );
    }
    else {
      return null;
    }
  }

  /**
   * Removes the host from the id mapping.
   * @param host the host to remove.
   */
  public void removeHost( NetworkIdentity identity ) {
    Integer id = grtable.getIdentityToIdMap().get(identity);
    if ( id != null ) {
      grtable.removefromIdtoIdentityMap(id);
      grtable.removefromIdentitytoIdMap(identity);
    }
  }

  /**
   * Check to delete old entries. If the lifetime has passed.
   */
  protected void checkForSwitch() {
    long currentTime = System.currentTimeMillis();
    // check if enough time has passed or the map size reached the max.
    if ( currentTime < grtable.getNextReplaceTime() && grtable.getCurrentMap().size() < grtable.MAX_ROUTE_TABLE_SIZE ) {
      return;
    }

    ConcurrentMap<byte[], Entry> temp = grtable.getLastMap();
    grtable.setLastMap(grtable.getCurrentMap());
    grtable.setCurrentMap(temp);
    grtable.setNextReplaceTime(currentTime+grtable.getLifetime());
    return;
  }


  /**
   * Returns the id for the host if there is a existing one already or creates
   * a new id otherwise.
   * @param host the host to get the id for.
   * @return the id of the host.
   */
  protected Integer getIdForHost( NetworkIdentity identity ) {

    Integer id = grtable.getIdentityToIdMap().get(identity); // host = wrong
    if (id != null){
      return id;
    }
    
    else{ // find free id
      id = Integer.valueOf( grtable.getNextId() + 1 );
      while ( grtable.getIdToIdentityMap().get( id ) != null ) {
        id = Integer.valueOf( grtable.getNextId() + 1 );
      }
    }
    grtable.putInIdToIdentitytMap(id, identity);
    grtable.putInIdentityToIdMap(identity, id);
    return id; 
  }

  protected Entry createNewEntry() {
    return new Entry();
  }
  
  protected Entry createNewEntry(Integer idForHost) {
    return new Entry(idForHost);
  }

}
