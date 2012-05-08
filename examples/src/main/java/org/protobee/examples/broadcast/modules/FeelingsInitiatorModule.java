package org.protobee.examples.broadcast.modules;

import java.net.SocketAddress;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.protobee.annotation.InjectLogger;
import org.protobee.compatability.Headers;
import org.protobee.events.BasicMessageReceivedEvent;
import org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.modules.ProtocolModule;
import org.protobee.network.ConnectionCreator;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolModel;
import org.protobee.util.SocketAddressUtils;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@SessionScope
@Headers(required = {})
public class FeelingsInitiatorModule extends ProtocolModule {

  @InjectLogger
  private Logger log;
  private final NetworkIdentity identity;
  private final Protocol feelingsProtocol;
  private final ConnectionCreator creator;
  private final ProtocolModel feelingsModel;
  private final SocketAddressUtils addressUtils;

  @Inject
  public FeelingsInitiatorModule(NetworkIdentity identity, 
      ConnectionCreator creator, Protocol feelings, ProtocolModel feelingsModel,
      SocketAddressUtils addressUtils) {
    this.identity = identity;
    this.creator = creator;
    this.feelingsProtocol = feelings;
    this.feelingsModel = feelingsModel;
    this.addressUtils = addressUtils;
  }

  @Subscribe
  public void messageReceived(BasicMessageReceivedEvent event) {
    Preconditions.checkArgument(event.getMessage() instanceof BroadcastMessage,
        "Not a broadcast message");
    BroadcastMessage message = (BroadcastMessage) event.getMessage();

    if (message.getMessage().equals("feelings!") && !identity.hasCurrentSession(feelingsProtocol)) {
      SocketAddress address =
          addressUtils.getAddress(message.getListeningAddress(), message.getListeningPort());
      log.info("Connecting to address " + address + " with feelings protocol");
      creator.connect(feelingsModel, address, HttpMethod.valueOf("SAY"), "/");
    }
  }
}
