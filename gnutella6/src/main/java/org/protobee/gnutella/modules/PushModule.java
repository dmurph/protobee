package org.protobee.gnutella.modules;

import org.protobee.gnutella.GnutellaServantModel;
import org.protobee.gnutella.RequestFilter;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.PushBody;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.managers.PushRoutingTableManager;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.guice.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.modules.ProtocolModule;
import org.protobee.network.ProtocolMessageWriter;
import org.protobee.protocol.Protocol;
import org.protobee.stats.DropLog;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@SessionScope
public class PushModule implements ProtocolModule {

  private final RequestFilter filter;
  private final PushRoutingTableManager pushRTManager;
  private final ProtocolMessageWriter messageDispatcher;
  private final MessageHeader.Factory headerFactory;
  private final GnutellaServantModel servantModel;
  private final NetworkIdentity identity;
  private final Protocol gnutella;
  
  private final DropLog dropLog;

  @Inject
  public PushModule(RequestFilter filter, MessageHeader.Factory headerFactory,
      PushRoutingTableManager pushRTManager, ProtocolMessageWriter messageDispatcher,
      GnutellaServantModel servantModel, DropLog dropLog, NetworkIdentity identity,
      Protocol gnutella) {
    this.filter = filter;
    this.pushRTManager = pushRTManager;
    this.messageDispatcher = messageDispatcher;
    this.headerFactory = headerFactory;
    this.servantModel = servantModel;
    this.dropLog = dropLog;
    this.identity = identity;
    this.gnutella = gnutella;
  }

  private void pushMessageRecieved(MessageReceivedEvent event, MessageHeader header)
      throws InvalidMessageException {

    PushBody pushBody = (PushBody) event.getMessage().getBody();

    if (pushBody.getServantID().equals(servantModel.getGuid())) {
      // use locally
    }

    
    String filterOutput = (filter.shouldRoutePushMessage(header.getTtl(), servantModel.getGuid()));
    if (filterOutput != null) {
      dropLog.messageDropped(identity.getSendingAddress(gnutella), gnutella, event.getMessage(), filterOutput);
      return;
    }

    MessageHeader newHeader =
        headerFactory.create(header.getGuid(), MessageHeader.F_PUSH, (byte) (header.getTtl() - 1),
            (byte) (header.getHops() + 1));

    // route push message
    messageDispatcher.write(pushRTManager.findRouting(servantModel.getGuid()), new GnutellaMessage(
        newHeader, event.getMessage().getBody()));
  }

  @Subscribe
  public void messageReceived(MessageReceivedEvent event) throws InvalidMessageException {
    MessageHeader header = event.getMessage().getHeader();
    if (header.getPayloadType() == MessageHeader.F_PUSH) {
      pushMessageRecieved(event, header);
    }
  }
}
