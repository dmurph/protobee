package org.protobee.network;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.protobee.protocol.Protocol;
import org.protobee.util.ProtocolUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class ProtobeeChannels {

  private final ChannelGroup channels;
  private final Map<Protocol, ChannelGroup> protocolChannels;

  @Inject
  public ProtobeeChannels(Set<Protocol> protocols) {
    this.channels = new DefaultChannelGroup("JnutellaChannelGroup");
    this.protocolChannels = new MapMaker().concurrencyLevel(protocols.size()).makeMap();
    for (Protocol protocol : protocols) {
      this.protocolChannels.put(protocol, new DefaultChannelGroup(ProtocolUtils.toString(protocol)
          + "-ChannelGroup"));
    }
  }

  /**
   * Adds a channel to the group that's associated with a protocol
   * 
   * @param channel
   * @param protocol
   */
  public void addChannel(Channel channel, Protocol protocol) {
    Preconditions
        .checkArgument(protocolChannels.containsKey(protocol), "Protocol not from configs");
    ChannelGroup group = protocolChannels.get(protocol);
    group.add(channel);
    channels.add(channel);
  }

  /**
   * Adds a channel to the group that's associated with a protocol
   * 
   * @param channel
   * @param protocols
   */
  public void addChannel(Channel channel, Set<Protocol> protocols) {
    for (Protocol protocol : protocols) {
      Preconditions.checkArgument(protocolChannels.containsKey(protocol),
          "Protocol not from configs");
      ChannelGroup group = protocolChannels.get(protocol);
      group.add(channel);
    }
    channels.add(channel);
  }

  public void removeChannel(Channel channel, Protocol protocol) {
    channels.remove(channel);
    protocolChannels.get(protocol).remove(channel);
  }

  /**
   * Gets all channels
   * 
   * @return
   */
  public ChannelGroup getChannels() {
    return channels;
  }

  /**
   * Gets the channels for a protocol
   * 
   * @param protocol
   * @return
   */
  public ChannelGroup getChannels(Protocol protocol) {
    return protocolChannels.get(protocol);
  }

  public void clear() {
    channels.clear();
    for (ChannelGroup group : protocolChannels.values()) {
      group.clear();
    }
  }
}
