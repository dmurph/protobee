package org.protobee.examples.broadcast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Random;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.protobee.ProtobeeServantBootstrapper;
import org.protobee.ProtobeeGuiceModule;
import org.protobee.examples.broadcast.constants.BroadcastListeningAddress;
import org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage;
import org.protobee.examples.protos.Common.Header;
import org.protobee.network.ConnectionCreator;
import org.protobee.protocol.ProtocolModel;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.util.Modules;
import com.google.protobuf.ByteString;

public class BroadcastMain {

  private static void p(String s) {
    System.out.println(s);
  }

  private static void usage() {
    p("Arguments: <listening port>\n" + "<listening port> <connection address> ...");
  }

  public static void main(String[] args) throws IOException {

    if (args.length < 1) {
      usage();
      return;
    }

    final int localPort = Integer.parseInt(args[0]);

    InetSocketAddress[] others = new InetSocketAddress[args.length - 1];

    for (int i = 1; i < args.length; i++) {
      int col = args[i].indexOf(":");
      String ip = args[i].substring(0, col);
      int port = Integer.parseInt(args[i].substring(col + 1));
      InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
      others[i - 1] = inetSocketAddress;
    }

    Injector inj =
        Guice.createInjector(Modules
            .override(new ProtobeeGuiceModule(), new BroadcastGuiceModule()).with(
                new AbstractModule() {
                  @Override
                  protected void configure() {
                    bind(SocketAddress.class).annotatedWith(BroadcastListeningAddress.class)
                        .toInstance(new InetSocketAddress(localPort));
                  }
                }));

    ProtobeeServantBootstrapper bootstrapper = inj.getInstance(ProtobeeServantBootstrapper.class);
    bootstrapper.startup();

    ProtocolModel broadcastProtocol =
        inj.getInstance(Key.get(ProtocolModel.class, Broadcast.class));
    ConnectionCreator creator = inj.getInstance(ConnectionCreator.class);
    for (InetSocketAddress inetSocketAddress : others) {
      creator.connect(broadcastProtocol, inetSocketAddress, HttpMethod.valueOf("SAY"), "/");
    }

    sendMessages(inj.getInstance(BroadcastMessageWriter.class));

    bootstrapper.shutdown(1000);
  }

  private static void sendMessages(BroadcastMessageWriter writer) throws IOException {
    String line = ""; // Line read from standard in

    System.out.println("Enter a line of text (type 'quit' to exit): ");
    InputStreamReader converter = new InputStreamReader(System.in);
    BufferedReader in = new BufferedReader(converter);

    line = in.readLine();
    while (!(line.equals("quit"))) {

      Random random = new Random();
      byte[] messageId = new byte[16];
      random.nextBytes(messageId);
      BroadcastMessage message =
          BroadcastMessage
              .newBuilder()
              .setHeader(
                  Header.newBuilder().setHops(0).setId(ByteString.copyFrom(messageId)).setTtl(2))
              .setMessage(line).setSendTimeMillis(System.currentTimeMillis()).build();

      writer.broadcastMessage(message);

      line = in.readLine();
    }
  }

}
