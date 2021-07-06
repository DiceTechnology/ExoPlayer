package com.google.android.exoplayer2.endeavor;

import android.net.Uri;
import com.google.android.exoplayer2.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class DebugBase {

  private static final String TAG = "====DebugBase====";

  public static boolean enable = false; // BuildConfig.DEBUG;
  public static boolean debug_drm = false;
  public static boolean debug_ad = false;
  public static boolean debug_track = false;
  public static boolean debug_extractor = false;
  public static boolean debug_thumbnail = false;
  public static boolean debug_media = false;
  public static boolean debug_manifest = true;
  public static boolean debug_lowlatency = true;
  public static boolean debug_metadata_feature = false; // plist data with vcid

  public static boolean use_info = false;
  public static int debug_sample_write = 0b0000; // extract and write to sample queue, audio 0b1, video 0b10, text 0b100, metadata 0b1000
  public static int debug_sample_read = 0b0000; // read from sample queue to decoder, audio 0b1, video 0b10, text 0b100, metadata 0b1000
  public static int debug_render = 0b0000; // read from decoder to surface, audio 0b1, video 0b10, text 0b100, metadata 0b1000
  public static String upload_server = null; // "http://172.16.2.43:4660/file/manifest/"; // 172.16.0.63:4680, 127.0.0.1:4680, 10.0.2.2:4660

  protected static AtomicLong queueUid = new AtomicLong(0);
  protected static String uploadsUid = null;
  protected static int[] uploadsCfg = new int[] {0, 16, 9, 500000, 0, 0}; // position, tab size, line size, builder size, records.N.txt, current line count
  protected static StringBuilder uploadsBuilder;
  protected static List<String> uploadsList = new ArrayList<String>(uploadsCfg[2] + 2);

  protected StringBuilder builder;

  public DebugBase(int sz) {
    this(enable, sz);
  }

  public DebugBase(boolean flag, int sz) {
    if (flag) {
      builder = new StringBuilder(sz < 10 ? 10 : sz);
    }
  }

  public static boolean debugSample() {
    return (debug_sample_write > 0 || debug_sample_read > 0 || debug_render > 0);
  }

  public static boolean debugSampleWrite(int trackType) {
    return debug(trackType, debug_sample_write);
  }

  public static boolean debugSampleRead(int trackType) {
    return debug(trackType, debug_sample_read);
  }

  public static boolean debugRender(int trackType) {
    return debug(trackType, debug_render);
  }

  private static boolean debug(int trackType, int flag) {
    return (trackType > 0 ? (flag & (0x1 << (trackType - 1))) != 0 : flag > 0);
  }

  public static void initUploadsUid() {
    if (!WebUtil.empty(upload_server)) {
      uploadsUid = Integer.toHexString((int) (Math.random() * 1000000) + 1) + "_" + WebUtil.time(0);
      initRecord();
    }
  }

  public static String manifestSaveUrl(Uri uri) {
    if (WebUtil.empty(upload_server) || uri == null) {
      return null;
    }
    initRecord();
    String url = uri.toString(), ext = "txt";
    int pos1 = url.indexOf("?");
    pos1 = (pos1 == -1 ? url.length() : pos1);
    int pos2 = url.lastIndexOf("/", pos1), pos3 = url.lastIndexOf(".", pos1);
    String origin = url, name = (pos2 > 0 ? url.substring(pos2 + 1, pos3 > pos2 ? pos3 : pos1) + "_" : "");
    if (url.contains(".mpd")) {
      ext = "mpd";
    } else if (url.contains(".m3u8")) {
      ext = "m3u8";
    }
    if (!ext.equals("txt")) {
      pos3 = origin.lastIndexOf("." + ext, pos1);
      name = (pos2 > 0 ? origin.substring(pos2 + 1, pos3 > pos2 ? pos3 : pos1) + "_" : "");
    }
    String prefix = upload_server + uploadsUid + "/";
    String time = WebUtil.time(0), ret = prefix + name + time + "." + ext;
    record(false, time, origin, ret);
    return ret;
  }

  public static String mediaSaveUrl(int trackType, Uri uri) {
    if (WebUtil.empty(upload_server) || uri == null) {
      return null;
    }
    initRecord();
    String url = uri.toString();
    int pos1 = url.indexOf("?");
    pos1 = (pos1 == -1 ? url.length() : pos1);
    int pos2 = url.lastIndexOf("/", pos1);
    if (pos2 < 1) {
      return null;
    }
    String path = upload_server.substring(0, uploadsCfg[0]) + "media/" + uploadsUid + "/", name = url.substring(pos2 + 1, pos1);
    if (pos1 < url.length()) {
      name += '_' + url.substring(pos1 + 1);
    }
    String ret = path + (trackType < 0 ? "m_" : trackType + "_") + Integer.toHexString(url.substring(0, pos2).hashCode()) + "_" + name;
    record(true, WebUtil.time(0), url, ret);
    return ret;
  }

  private static void record(boolean forceUpdate, String time, String origin, String url) {
    int pos = url.indexOf("/", uploadsCfg[0]);
    String uri = url.substring(pos + 1), line = time + "  " + uri;
    int sz = Math.max(1, uploadsCfg[1] - line.length() / 8);
    for (int i = 0; i < sz; i++) {
      line += "\t";
    }
    line += origin;
    synchronized (uploadsList) {
      uploadsList.add(line);
      boolean upload = ++uploadsCfg[5] >= uploadsCfg[2];
      if (forceUpdate || upload) {
        if (uploadsBuilder == null || uploadsBuilder.length() > uploadsCfg[3]) {
          uploadsBuilder = new StringBuilder((int) (uploadsCfg[3] * 1.2));
          uploadsCfg[4]++;
        }
        for (String str : uploadsList) {
          uploadsBuilder.append(str).append("\n");
        }
        if (upload) {
          uploadsBuilder.append("\n");
          uploadsCfg[5] = 0;
        }
        uploadsList.clear();
        forceUpdate = true;
      }
    }
    if (forceUpdate) {
      WebUtil.asyncPost(upload_server.substring(0, uploadsCfg[0]) + uploadsUid + "_records_" + uploadsCfg[4] + ".txt", uploadsBuilder.toString());
    }
  }

  private static void initRecord() {
    if (uploadsUid == null) {
      initUploadsUid();
    }
    if (uploadsCfg[0] < 1) {
      int pos = (WebUtil.empty(upload_server) ? 1 : upload_server.indexOf("/file/"));
      if (pos > 0) {
        uploadsCfg[0] = pos + 6;
      }
    }
  }

  public static long queueUid() {
    return queueUid.incrementAndGet();
  }

  public boolean enable() {
    return (builder != null);
  }

  public DebugBase line(String str) {
    if (enable()) {
      builder.append('\n').append(str);
    }
    return this;
  }

  public DebugBase clock(long time) {
    if (enable()) {
      if (time < 0) {
        time = 0;
        builder.append('*');
      }
      builder.append(time).append('(').append(WebUtil.time(time)).append(')');
    }
    return this;
  }

  public DebugBase dur(long dur) {
    if (enable()) {
      if (dur < 0) {
        dur = 0;
        builder.append('*');
      }
      builder.append(dur);
    }
    return this;
  }

  public DebugBase add(Object obj) {
    if (enable()) {
      builder.append(obj);
    }
    return this;
  }

  public void reset() {
    if (enable()) {
      builder.setLength(0);
    }
  }

  public void d(String tag) {
    if (enable()) {
      tag = (WebUtil.empty(tag) ? TAG : tag);
      int size = builder.length(), limit = 4000;
      for (int i = 0; i < size; ) {
        int next = (size - i < limit ? size : builder.lastIndexOf("\n", i + limit));
        if (next <= i) {
          next = size;
        }
        if (use_info) {
          Log.i(tag, builder.substring(i, Math.min(next, size)));
        } else {
          Log.d(tag, builder.substring(i, Math.min(next, size)));
        }
        i = next;
      }
      reset();
    }
  }

  public void dd(String tag, Object obj) {
    add(obj);
    d(tag);
  }
}
