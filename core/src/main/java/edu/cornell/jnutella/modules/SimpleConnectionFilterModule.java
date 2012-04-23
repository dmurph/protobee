package edu.cornell.jnutella.modules;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMessage;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.session.GnutellaSessionState;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.session.HandshakeReceivedEvent;
import edu.cornell.jnutella.session.SessionModel;

// not complete
public abstract class SimpleConnectionFilterModule implements ProtocolModule {

  private final Set<ClassFilterEntry> classRules;
  private final Set<HeaderFilterEntry> headerRules;
  private final Set<Predicate<HttpMessage>> customRules;
  private final SessionModel session;

  public SimpleConnectionFilterModule(Set<HeaderFilterEntry> headerFilters,
      Set<ClassFilterEntry> classFilters, Set<Predicate<HttpMessage>> customFilters,
      SessionModel session) {
    this.classRules = ImmutableSet.copyOf(classFilters);
    this.headerRules = ImmutableSet.copyOf(headerFilters);
    this.customRules = ImmutableSet.copyOf(customFilters);
    this.session = session;
  }

  @Subscribe
  public void handshakeReceived(HandshakeReceivedEvent event) {
    switch (session.getSessionState()) {
      case HANDSHAKE_0:
      case HANDSHAKE_1:
        
        break;
        
      case HANDSHAKE_2:
        
        break;

      default:
        break;
    }
  }

  public static enum FilterType {
    /**
     * Connections have to include the header or module
     */
    INCLUSION,
    /**
     * Connections cannot include the header or module
     */
    EXCLUSION
  }
  
  public static class FilterHeader {
    private final String name;
    private final String minVersion;
    private final String maxVersion;

    public FilterHeader(String name) {
      this(name, null, null);
    }

    public FilterHeader(String name, String version) {
      this(name, version, version);
    }

    public FilterHeader(String name, String minVersion, String maxVersion) {
      this.name = name;
      this.minVersion = minVersion;
      this.maxVersion = maxVersion;
    }
  }
  
  public static class HeaderFilterEntry {
    private final FilterType type;
    private final FilterHeader filterHeader;

    public HeaderFilterEntry(FilterType type, FilterHeader filterHeader) {
      this.type = type;
      this.filterHeader = filterHeader;
    }
  }

  public static class ClassFilterEntry {
    private final FilterType type;
    private final Class<? extends ProtocolModule> moduleClass;

    public ClassFilterEntry(FilterType type, GnutellaSessionState state, Class<? extends ProtocolModule> moduleClass) {
      super();
      this.type = type;
      this.moduleClass = moduleClass;
    }
  }

  public static class CustomFilterEntry {
    private final Predicate<HttpMessage> allowed;

    public CustomFilterEntry(Predicate<HttpMessage> allowed) {
      this.allowed = allowed;
    }
  }
}
