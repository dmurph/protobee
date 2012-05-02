package org.protobee.guice.scopes;

import java.util.Map;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

public class ScopesGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bindScope(SessionScope.class, ProtobeeScopes.SESSION);
    bindScope(IdentityScope.class, ProtobeeScopes.IDENTITY);
    bindScope(ProtocolScope.class, ProtobeeScopes.PROTOCOL);

    // bind our map providers
    TypeLiteral<Map<String, Object>> scopeMap = new TypeLiteral<Map<String, Object>>() {};
    bind(scopeMap).annotatedWith(NewSessionScopeHolder.class).toProvider(
        DefaultScopeMapProvider.class);
    bind(scopeMap).annotatedWith(NewIdentityScopeHolder.class).toProvider(
        DefaultScopeMapProvider.class);
    bind(scopeMap).annotatedWith(NewProtocolScopeHolder.class).toProvider(
        DefaultScopeMapProvider.class);

    bind(ScopeHolder.class).annotatedWith(SessionScopeHolder.class)
        .toProvider(new PrescopedProvider<ScopeHolder>("ScopeHolder")).in(SessionScope.class);
    bind(ScopeHolder.class).annotatedWith(IdentityScopeHolder.class)
        .toProvider(new PrescopedProvider<ScopeHolder>("ScopeHolder")).in(IdentityScope.class);
    bind(ScopeHolder.class).annotatedWith(ProtocolScopeHolder.class)
        .toProvider(new PrescopedProvider<ScopeHolder>("ScopeHolder")).in(ProtocolScope.class);
  }

  @Provides
  @NewSessionScopeHolder
  public ScopeHolder createSessionHolder(@NewSessionScopeHolder Map<String, Object> scopeMap) {
    return ProtobeeScopes.SESSION.createScopeHolder(scopeMap);
  }

  @Provides
  @NewIdentityScopeHolder
  public ScopeHolder createIdentityHolder(@NewIdentityScopeHolder Map<String, Object> scopeMap) {
    return ProtobeeScopes.IDENTITY.createScopeHolder(scopeMap);
  }

  @Provides
  @NewProtocolScopeHolder
  public ScopeHolder createProtocolHolder(@NewProtocolScopeHolder Map<String, Object> scopeMap) {
    return ProtobeeScopes.PROTOCOL.createScopeHolder(scopeMap);
  }
}
