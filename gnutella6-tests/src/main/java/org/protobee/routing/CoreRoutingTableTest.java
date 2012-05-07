package org.protobee.routing;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.junit.Test;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.messages.MessageBodyFactory;
import org.protobee.gnutella.messages.QueryBody;
import org.protobee.gnutella.messages.TestUtils;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.managers.CoreRoutingTableManager;
import org.protobee.gnutella.routing.message.PatchBody;
import org.protobee.gnutella.routing.message.ResetBody;
import org.protobee.gnutella.routing.tables.CoreRoutingTable;

public class CoreRoutingTableTest extends AbstractGnutellaTest {

  @Test
  public void testAddAndGetQueries() throws DecodingException{
    String [] queries = {"kjhsdfkjs", "f"};
    for (String query : queries){
      CoreRoutingTableManager manager = injector.getInstance(CoreRoutingTableManager.class);

      MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);
      QueryBody queryBody1 = factory.createQueryMessage( (short) (Integer.MAX_VALUE + 1), query, TestUtils.getHUGE(), null);
      QueryBody queryBody2 = factory.createQueryMessage( (short) (Integer.MAX_VALUE + 1), "kj435sdfkjs", TestUtils.getHUGE(), null);

      manager.add(queryBody1);
      assertEquals(manager.containsQuery(queryBody1), true);
      assertEquals(manager.containsQuery(queryBody2), false);

      manager.add(queryBody2);
      assertEquals(manager.containsQuery(queryBody2), true);

      manager.clear();
    }
  }

  @Test
  public void testAggregateToRouteTable() throws DecodingException{

    CoreRoutingTableManager manager = injector.getInstance(CoreRoutingTableManager.class);

    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);    
    QueryBody queryBody1 = factory.createQueryMessage( (short) (Integer.MAX_VALUE + 1), "AAA", TestUtils.getHUGE(), null);
    QueryBody queryBody2 = factory.createQueryMessage( (short) (Integer.MAX_VALUE + 1), "BBB", TestUtils.getHUGE(), null);

    manager.add(queryBody1);
    CoreRoutingTable tbl = manager.getCoreRoutingTable();
    manager.clear();    
    manager.add(queryBody2);
    manager.aggregateToRouteTable(tbl);

    assertEquals(manager.containsQuery(queryBody1), true);
    assertEquals(manager.containsQuery(queryBody2), true);

    manager.clear();

  }

  @Test
  public void testAddPatch() {

    CoreRoutingTableManager manager = injector.getInstance(CoreRoutingTableManager.class);

    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);
    QueryBody queryBody1 = factory.createQueryMessage( (short) (Integer.MAX_VALUE + 1), "jhvjcrt", TestUtils.getHUGE(), null);
    manager.add(queryBody1);
    
    byte[] inflatedData = {(byte) 0x0, (byte) 0x1, (byte) 0x2, (byte) 0x3};

    PatchBody patch = factory.createPatchMessage((byte) 4, (byte) 6, PatchBody.COMPRESSOR_ZLIB, (byte) 4, deflate(inflatedData));
    
    try {
      manager.update(patch);
    } catch (InvalidMessageException e) {
      assertEquals(false, true);
    }

    manager.clear();

  }
  
  @Test
  public void testReset() {

    CoreRoutingTableManager manager = injector.getInstance(CoreRoutingTableManager.class);

    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);
    QueryBody queryBody1 = factory.createQueryMessage( (short) (Integer.MAX_VALUE + 1), "jhvjcrt", TestUtils.getHUGE(), null);
    manager.add(queryBody1);
    assertEquals(manager.containsQuery(queryBody1), true);

    
    ResetBody reset = factory.createResetMessage(100l, manager.getCoreRoutingTable().getInfinity());
    
    try {
      manager.update(reset);
    } catch (InvalidMessageException e) {
      assertEquals(false, true);
    }
    
    assertEquals(manager.containsQuery(queryBody1), false);

    manager.clear();

  }
  
  
  
  public byte[] deflate(byte[] data) {
    OutputStream dos = null;
    Deflater def = null;
    try {
      def = new Deflater();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      dos = new DeflaterOutputStream(baos, def);
      dos.write(data, 0, data.length);
      dos.close(); // flushes bytes
      return baos.toByteArray();
    } catch (IOException impossible) {
      throw new RuntimeException(impossible);
    } finally {
      if (dos != null) {
        try {
          dos.close();
        } catch (IOException e) {}
      }
      if (def != null) {
        def.end();
      }
    }
  }


}
