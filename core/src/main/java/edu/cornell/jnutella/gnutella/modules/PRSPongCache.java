package edu.cornell.jnutella.gnutella.modules;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PongBody;
import edu.cornell.jnutella.util.Clock;

/**
 * <a href=
 * "http://f1.grp.yahoofs.com/v1/EFiTT1ukSOM2-bOUFX1VAY8kPSLBvOZCb1JJf-wCWysyDL_jIEW7BrT-IZUw-As-UJjPNX4ktqsrJcHX8kDXRE0iB0vxIK0H13EBJuTKorDKj68/Proposals/Working%20Proposals/PONG/Variants/pingreduce.txt"
 * >Pong reduction scheme</a> pong cache
 * 
 * @author Daniel
 */
@Singleton
public class PRSPongCache implements PongCache {
  
  private final Set<CacheElement> cache = Sets.newLinkedHashSet();
  private final Object cacheLock = new Object();
  private final Clock clock;
  private final MessageBodyFactory factory;

  @Inject
  public PRSPongCache(Clock clock, MessageBodyFactory factory) {
    this.clock = clock;
    this.factory = factory;
  }

  @Override
  public void addPong(GnutellaMessage message) {
    Preconditions.checkArgument(message.getBody() instanceof PongBody);
    MessageHeader header = message.getHeader();
    Preconditions.checkArgument(header.getHops() != 0);

    PongBody body = (PongBody) message.getBody();
    CacheElement cacheElement =
        new CacheElement(body.getAddress(), body.getFileCount(), body.getFileSizeInKB(),
            clock.currentTimeMillis(), body.getGgep());
    synchronized (cacheLock) {
      cache.add(cacheElement);
    }
  }

  @Override
  public void filter(long timoutMillis) {
    long now = clock.currentTimeMillis();
    synchronized (cacheLock) {
      Iterator<CacheElement> it = cache.iterator();
      while(it.hasNext()) {
        CacheElement element = it.next();
        if(now - element.millisAdded > timoutMillis) {
          it.remove();
        } else {
          break;
        }
      }
    }
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public Iterable<PongBody> getPongs(SocketAddress destAddress, int num) {
    synchronized (cacheLock) {
      return ImmutableSet.copyOf(Iterables.transform(Iterables.limit(cache, num),
          new Function<PRSPongCache.CacheElement, PongBody>() {
            @Override
            public PongBody apply(CacheElement input) {
              // TODO change when it's a socketaddress
              return factory.createPongMessage(null, 0, input.numFiles, input.fileSize, input.ggep);
            }
          }));
    }
  }

  private static class CacheElement {

    final SocketAddress address;
    final long numFiles;
    final long fileSize;
    final long millisAdded;
    final GGEP ggep;

    public CacheElement(SocketAddress address, long numFiles, long fileSize, long millisAdded,
        GGEP ggep) {
      this.address = address;
      this.numFiles = numFiles;
      this.fileSize = fileSize;
      this.millisAdded = millisAdded;
      this.ggep = ggep;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((address == null) ? 0 : address.hashCode());
      result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
      result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
      result = prime * result + (int) (numFiles ^ (numFiles >>> 32));
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      CacheElement other = (CacheElement) obj;
      if (address == null) {
        if (other.address != null) return false;
      } else if (!address.equals(other.address)) return false;
      if (fileSize != other.fileSize) return false;
      if (ggep == null) {
        if (other.ggep != null) return false;
      } else if (!ggep.equals(other.ggep)) return false;
      if (numFiles != other.numFiles) return false;
      return true;
    }
  }

}
