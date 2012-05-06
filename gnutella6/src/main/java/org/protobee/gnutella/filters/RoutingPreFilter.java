package org.protobee.gnutella.filters;

import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.util.PreFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class RoutingPreFilter implements PreFilter<GnutellaMessage> {


  @Inject
  public RoutingPreFilter() {
  }

  @Override
  public String shouldFilter(GnutellaMessage message) {
    // // TODO Auto-generated method stub IF ULTRAPEER I THINK
    return null;
  }
}
