package org.protobee.gnutella.routing.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.protobee.annotation.InjectLogger;
import org.protobee.gnutella.messages.QueryBody;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.message.PatchBody;
import org.protobee.gnutella.routing.message.ResetBody;
import org.protobee.gnutella.routing.message.RoutingBody;
import org.protobee.gnutella.routing.tables.CoreRoutingTable;
import org.protobee.gnutella.util.URN;
import org.protobee.util.IOUtils;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class CoreRoutingTableManager {

  @InjectLogger
  private Logger log;
  private final IOUtils ioUtils;
  private CoreRoutingTable qrtable;
  
  @Inject
  public CoreRoutingTableManager(IOUtils ioUtils, CoreRoutingTable.Factory factory) {
    this.ioUtils = ioUtils;
    this.qrtable = factory.create();
  }

  public void update(RoutingBody message) throws InvalidMessageException {
    if ( message.getVariant() == RoutingBody.RESET_TABLE_VARIANT ) {
      qrtable = new CoreRoutingTable((int) ((ResetBody) message).getTableLength());
    }
    else if ( message.getVariant() == RoutingBody.PATCH_TABLE_VARIANT ) {
      PatchBody patchMessage = (PatchBody) message;
      byte compressor = patchMessage.getCompressor();
      byte[] patchData = patchMessage.getData();
      byte entryBits = patchMessage.getEntryBits();

      // checks
      Preconditions.checkArgument(compressor == PatchBody.COMPRESSOR_ZLIB
          || compressor == PatchBody.COMPRESSOR_NONE, 
          new InvalidMessageException("compressor is not a known compressor: "+compressor));
      Preconditions.checkArgument(entryBits != 8, 
          new InvalidMessageException("entry bits cannot equal 8: "+entryBits));

      // Validate message info...
      byte msgSequenceSize = patchMessage.getSequenceSize();
      byte msgSequenceNumber = patchMessage.getSequenceNum();

      // initializing case...
      if ( qrtable.getSequenceSize() == 0 && qrtable.getSequenceNumber() == 0 ) {
        qrtable.setSequenceSize(msgSequenceSize);
        qrtable.setSequenceNumber((byte) 0x1);
      }

      Preconditions.checkArgument(qrtable.getSequenceSize() != msgSequenceSize 
          || msgSequenceNumber != qrtable.getSequenceNumber() || msgSequenceSize == 0);

      qrtable.setSequenceNumber(msgSequenceNumber);

      if ( compressor == PatchBody.COMPRESSOR_ZLIB ) {
        try {
          patchData = ioUtils.inflate( patchData );
        } catch (IOException e) {
          throw new InvalidMessageException("Can't inflate patch data" );
        }
        Preconditions.checkArgument(patchData != null, 
            new InvalidMessageException("patchData cannot be null: "+patchData));
      }

      // expand patch data.. this consumes extra memory... can't we do
      // it on the fly?
      if ( entryBits == 4 ) {
        byte[] buf = new byte[ patchData.length * 2 ];
        byte tmpVal;
        for ( int i = 0; i < patchData.length; i++ ) {
          buf[ i * 2 ] = (byte)(patchData[i]>>4);
          tmpVal = (byte)(patchData[i] & 0x0F);
          if ( (tmpVal & 0x08) != 0 ) {
            tmpVal = (byte)(0xF0 | tmpVal);
          }
          buf[ i * 2 + 1 ] = tmpVal;
        }
        patchData = buf;
      }

      try {
        // used to determine if a new entry was set
        boolean prevBitSet, currBitSet;
        int loggedInvalidPatchFieldValue = 0;
        for ( int i = 0; i < patchData.length; i++ ) {
          prevBitSet = qrtable.get( qrtable.getPatchPosition() );

          //if ( patchData[i] == 1 - infinity )
          if ( patchData[i] < 0 ) { // use this to also accept invalid clients
            qrtable.set( qrtable.getPatchPosition() );
          }
          else if ( patchData[i] > 0 ){ //else if ( patchData[i] == infinity - 1 ) // use this to also accept invalid clients
            qrtable.clear( qrtable.getPatchPosition() );
          }
          else if ( patchData[i] != 0 )
          {// we received a QRT with a patch data value that is not
            // really in range...
            // we like to log each value only once therefore we
            // flag the logged value
            if ( loggedInvalidPatchFieldValue == 0 ||
                loggedInvalidPatchFieldValue != patchData[i] )
            {
              log.warn("Received invalid PatchData field value: " + patchData[i]);
              loggedInvalidPatchFieldValue = patchData[i];
            }
          }
          currBitSet = qrtable.get( qrtable.getPatchPosition() );
          if ( prevBitSet && !currBitSet ) {
            qrtable.decrementEntryCount();
          }
          else if ( !prevBitSet && currBitSet ) {
            qrtable.augmentEntryCount();
          }
          qrtable.augmentPatchPosition();
        }
      }
      catch ( IndexOutOfBoundsException exp ) {
        throw new InvalidMessageException("QRTPatchMsg Wrong patch message data size." );
      }

      if ( qrtable.getSequenceNumber() == qrtable.getSequenceSize() ) {
        qrtable.setSequenceSize((byte) 0x0);
        qrtable.setSequenceNumber((byte) 0x0);
        qrtable.setPatchPosition((byte) 0x0);
        log.debug( "Updated QRT: " + qrtable.getEntryCount() + " / " + qrtable.getTableSize() );
      }
      else {
        qrtable.augmentSequenceNumber();
      }
    }
  }

  public void aggregateToRouteTable( CoreRoutingTable queryRoutingTable ) {
    BitSet bitSetToAggregate;
    if ( qrtable.getTableSize() != queryRoutingTable.getTableSize() ) {
      bitSetToAggregate = resizeRouteTable( queryRoutingTable, (int) qrtable.getTableSize() );
    }
    else {
      bitSetToAggregate = queryRoutingTable.getQrTable();
    }
    if ( bitSetToAggregate == null ) {// try to find out what went wrong here...
      log.error( "Aggregate source is null: {}/{}",
        qrtable.getTableSize(), queryRoutingTable.getTableSize() );
      return;
    }

    qrtable.or(bitSetToAggregate);
  }

  private BitSet resizeRouteTable( CoreRoutingTable qrt, int newSize ) {
    if ( qrt.getTableSize() == newSize ) {
      return qrt.getQrTable();
    }

    if ( qrt.getResizedQRTable() != null && qrt.getResizedQRTable().size() == newSize ) {
      return qrt.getResizedQRTable();
    }

    BitSet tempQRTable = new BitSet( newSize );
    // create the resized version by using a simple algorithm.
    double factor = ( (double) newSize ) / ( (double) qrt.getTableSize() );

    for ( int i = 0; i < qrt.getTableSize(); i++ ) {
      if ( !qrt.getQrTable().get( i ) ) {// bit not set... don't need to handle...
        continue;
      }

      int from = (int) Math.floor( i * factor );
      int to = (int) Math.ceil( (i+1) * factor );

      for (int j = from; j < to; j++) {
        tempQRTable.set( j );
      }
    }
    qrt.setResizedQRTable(tempQRTable);
    return tempQRTable;
  }
  
  /**
   * Fills the given QueryRoutingTable with the last received 
   * QueryRoutingTables of connected leafs, when the servent is an ultrapeer.
   * @param qrTable the QueryRoutingTable to fill.
   * @param servent the Servent to act for.
   */
//  // if ultrapeer
//  public void fillQRTWithLeaves( NetworkIdentity[] leaves ) {
//    // add QRT of leafs...
//    // leaves = servent.getHostService().getNetworkHostsContainer().getLeafConnections();
//    QueryRoutingTable hostQRT;
//    for (NetworkIdentity leaf: leaves){
//      if (leaf.getHopsFlowLimit() < 3){
//        continue;
//      }
//      hostQRT = leaf.getLastReceivedRoutingTable();
//      if ( hostQRT != null ) {
//        aggregateToRouteTable( hostQRT );
//      }
//    }
//  }
  
  /**
   * Creates the current local QueryRoutingTable that is used to update
   * remote clients.
   */
//  public QueryRoutingTable createLocalQueryRoutingTable( List<ShareFile> sharedFiles ) {
//    long start = System.currentTimeMillis();
//    HashSet<String> wordSet = new HashSet<String>();
//    for ( ShareFile file : sharedFiles ) {
//      // add urn:sha1:xxx
//      //URN urn = sharedFiles[i].getURN();
//      //if ( urn != null )
//      //{
//      //    wordSet.add( urn.getAsString() );
//      //}
//
//      // add splitted words.
//      String[] words = splitFilePath( file.getSystemFile().getAbsolutePath() );
//      for ( int j = 0; j < words.length; j++ ) {
//        wordSet.add( words[ j ] );
//      }
//    }
//
//    QueryRoutingTable qrTable = new QueryRoutingTable( QueryRoutingTable.MIN_TABLE_SIZE );
//    while( true ) {
//      fillLocalQRTWithShare( qrTable, wordSet );
//      if ( qrTable.getTableSize() < QueryRoutingTable.MAX_TABLE_SIZE && qrTable.getFillRatio() > QueryRoutingTable.MAX_FILL_RATIO ) {
//        qrTable = new QueryRoutingTable((int) qrTable.getTableSize() * 2 );
//        continue;
//      }
//      break;
//    }
//
//    long end = System.currentTimeMillis();
//    return qrTable;
//  }

  private void fillLocalQRTWithShare( CoreRoutingTable qrTable, HashSet<String> wordSet ) {
    int counter = 0;
    for ( String word : wordSet ) {
      addWord( word );
      counter ++;
      // check if the table is already full
      if ( counter%1000 == 0 && qrTable.getTableSize() < CoreRoutingTable.MAX_TABLE_SIZE 
          && qrTable.getFillRatio() > CoreRoutingTable.MAX_FILL_RATIO ) {
        return;
      }
    }
  }

  /**
   * Adds a character sequence to the qrt. This sequence is split into words
   * each word is added separately into the qrt.
   * @param absoluteFilePath the character sequence to add.
   */
  private void add( String absoluteFilePath ) {
    String[] words = splitFilePath( absoluteFilePath );
    for (String word : words){
      addWord(word);
    }
  }


  /**
   * Adds a single word into the qrt the word is hashed without modification.
   * @param singleWord the word to add to the qrt without modification.
   */
  private void addWord( String singleWord ) {
    int hashVal = qrpHash( singleWord, 0, singleWord.length(), qrtable.getTableBits() );
    if ( !qrtable.get( hashVal ) ) {
      qrtable.augmentEntryCount();

      qrtable.set( hashVal );
    }
  }

  private String[] splitFilePath( String filePath ){
    StringTokenizer tokenizer = new StringTokenizer( filePath, CoreRoutingTable.FILE_DELIMITERS );
    ArrayList<String> list = new ArrayList<String>( 20 );
    while( tokenizer.hasMoreTokens() ) {
      String word = tokenizer.nextToken();
      list.add( word );

      // generate prefix strings
      int length = word.length();
      for ( int i = 1; i < 5 && (length - i) > 5; i++ ) {
        list.add( word.substring( 0, length - i ) );
      }
    }
    String[] strArr = new String[ list.size() ];
    list.toArray( strArr );
    return strArr;
  }

  /**
   * Checks if the QRT has the hash value of the query in the queryBody.
   * @param queryBody
   * @return true if the hash of the query string is flagged.
   */
  public boolean containsQuery( QueryBody queryBody ) {
    URN[] urns = queryBody.getHuge().getUrns();
    if (queryBody.hasInvalidQuery()){
      for (URN urn: urns){
        String urnString = urn.getUrnString();
        int hashVal = qrpHash( urnString, 0, urnString.length(), qrtable.getTableBits() );
        if( qrtable.get( hashVal ) ) return true;
      }
      return false;
    }
    String[] words = splitQueryString( queryBody.getQuery() );
    for (String word: words){
      if (!qrtable.get(qrpHash(word, 0, word.length(), qrtable.getTableBits()))) return false;
    }
    return true;
  }

  /**
   * Splits a file path into pieces and takes creates a array of the pieces
   * and there prefixes.
   */
  private static String[] splitQueryString( String queryString ) {
    StringTokenizer tokenizer = new StringTokenizer( queryString,
      CoreRoutingTable.FILE_DELIMITERS );
    ArrayList<String> list = new ArrayList<String>( 10 );
    while( tokenizer.hasMoreTokens() ) {
      String word = tokenizer.nextToken();
      list.add( word );
    }
    String[] strArr = new String[ list.size() ];
    list.toArray( strArr );
    return strArr;
  }

  public Iterator<RoutingBody> buildRoutingBodyIterator( CoreRoutingTable currentTable, CoreRoutingTable oldTable ) {
    ArrayList<RoutingBody> msgList = new ArrayList<RoutingBody>();

    if ( oldTable == null ) {
      // never sent a table before... send reset msg first
      msgList.add( new ResetBody( currentTable.getTableSize(), currentTable.getInfinity() ) );
    }

    boolean isPatchNeeded = false;
    // we always send 4 signed bits tables, therefore we only need to patch half
    // table size.
    byte[] patchData = new byte[ ( (int) currentTable.getTableSize()) / 2 ];

    for ( int i = 0; i < patchData.length; i++ ) {
      byte b1;
      byte b2;
      if ( oldTable == null ) {
        if ( currentTable.getQrTable().get( i * 2 ) ) {
          b1 = (byte)(1 - currentTable.getInfinity());
        }
        else {
          b1 = (byte)0;
        }
        if ( currentTable.getQrTable().get( i * 2 + 1) ) {
          b2 = (byte)(1 - currentTable.getInfinity());
        }
        else {
          b2 = (byte)0;
        }
      }
      else {
        boolean currentVal = currentTable.getQrTable().get( i * 2 );
        if ( currentVal == oldTable.getQrTable().get( i * 2 ) ) {
          b1 = (byte)0;
        }
        else if ( currentVal ) {
          b1 = (byte)(1 - currentTable.getInfinity());
        }
        else {
          b1 = (byte)(currentTable.getInfinity() - 1);
        }
        currentVal = currentTable.getQrTable().get( i * 2 + 1);
        if ( currentVal == oldTable.getQrTable().get( i * 2 + 1) ) {
          b2 = (byte)0;
        }
        else if ( currentVal ) {
          b2 = (byte)(1 - currentTable.getInfinity());
        }
        else {
          b2 = (byte)(currentTable.getInfinity() - 1);
        }
      }
      patchData[i] = (byte)( ( b1 << 4 ) | ( b2 & 0x0F ) );

      // check if we need a patch
      if ( patchData[i] != 0 ) {
        isPatchNeeded = true;
      }
    }

    if ( !isPatchNeeded ) {// no patch message needed
      return msgList.iterator();
    }

    // try to compress data
    byte compressor = PatchBody.COMPRESSOR_NONE;
    byte[] compressedPatchData = ioUtils.deflate( patchData );

    byte[] currData = null;
    currData = ioUtils.deflate(currData);

    // verify if compressing made sense...
    if ( compressedPatchData.length < patchData.length ) {
      patchData = compressedPatchData;
      compressor = PatchBody.COMPRESSOR_ZLIB;
    }

    // build patch messages
    // 1KB max message size was proposed...

    byte sequenceSize = (byte)Math.ceil( (double)patchData.length /
      (double) PatchBody.MAX_MESSAGE_DATA_SIZE );

    byte sequenceNo = 1;
    int offset = 0;
    do {
      int length = Math.min( PatchBody.MAX_MESSAGE_DATA_SIZE,
        patchData.length - offset );

      PatchBody msg = new PatchBody( sequenceNo, sequenceSize,
        compressor, (byte)4, patchData );

      msgList.add( msg );
      offset += length;
      sequenceNo ++;
    }
    while ( offset < patchData.length );
    return msgList.iterator();
  }

  /**
   * Returns the same value as hash(x.substring(start, end), bits),
   * but tries to avoid allocations.  Note that x is lower-cased
   * when hashing.
   *
   * @param x the string to hash
   * @param bits the number of bits to use in the resulting answer
   * @param start the start offset of the substring to hash
   * @param end just PAST the end of the substring to hash
   * @return the hash value
   */
  private static int qrpHash( String x, int start, int end, byte bits ) {
    //1. First turn x[start...end-1] into a number by treating all 4-byte
    //chunks as a little-endian quadword, and XOR'ing the result together.
    //We pad x with zeroes as needed.
    //    To avoid having do deal with special cases, we do this by XOR'ing
    //a rolling value one byte at a time, taking advantage of the fact that
    //x XOR 0==x.
    int xor=0;  //the running total
    int j=0;    //the byte position in xor.  INVARIANT: j==(i-start)%4
    for (int i=start; i<end; i++)
    {
      //TODO: internationalization be damned?
      int b = Character.toLowerCase(x.charAt(i)) & 0xFF;
      b = b<<(j*8);
      xor = xor^b;
      j = (j+1)%4;
    }
    //2. Now map number to range 0 - (2^bits-1).
    //Multiplication-based hash function.  See Chapter 12.3.2. of CLR.
    long prod= (long)xor * (long) CoreRoutingTable.A_INT;
    long ret= prod << 32;
    ret = ret >>> (32 + (32 - bits));
    return (int)ret;
  }



}