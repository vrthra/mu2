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
  static XJSONArray xjsonObject;
  static JSONArray jsonObject;
  static int MaxSeconds = 5;
  static final ExecutorService executor = Executors.newSingleThreadExecutor();

  public static int testJSON(String str) throws Exception {
    jsonObject = null;
    xjsonObject = null;
    final Runnable xJsonTask = new Thread() {
      @Override
      public void run() { xjsonObject = new XJSONArray(str); }
    };
    final Runnable jsonTask = new Thread() {
      @Override
      public void run() { jsonObject = new JSONArray(str); }
    };

    final Future xjsonFuture = executor.submit(xJsonTask);
    final Future jsonFuture = executor.submit(jsonTask);
    // xjson should not throw exceptions!
    xjsonFuture.get(MaxSeconds, TimeUnit.SECONDS);
    try {
      jsonFuture.get(5, TimeUnit.SECONDS);
      return jsonObject.toString().equals(xjsonObject.toString()) == true ? 1 : 0;
    } catch (TimeoutException te) {
      return -2;
    } catch (Exception ex) {
      Throwable th = ex.getCause();
      if (th instanceof JSONException) {
        return -1;
      } else if (th instanceof NullPointerException) {
        return -1;
      } else {
        th.printStackTrace();
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
      System.out.println(args[1]);
      while ((line = bufferedReader.readLine()) != null) {
        System.out.println(line);
        int testJsonResult = 0;
        testJsonResult = testJSON("[" + line + "]");
        if (testJsonResult == 1) {
          System.out.println("1");
        } else if (testJsonResult == -2) {
          System.out.println("t");
        } else if (testJsonResult == -1) {
          System.out.println("-1");
        } else if (testJsonResult == 0) {
          System.out.println("0");
        } else {
          throw new Exception(line + " " + testJsonResult);
        }
        count++;
      }
      fileReader.close();
      if (!executor.isTerminated()) executor.shutdownNow();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
