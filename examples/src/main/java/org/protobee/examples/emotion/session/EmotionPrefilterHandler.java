package org.protobee.examples.emotion.session;

import java.util.Set;

import org.protobee.examples.emotion.Emotion;
import org.protobee.examples.protos.EmotionProtos.EmotionMessage;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.handlers.FilterChannelHandler;
import org.protobee.protocol.handlers.FilterMode;
import org.protobee.stats.DropLog;
import org.protobee.util.PreFilter;

public class EmotionPrefilterHandler extends FilterChannelHandler<EmotionMessage> {
  public EmotionPrefilterHandler(@Emotion Set<PreFilter<EmotionMessage>> filters,
                                   DropLog dropLog, Protocol protocol) {
    super(filters, dropLog, protocol, EmotionMessage.class, FilterMode.ERROR_ON_MISMATCHED_TYPE);
  }
}
