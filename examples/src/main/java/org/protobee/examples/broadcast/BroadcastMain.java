package org.protobee.examples.broadcast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.protobee.ProtobeeGuiceModule;
import org.protobee.ProtobeeServantBootstrapper;
import org.protobee.examples.broadcast.constants.BroadcastListeningAddress;
import org.protobee.examples.broadcast.modules.BroadcastMessageModule;
import org.protobee.examples.broadcast.modules.FeelingsInitiatorModule;
import org.protobee.examples.broadcast.modules.TimeBroadcastMessageModule;
import org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage;
import org.protobee.examples.protos.Common.Header;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;
import org.protobee.network.ConnectionCreator;
import org.protobee.protocol.ProtocolModel;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import com.google.protobuf.ByteString;

public class BroadcastMain {

  private static Options options = new Options();

  private static void usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("BroadcastMain.class [options] [listen port] [remote address]...", options);
  }

  public static void main(String[] args) throws IOException {
    options.addOption("no_time", false, "Flags the broadcaster to not use time");
    options.addOption("feelings", false, "Adds the feelings module");

    CommandLineParser parser = new PosixParser();
    final CommandLine line;
    try {
      line = parser.parse(options, args);
    } catch (ParseException exp) {
      System.err.println("Parsing failed.  Reason: " + exp.getMessage());
      usage();
      return;
    }
    String[] rest = line.getArgs();

    if (rest.length == 0) {
      usage();
      return;
    }

    final int localPort = Integer.parseInt(rest[0]);

    InetSocketAddress[] others = new InetSocketAddress[rest.length - 1];

    for (int i = 1; i < rest.length; i++) {
      int col = rest[i].indexOf(":");
      String ip = rest[i].substring(0, col);
      int port = Integer.parseInt(rest[i].substring(col + 1));
      InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
      others[i - 1] = inetSocketAddress;
    }

    AbstractModule optionModule = new AbstractModule() {
      @Override
      protected void configure() {}

      @SuppressWarnings("unused")
      @Provides
      @Broadcast
      @SessionScope
      public Set<ProtocolModule> getModules(Provider<BroadcastMessageModule> messageModule,
          Provider<TimeBroadcastMessageModule> timedModule, 
          Provider<FeelingsInitiatorModule> feelings) {
        Set<ProtocolModule> modules = Sets.newHashSet();
        modules.add(messageModule.get());
        if (!line.hasOption("no_time")) {
          modules.add(timedModule.get());
        }
        if (line.hasOption("feelings")) {
          modules.add(feelings.get());
        }
        return modules;
      }

      @SuppressWarnings("unused")
      @Provides
      @Broadcast
      @Singleton
      public Set<Class<? extends ProtocolModule>> getModuleClasses() {
        Set<Class<? extends ProtocolModule>> modules = Sets.newHashSet();
        modules.add(BroadcastMessageModule.class);
        if (!line.hasOption("no_time")) {
          modules.add(TimeBroadcastMessageModule.class);
        }
        if (line.hasOption("feelings")) {
          modules.add(FeelingsInitiatorModule.class);
        }
        return modules;
      }

    };

    Set<AbstractModule> overridingModules = Sets.newHashSet(optionModule, new AbstractModule() {
      @Override
      protected void configure() {
        bind(SocketAddress.class).annotatedWith(BroadcastListeningAddress.class).toInstance(
            new InetSocketAddress(localPort));
      }
    });

    Injector inj =
        Guice.createInjector(Modules
            .override(new ProtobeeGuiceModule(), new BroadcastGuiceModule())
            .with(overridingModules));

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
      BroadcastMessage.Builder message =
          BroadcastMessage
              .newBuilder()
              .setHeader(
                  Header.newBuilder().setHops(0).setId(ByteString.copyFrom(messageId)).setTtl(2))
              .setMessage(line);

      writer.broadcastMessage(message);

      line = in.readLine();
    }
  }

}
