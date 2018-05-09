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

public class JSONTest {
  static String xjson;
  static String json;
  static int MaxSeconds = 1;
  static final ExecutorService executor = Executors.newSingleThreadExecutor();

  public static int testJSON(String str) throws Exception {
    xjson = XJSON.testThis(str);
    json = null;
    final Runnable jsonTask = new Thread() {
      @Override
      public void run() { json = JSON.testThis(str); }
    };
    final Future jsonFuture = executor.submit(jsonTask);
    try {
      jsonFuture.get(MaxSeconds, TimeUnit.SECONDS);
      if (json == null) return -1;

      return json.equals(xjson) ? 1 : 0;
    } catch (TimeoutException te) {
      return -2;
    } catch (Exception ex) {
      Throwable th = ex.getCause();
      if (th instanceof JSONException) {
        return -1;
      } else if (th instanceof NullPointerException) {
        return -1;
      } else {
        ex.printStackTrace();
        throw new Exception(th);
      }
    }
  }

  public static void main(String[] args) {
    try {
      File file = new File(args[0]);
      FileReader fileReader = new FileReader(file);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      StringBuffer stringBuffer = new StringBuffer();
      String line;
      int count = 0;
      try{
        while ((line = bufferedReader.readLine()) != null) {
          System.err.println(">\t" + line);
          int testJsonResult = 0;
          testJsonResult = testJSON(line);
          if (testJsonResult == 1) {
            System.out.println("1");
          } else if (testJsonResult == -2) {
            System.out.println("t");
          } else if (testJsonResult == -1) {
            System.out.println("-1");
          } else if (testJsonResult == -3) {
            System.out.println("?");
          } else if (testJsonResult == 0) {
            System.out.println("0");
          } else {
            throw new Exception(line + " " + testJsonResult);
          }
          count++;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      fileReader.close();
      if (!executor.isTerminated()) executor.shutdownNow();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
