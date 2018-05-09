package org.json;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class JSONFilter {
  static String xJsonStr;
  static int MaxSeconds = 5;
  static final ExecutorService executor = Executors.newSingleThreadExecutor();

  public static int testJSON(String str) throws Exception {
    xJsonStr = null;
    final Runnable xJsonTask = new Thread() {
      @Override
      public void run() { xJsonStr = XJSON.testThis(str); }
    };
    final Future xjsonFuture = executor.submit(xJsonTask);
    try {
      xjsonFuture.get(MaxSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException te) {
      return -1;
    } catch (Exception ex) {
      return -2;
    }
    return 0;
  }

  public static void main(String[] args) {
    try {
      Scanner in = new Scanner(System.in);
      StringBuffer stringBuffer = new StringBuffer();
      String line;
      while (in.hasNext()) {
        line = in.nextLine();
        int testJsonResult = testJSON(line);
        if (testJsonResult == 0) {
          System.out.println(line);
        } else {
          System.err.println(line);
        }
      }
      if (!executor.isTerminated()) executor.shutdownNow();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
