package edu.cornell.jnutella.gnutella.session;

import java.util.Map;
import java.util.Stack;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.util.Clock;

public class SachrifcFlowControl extends SimpleChannelDownstreamHandler
    implements
      FlowControlHandler {

  @InjectLogger
  private Logger log;
  private Clock clock;

  @Inject
  public SachrifcFlowControl(Clock clock) {

  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Preconditions
        .checkArgument(e.getMessage() instanceof GnutellaMessage, "Not a gnutella message");

    GnutellaMessage message = (GnutellaMessage) e.getMessage();

    
  }
  
  @Override
  public void stop(int maxWaitMillis) {
    // TODO Auto-generated method stub
    
  }

  private class FlowController implements Runnable {

    private final Map<Byte, Stack<QueuedMessage>> messagesMap = Maps.newHashMap();
    private final Object mapLock = new Object();

    private QueuedMessage[] pool = new QueuedMessage[10];
    private int location = 0;

    private boolean running = true;
    
    private void addMessage(GnutellaMessage message) {
      byte type = message.getHeader().getPayloadType();
      QueuedMessage queued = new QueuedMessage(message, clock.currentTimeMillis());
      synchronized (mapLock) {
        Stack<QueuedMessage> stack;
        if (!messagesMap.containsKey(type)) {
          stack = new Stack<SachrifcFlowControl.QueuedMessage>();
          messagesMap.put(type, stack);
        } else {
          stack = messagesMap.get(type);
        }
        stack.push(queued);
      }
    }

    @Override
    public void run() {
      running = true;
      while(running) {
        
      }
    }
  }

  private static class QueuedMessage {
    private GnutellaMessage message;
    private long queuedTime;

    public QueuedMessage(GnutellaMessage message, long queuedTime) {
      this.message = message;
      this.queuedTime = queuedTime;
    }

    public void set(GnutellaMessage message, long queuedTime) {
      this.message = message;
      this.queuedTime = queuedTime;
    }

    public void clear() {
      this.message = null;
    }

    public GnutellaMessage getMessage() {
      return message;
    }

    public long getQueuedTime() {
      return queuedTime;
    }
  }
}
