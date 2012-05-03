package org.protobee.examples.questions.modules;

import org.protobee.compatability.Headers;
import org.protobee.events.BasicMessageReceivedEvent;
import org.protobee.examples.questions.QuestionMessage;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;

import com.google.common.eventbus.Subscribe;

@SessionScope
@Headers(required = {})
public class QuestionModule extends ProtocolModule {

  @Subscribe
  public void messageRecieved(BasicMessageReceivedEvent event) {
    QuestionMessage message = (QuestionMessage) event.getMessage();
    
  }
}
