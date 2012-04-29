package org.protobee.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.protobee.annotation.LimeChat;
import org.protobee.session.SessionModel;

import com.google.inject.Provider;


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
