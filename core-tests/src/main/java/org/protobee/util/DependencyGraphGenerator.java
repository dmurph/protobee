package org.protobee.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;


public class DependencyGraphGenerator {

  public final static Injector graphGood(String filename, Injector inj) {
    return graphGood(filename, inj, null);
  }

  public final static Injector graphGood(String filename, Injector inj, Set<String> exclusionClasses) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(baos);

      Injector injector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
      GraphvizRenderer renderer = injector.getInstance(GraphvizRenderer.class);
      renderer.setOut(out).setRankdir("TB");

      injector.getInstance(InjectorGrapher.class).of(inj).graph();

      out = new PrintWriter(new File(filename + "-unexluded.dot"), "UTF-8");
      String original = baos.toString("UTF-8");
      String s = fixGrapherBug(original);
      s = hideClassPaths(s);
      s = removeArguments(s);
      out.write(s);
      out.close();

      if (exclusionClasses != null && exclusionClasses.size() != 0) {
        out = new PrintWriter(new File(filename + "-excluded.dot"), "UTF-8");
        s = fixGrapherBug(original);
        s = hideClassPaths(s);
        s = removeArguments(s);
        for (String string : exclusionClasses) {
          s = removeClass(s, string);
        }
        out.write(s);
        out.close();
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return inj;
  }

  public static String removeClass(String s, String klass) {
    int index = s.lastIndexOf(">" + klass + "<");
    int lineStart = s.lastIndexOf("\n", index);
    int lineEnd = s.indexOf("\n", index);
//    if(s.indexOf(">@Me<", lineStart) < lineEnd) {
//      index = s.indexOf(">" + klass + "<", lineEnd);
//      lineStart = s.lastIndexOf("\n", index);
//      lineEnd = s.indexOf("\n", index);
//    }
    String key = s.substring(lineStart + 1, s.indexOf(' ', lineStart));
    s = s.substring(0, lineStart) + s.substring(lineEnd);
    s = s.replaceAll(".*" + key + ".*", "\n");
    return s;
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

  public static String removeArguments(String s) {
    return s.replaceAll("\\(([a-zA-Z]+, )*([a-zA-Z]+)\\)", "(...)");
  }
}
