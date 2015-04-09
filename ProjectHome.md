This is a gui-less java framework to create arbitrary distributed networks with one or more protocols.

The framework is built on Netty and Guice, and provides the following:
  * Automated http handshaking with capability filtering
  * Protocol, Session, and Identity scopes for object storage
  * Easy inter-protocol and inter-identity communication
  * Various handlers + modules for common functions like filtering

Example protocol implementations are included, as well as a gnutella6 implementation.