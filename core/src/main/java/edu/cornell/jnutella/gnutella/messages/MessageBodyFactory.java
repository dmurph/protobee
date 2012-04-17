package edu.cornell.jnutella.gnutella.messages;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.util.GUID;
import edu.cornell.jnutella.util.JnutellaSocketAddress;
import edu.cornell.jnutella.util.VendorCode;

public interface MessageBodyFactory {
  PingBody createPingMessage(@Assisted("ggep") GGEP ggep);

  PongBody createPongMessage(@Assisted JnutellaSocketAddress address, @Assisted("fileCount") long fileCount, 
                             @Assisted("fileSizeInKB") long fileSizeInKB, @Assisted("ggep") GGEP ggep);

  QueryBody createQueryMessage(short minSpeed, String query, GGEP ggep);
  
  PushBody createPushMessage(GUID servantID, long index, 
                             JnutellaSocketAddress socketAddress, GGEP ggep);
  
  QueryHitBody createQueryHitMessage( @Assisted JnutellaSocketAddress socketAddress, @Assisted long speed, 
                                      @Nullable @Assisted ResponseBody[] hitList, @Assisted VendorCode vendorCode,
                                      @Assisted("flags") byte flags, @Assisted("controls") byte controls, 
                                      @Assisted("privateArea1") byte[] privateArea1, @Nullable @Assisted GGEP ggep,
                                      @Assisted("xmlBytes") byte[] xmlBytes, @Assisted("privateArea2") byte[] privateArea2,
                                      @Assisted GUID servantID);
  
}