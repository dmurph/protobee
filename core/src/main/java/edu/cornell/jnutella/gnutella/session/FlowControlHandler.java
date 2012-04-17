package edu.cornell.jnutella.gnutella.session;

import org.jboss.netty.channel.ChannelDownstreamHandler;

import com.google.inject.Singleton;

@Singleton
public interface FlowControlHandler extends ChannelDownstreamHandler {

}
