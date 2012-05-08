package org.protobee.examples.broadcast.session;

import java.util.Set;

import org.protobee.examples.broadcast.Broadcast;
import org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.handlers.FilterChannelHandler;
import org.protobee.protocol.handlers.FilterMode;
import org.protobee.stats.DropLog;
import org.protobee.util.PreFilter;

import com.google.inject.Inject;

public class BroadcastPrefilterHandler extends FilterChannelHandler<BroadcastMessage> {
  
  @Inject
  public BroadcastPrefilterHandler(@Broadcast Set<PreFilter<BroadcastMessage>> filters,
      DropLog dropLog, Protocol protocol) {
    super(filters, dropLog, protocol, BroadcastMessage.class, FilterMode.ERROR_ON_MISMATCHED_TYPE);
  }
}
