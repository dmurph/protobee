package edu.cornell.jnutella.network;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import com.google.inject.Inject;

import edu.cornell.jnutella.messages.decoding.PartDecoder;
import edu.cornell.jnutella.messages.decoding.PartEncoder;

public class GnutellaPipelineFactory implements ChannelPipelineFactory {

  @Inject
  public GnutellaPipelineFactory(PartEncoder<?> encoders, PartDecoder<?> decoders) {
    // TODO Auto-generated constructor stub
  }

  @Override
  public ChannelPipeline getPipeline() throws Exception {
    ChannelPipeline pipeline = Channels.pipeline();
    return null;
  }
}
