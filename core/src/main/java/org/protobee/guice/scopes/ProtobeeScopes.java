package org.protobee.guice.scopes;

public class ProtobeeScopes {
  public static final MultiScope SESSION = new MultiScope("SESSION", SessionScopeHolder.class);
  public static final MultiScope IDENTITY = new MultiScope("IDENTITY", IdentityScopeHolder.class);
  public static final MultiScope PROTOCOL = new MultiScope("PROTOCOL", ProtocolScopeHolder.class);
}
