package edu.cornell.jnutella.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.concurrent.Executors;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;

import edu.cornell.jnutella.guice.JnutellaMainModule;
import edu.cornell.jnutella.guice.netty.ExecutorModule;

public class DependencyGraphGenerator {
  public static void main(String[] args) {
    graphGood(
        "depGraph",
        Guice.createInjector(new JnutellaMainModule(),
            new ExecutorModule(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())));
  }

  public final static Injector graphGood(String filename, Injector inj) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(baos);

      Injector injector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
      GraphvizRenderer renderer = injector.getInstance(GraphvizRenderer.class);
      renderer.setOut(out).setRankdir("TB");

      injector.getInstance(InjectorGrapher.class).of(inj).graph();

      out = new PrintWriter(new File(filename + new Date().getTime() + ".dot"), "UTF-8");
      String s = baos.toString("UTF-8");
      s = fixGrapherBug(s);
      s = hideClassPaths(s);
      out.write(s);
      out.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return inj;
  }

  public static String hideClassPaths(String s) {
    s = s.replaceAll("\\w[a-z\\d_\\.]+\\.([A-Z][A-Za-z\\d_]*)", "");
    s = s.replaceAll("value=[\\w-]+", "random");
    return s;
  }

  public static String fixGrapherBug(String s) {
    s = s.replaceAll("style=invis", "style=solid");
    return s;
  }
}
