package org.protobee.modules;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.protobee.annotation.InjectLogger;
import org.protobee.session.HandshakeInterruptor;
import org.protobee.session.HandshakeReceivedEvent;
import org.protobee.session.SessionModel;
import org.protobee.session.SessionState;
import org.protobee.util.PreFilter;
import org.protobee.util.VersionComparator;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;


// not complete
public abstract class SimpleConnectionFilterModule implements ProtocolModule {

  @InjectLogger
  protected Logger log;
  private final Set<ClassFilterEntry> classRules;
  private final Map<SessionState, Set<HeaderFilterEntry>> headerRules;
  private final Map<SessionState, Set<CustomFilterEntry>> customRules;
  private final SessionModel session;
  private final VersionComparator comp;

  protected SimpleConnectionFilterModule(SessionModel session, VersionComparator comp,
      @Nullable Map<SessionState, Set<HeaderFilterEntry>> headerFilters,
      @Nullable Set<ClassFilterEntry> classFilters,
      @Nullable Map<SessionState, Set<CustomFilterEntry>> customFilters) {
    this.classRules =
        classFilters != null ? ImmutableSet.copyOf(classFilters) : ImmutableSet
            .<ClassFilterEntry>of();
    this.headerRules =
        headerFilters != null ? ImmutableMap.copyOf(headerFilters) : ImmutableMap
            .<SessionState, Set<HeaderFilterEntry>>of();
    this.customRules =
        customFilters != null ? ImmutableMap.copyOf(customFilters) : ImmutableMap
            .<SessionState, Set<CustomFilterEntry>>of();

    this.comp = comp;
    this.session = session;
  }

  @Subscribe
  public void handshakeReceived(HandshakeReceivedEvent event) {
    SessionState state = session.getSessionState();

    Set<HeaderFilterEntry> headerFilters = headerRules.get(state);
    Set<CustomFilterEntry> customFilters = customRules.get(state);
    HttpMessage message = event.getMessage();
    HandshakeInterruptor interruptor = event.getInterruptor();

    if (customFilters != null) {
      for (CustomFilterEntry customFilter : customFilters) {
        String error = customFilter.allowed.shouldFilter(message);
        if (error != null) {
          log.info("Rejecting connection by custom filter, reason: " + error);
          interruptor.disconnectWithStatus(new HttpResponseStatus(customFilter.code, error));
        }
      }
    }
    if (headerFilters != null) {
      for (HeaderFilterEntry headerFilterEntry : headerFilters) {
        FilterHeader filterHeader = headerFilterEntry.filterHeader;
        String value = message.getHeader(filterHeader.name);
        if (value == null) {
          if (headerFilterEntry.type == FilterType.INCLUSION) {
            interruptor.disconnectWithStatus(new HttpResponseStatus(headerFilterEntry.code,
                headerFilterEntry.reason));
          }
          continue;
        }
        if (!comp.isValidVersionString(value)) {
          log.info("Not a valid version string '" + value + "' for header " + filterHeader.name
              + ".  Rejecting.");
          interruptor.disconnectWithStatus(new HttpResponseStatus(headerFilterEntry.code,
              headerFilterEntry.reason));
        }


      }
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
    private final int code;
    private final String reason;

    public HeaderFilterEntry(FilterType type, FilterHeader filterHeader) {
      this(type, filterHeader, 503, "");
    }

    public HeaderFilterEntry(FilterType type, FilterHeader filterHeader, int code, String reason) {
      Preconditions.checkNotNull(type);
      Preconditions.checkNotNull(filterHeader);
      Preconditions.checkNotNull(reason);
      this.type = type;
      this.filterHeader = filterHeader;
      this.code = code;
      this.reason = reason;
    }
  }

  public static class ClassFilterEntry {
    private final FilterType type;
    private final Class<? extends ProtocolModule> moduleClass;
    private final int code;
    private final String reason;

    public ClassFilterEntry(FilterType type, Class<? extends ProtocolModule> moduleClass) {
      this(type, moduleClass, 503, "");
    }

    public ClassFilterEntry(FilterType type, Class<? extends ProtocolModule> moduleClass, int code,
        String reason) {
      Preconditions.checkNotNull(type);
      Preconditions.checkNotNull(moduleClass);
      Preconditions.checkNotNull(reason);
      this.type = type;
      this.moduleClass = moduleClass;
      this.code = code;
      this.reason = reason;
    }
  }

  public static class CustomFilterEntry {
    private final PreFilter<HttpMessage> allowed;
    private final int code;

    public CustomFilterEntry(PreFilter<HttpMessage> allowed) {
      this(allowed, 503);
    }

    public CustomFilterEntry(PreFilter<HttpMessage> allowed, int code) {
      Preconditions.checkNotNull(allowed);
      this.allowed = allowed;
      this.code = code;
    }
  }
}
