package edu.cornell.jnutella.protocol.headers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;

import edu.cornell.jnutella.guice.Slf4jTypeListener;

public class HeaderMergerTest {

  @Test
  public void testGenerateMergedModuleHeaders() {
    CompatabilityHeader[] required =
        new CompatabilityHeaderImpl[] {new CompatabilityHeaderImpl("A", "0.1", "0.5"),
            new CompatabilityHeaderImpl("A", "0.1", "0.4"),
            new CompatabilityHeaderImpl("B", "0.1", "0.1"),
            new CompatabilityHeaderImpl("B", "0.1", "0.5"),
            new CompatabilityHeaderImpl("C", "4", "8"), new CompatabilityHeaderImpl("C", "2", "9"),};

    CompatabilityHeader[] requested = new CompatabilityHeader[0];

    Headers header = new HeadersImpl(required, requested);

    Injector inj = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {}
    });

    CompatabilityHeaderMerger merger = inj.getInstance(CompatabilityHeaderMerger.class);

    HttpMessage mockMessage = mock(HttpMessage.class);
    merger.addHeaders(header);
    merger.populateModuleHeaders(mockMessage);

    verify(mockMessage, times(1)).addHeader(eq("A"), eq("0.4"));
    verify(mockMessage, times(1)).addHeader(eq("B"), eq("0.1"));
    verify(mockMessage, times(1)).addHeader(eq("C"), eq("8"));
  }

  @Test
  public void testGenerateMergedIncomingHeaders() {
    CompatabilityHeader[] required =
        new CompatabilityHeaderImpl[] {new CompatabilityHeaderImpl("A", "0.1", "0.5"),
            new CompatabilityHeaderImpl("A", "0.1", "0.4"),
            new CompatabilityHeaderImpl("B", "0.1", "0.1"),
            new CompatabilityHeaderImpl("B", "0.1", "0.5"),
            new CompatabilityHeaderImpl("C", "4", "8"), new CompatabilityHeaderImpl("C", "2", "9"),};

    CompatabilityHeader[] requested = new CompatabilityHeader[0];

    Headers header = new HeadersImpl(required, requested);

    Injector inj = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {

        bindListener(Matchers.any(), new Slf4jTypeListener());
      }
    });

    CompatabilityHeaderMerger merger = inj.getInstance(CompatabilityHeaderMerger.class);

    HttpMessage mockMessage = mock(HttpMessage.class);

    List<Map.Entry<String, String>> headers = Lists.newArrayList();
    headers.add(new MyEntry("A", "0.2"));
    headers.add(new MyEntry("B", "0.8"));
    headers.add(new MyEntry("D", "0.8"));
    when(mockMessage.getHeaders()).thenReturn(headers);
    merger.addHeaders(header);

    Map<String, String> agreed = merger.mergeHeaders(mockMessage);

    assertTrue(agreed.containsKey("A"));
    assertTrue(agreed.containsKey("B"));
    assertFalse(agreed.containsKey("C"));
    assertFalse(agreed.containsKey("D"));

    assertEquals("0.2", agreed.get("A"));
    assertEquals("0.1", agreed.get("B"));
  }

  private static class MyEntry implements Entry<String, String> {
    private final String key;
    private final String value;

    public MyEntry(String key, String value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public String setValue(String value) {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
