package edu.cornell.jnutella.gnutella.messages;

import java.net.InetSocketAddress;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.extension.HUGEExtension;
import edu.cornell.jnutella.gnutella.routing.PatchBody;
import edu.cornell.jnutella.gnutella.routing.ResetBody;
import edu.cornell.jnutella.util.GUID;
import edu.cornell.jnutella.util.VendorCode;

public interface MessageBodyFactory {
  PingBody createPingMessage(@Assisted("ggep") GGEP ggep);

  PongBody createPongMessage( InetSocketAddress address, @Assisted("fileCount") long fileCount, 
                              @Assisted("fileSizeInKB") long fileSizeInKB, @Assisted("ggep") GGEP ggep);

  QueryBody createQueryMessage(short minSpeed, String query, @Assisted @Nullable HUGEExtension huge, @Assisted @Nullable GGEP ggep);

  PushBody createPushMessage(GUID servantID, long index, 
                             InetSocketAddress socketAddress, @Assisted @Nullable GGEP ggep);

  QueryHitBody createQueryHitMessage( InetSocketAddress socketAddress, @Assisted long speed, 
                                      @Nullable @Assisted ResponseBody[] hitList, @Assisted VendorCode vendorCode,
                                      @Assisted("flags") byte flags, @Assisted("controls") byte controls, 
                                      @Assisted("privateArea1") byte[] privateArea1, @Nullable @Assisted GGEP ggep,
                                      @Assisted("xmlBytes") byte[] xmlBytes, @Assisted("privateArea2") byte[] privateArea2,
                                      @Assisted GUID servantID);

  ResetBody createResetMessage( @Assisted long tableLength, @Assisted byte infinity);

  PatchBody createPatchMessage( @Assisted("sequenceNum") byte sequenceNum,
                                @Assisted("sequenceSize") byte sequenceSize,
                                @Assisted("compressor") byte compressor,
                                @Assisted("entryBits") byte entryBits,
                                @Assisted byte[] data );

}