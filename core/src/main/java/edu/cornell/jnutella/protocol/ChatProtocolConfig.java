package edu.cornell.jnutella.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;

import com.google.inject.Provider;

import edu.cornell.jnutella.annotation.LimeChat;
import edu.cornell.jnutella.session.SessionModel;

//@Protocol(name = "CHAT", version = "0.1", headerRegex = "^CHAT CONNECT/0\\.1$")
//public class ChatProtocolConfig implements ProtocolConfig {
//
//  private final Provider<Iterable<ChannelHandler>> channelsProvider;
//
//  public ChatProtocolConfig(@LimeChat Provider<Iterable<ChannelHandler>> channelsProvider) {
//    this.channelsProvider = channelsProvider;
//  }
//
//  @Override
//  public SessionModel createSessionModel(Channel channel, Protocol protocol) {
//    return null; // TODO
//  }
//
//  @Override
//  public Iterable<ChannelHandler> createChannelHandlers() {
//    return channelsProvider.get();
//  }
//
//}
