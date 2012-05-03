package org.protobee.gnutella.session;

import java.util.Set;

import org.protobee.gnutella.Gnutella;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.network.handlers.FilterChannelHandler;
import org.protobee.protocol.Protocol;
import org.protobee.stats.DropLog;
import org.protobee.util.PreFilter;

import com.google.inject.Inject;

public class GnutellaPrefilterHandler extends FilterChannelHandler<GnutellaMessage> {

  @Inject
  public GnutellaPrefilterHandler(@Gnutella Set<PreFilter<GnutellaMessage>> prefilters, DropLog dropLog,
      @Gnutella Protocol gnutella) {
    super(prefilters, dropLog, gnutella, GnutellaMessage.class,
        FilterMode.ERROR_ON_MISMATCHED_TYPE);
  }
}
