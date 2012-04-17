package edu.cornell.jnutella;

public interface Stoppable {
  void signalShutdown();
  void join(int maxMillis);
}
