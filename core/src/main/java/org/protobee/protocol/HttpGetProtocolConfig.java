package org.protobee.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.protobee.gnutella.Gnutella;
import org.protobee.session.SessionModel;

import com.google.inject.Provider;


//@Protocol(name = "GET", version = "1.1", headerRegex = "^GET /[\\d]+/[^\\\\/:\\*\\\"<>\\|]+ HTTP/1.1$")
//public class HttpGetProtocolConfig implements ProtocolConfig {
//
//  private final Provider<Iterable<ChannelHandler>> channelsProvider;
//
//  public HttpGetProtocolConfig(@Gnutella Provider<Iterable<ChannelHandler>> channelsProvider) {
//    this.channelsProvider = channelsProvider;
//  }
//
//  @Override
//  public SessionModel createSessionModel(Channel channel, Protocol protocol) {
//    return null; // todo;
//  }
//
//  @Override
//  public Iterable<ChannelHandler> createChannelHandlers() {
//    return channelsProvider.get();
//  }
//
//}
