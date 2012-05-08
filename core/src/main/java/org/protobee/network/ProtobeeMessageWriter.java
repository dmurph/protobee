package org.protobee.network;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.HttpMethod;


/**
 * Writes a message to an identity or session with any protocol registered in the framework.
 * 
 * @author Daniel
 */
public interface ProtobeeMessageWriter {

  public static enum ConnectionOptions {
    CAN_CREATE_CONNECTION, EXIT_IF_NO_CONNECTION
  }

  public static enum HandshakeOptions {
    WAIT_FOR_HANDSHAKE, EXIT_IF_HANDSHAKING
  }

  /**
   * Writes the object the scoped session (using it's protocol) Preconditions: in session scope
   */
  ChannelFuture write(Object message);

  /**
   * Writes the object the scoped identity using the scoped protocol, using the handshake options.
   * Connection options defaults to {@link ConnectionOptions#CAN_CREATE_CONNECTION}, with handshake
   * options of {@link HandshakeOptions#WAIT_FOR_HANDSHAKE}. method and uri are for making
   * connections if needed. Preconditions: in identity and protocol scope, NOT in a session scope
   */
  ChannelFuture write(final Object message, HttpMethod method, String uri);

  /**
   * Writes the object the scoped identity using the scoped protocol, using the handshake options.
   * Connection options defaults to {@link ConnectionOptions#EXIT_IF_NO_CONNECTION}. Preconditions:
   * in identity and protocol scope, NOT in a session scope
   */
  ChannelFuture write(final Object message, HandshakeOptions handshakeOptions);

  /**
   * Sends the message to the scoped identity using the scoped protocol, with the give options.
   * method and uri are for making new connections if needed. Preconditions: in identity and
   * protocol scope, NOT in a session scope
   */
  ChannelFuture write(Object message, ConnectionOptions connectionOptions, HttpMethod method,
      String uri, HandshakeOptions handshakeOptions);
}
