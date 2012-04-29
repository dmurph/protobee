package org.protobee.gnutella.messages;

import java.net.InetSocketAddress;

import javax.annotation.Nullable;

import org.protobee.extension.GGEP;
import org.protobee.extension.HUGEExtension;
import org.protobee.gnutella.routing.message.PatchBody;
import org.protobee.gnutella.routing.message.ResetBody;
import org.protobee.util.VendorCode;

import com.google.inject.assistedinject.Assisted;


public interface MessageBodyFactory {
  PingBody createPingMessage(@Assisted("ggep") GGEP ggep);

  PongBody createPongMessage( InetSocketAddress address, @Assisted("fileCount") long fileCount, 
                              @Assisted("fileSizeInKB") long fileSizeInKB, @Assisted("ggep") GGEP ggep);

  QueryBody createQueryMessage( short minSpeed, String query, @Assisted @Nullable HUGEExtension huge, @Assisted @Nullable GGEP ggep);

  PushBody createPushMessage(byte[] servantID, long index, 
                             InetSocketAddress socketAddress, @Assisted @Nullable GGEP ggep);

  QueryHitBody createQueryHitMessage( InetSocketAddress socketAddress, long speed, 
                                      @Nullable @Assisted ResponseBody[] hitList, VendorCode vendorCode,
                                      @Assisted("flags") byte flags, @Assisted("controls") byte controls, 
                                      @Assisted("privateArea1") byte[] privateArea1, @Nullable @Assisted GGEP ggep,
                                      @Assisted("xmlBytes") byte[] xmlBytes, @Assisted("privateArea2") byte[] privateArea2,
                                      @Assisted("servantID") byte[] servantID);

  ResetBody createResetMessage( long tableLength, byte infinity);

  PatchBody createPatchMessage( @Assisted("sequenceNum") byte sequenceNum,
                                @Assisted("sequenceSize") byte sequenceSize,
                                @Assisted("compressor") byte compressor,
                                @Assisted("entryBits") byte entryBits,
                                byte[] data );

}