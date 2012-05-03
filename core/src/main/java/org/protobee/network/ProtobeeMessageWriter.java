package org.protobee.network;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.protobee.identity.NetworkIdentity;
import org.protobee.network.ProtobeeMessageWriterImpl.ConnectionOptions;
import org.protobee.network.ProtobeeMessageWriterImpl.HandshakeOptions;



public interface ProtobeeMessageWriter {

  /**
   * Writes the object to this object's session.
   * 
   * @param message
   * @return
   */
  public abstract ChannelFuture write(Object message);

  /**
   * Writes the object to the given identity's session, using the the protocol of this object's
   * session. Defaults with options {@link ConnectionOptions#CAN_CREATE_CONNECTION} and
   * {@link HandshakeOptions#WAIT_FOR_HANDSHAKE}, with method of "CONNECT" and uri of ""
   * 
   * @param identity
   * @param message
   * @return
   */
  public abstract ChannelFuture write(NetworkIdentity identity, final Object message);

  /**
   * Writes the object to the given identity's session, using the the protocol of this object's
   * session. Uses connection and handshake options. If a new connection is required, method
   * defaults to "CONNECT" and uri is ""
   * 
   * @param identity
   * @param message
   * @param connectionOptions
   * @param handshakeOptions
   * @return
   */
  public abstract ChannelFuture write(final NetworkIdentity identity, final Object message,
      ConnectionOptions connectionOptions, HandshakeOptions handshakeOptions);

  /**
   * Writes the object to the given identity's session, using the the protocol of this object's
   * session. Uses connection and handshake options. If a new connection is required, the given
   * method and uri are used
   * 
   * @param identity
   * @param message
   * @param connectionOptions
   * @param method
   * @param uri
   * @param handshakeOptions
   * @return
   */
  public abstract ChannelFuture write(final NetworkIdentity identity, final Object message,
      ConnectionOptions connectionOptions, HttpMethod method, String uri,
      HandshakeOptions handshakeOptions);

}
