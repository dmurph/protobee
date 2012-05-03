package org.protobee.examples.questions.modules;

import org.protobee.compatability.Headers;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;

@SessionScope
@Headers(required = {})
public class QuestionModule extends ProtocolModule {
  
    
}
