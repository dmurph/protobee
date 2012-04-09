package edu.cornell.jnutella.session.gnutella;

import org.jboss.netty.channel.ChannelDownstreamHandler;

import com.google.inject.Singleton;

@Singleton
public interface FlowControlHandler extends ChannelDownstreamHandler {

  void stop(int maxWaitMillis);
}
