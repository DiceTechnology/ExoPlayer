package com.google.android.exoplayer2.endeavor;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.LoadEventInfo;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

public class DebugUtil extends DebugBase {

  public DebugUtil(int sz) {
    super(enable, sz);
  }

  public DebugUtil(boolean flag, int sz) {
    super(flag, sz);
  }

  public static void debug(String tag, Timeline.Period period) {
    if (enable) {
      DebugUtil util = new DebugUtil(128);
      util.line("period, pid ").add(period.id);
      util.add(", uid ").add(period.uid);
      util.add(", windowIndex ").add(period.windowIndex);
      util.add(", windowUs ").add(period.getPositionInWindowUs());
      util.add(", durUs ").dd(tag, period.durationUs);
    }
  }

  public static void debug(String tag, Timeline.Window window) {
    if (enable) {
      DebugUtil util = new DebugUtil(128);
      util.line("window, presMs ").clock(window.presentationStartTimeMs);
      util.add(", periodIds (").add(window.firstPeriodIndex).add(", ").add(window.lastPeriodIndex).add(')');
      util.add(", periodUs ").add(window.positionInFirstPeriodUs);
      util.add(", durUs ").add(window.durationUs);
      util.add(", defPosUs ").add(window.defaultPositionUs);
      util.add(", startWallMs ").dd(tag, window.windowStartTimeMs);
    }
  }

  public static StringBuilder load(LoadEventInfo loadEventInfo, StringBuilder builder) {
    return load(false, loadEventInfo, builder);
  }

  public static StringBuilder load(boolean withUri, LoadEventInfo loadEventInfo, StringBuilder builder) {
    builder.append("[").append(loadEventInfo.bytesLoaded / 1000f).append(" kb, ");
    builder.append(WebUtil.ms2s(loadEventInfo.loadDurationMs));
    if (loadEventInfo.loadDurationMs > 2000) {
      builder.append("!!!");
    }
    builder.append(" s]");
    if (withUri) {
      builder.append("\n").append(loadEventInfo.uri.toString());
    }
    return builder;
  }

  public boolean enable() {
    return (builder != null);
  }

  @Override
  public DebugUtil line(String str) {
    super.line(str);
    return this;
  }

  @Override
  public DebugUtil clock(long time) {
    super.clock(time);
    return this;
  }

  @Override
  public DebugUtil dur(long dur) {
    super.dur(dur);
    return this;
  }

  public DebugUtil discont(int reason) {
    if (enable()) {
      switch (reason) {
        case Player.DISCONTINUITY_REASON_PERIOD_TRANSITION:
          builder.append(", discont period>").append(reason);
          break;
        case Player.DISCONTINUITY_REASON_SEEK:
          builder.append(", discont seek>").append(reason);
          break;
        case Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT:
          builder.append(", discont seek2>").append(reason);
          break;
        case Player.DISCONTINUITY_REASON_AD_INSERTION:
          builder.append(", discont ad>").append(reason);
          break;
        case Player.DISCONTINUITY_REASON_INTERNAL:
          builder.append(", discont internal>").append(reason);
          break;
        default:
          builder.append(", discont unknown>").append(reason);
          break;
      }
    }
    return this;
  }

  public DebugUtil windchg(int reason) {
    if (enable()) {
      switch (reason) {
        case Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE:
          builder.append(", windchg source update:").append(reason);
          break;
        case Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED:
          builder.append(", windchg playlist changed:").append(reason);
          break;
        default:
          builder.append(", windchg unknown:").append(reason);
          break;
      }
    }
    return this;
  }

  public DebugUtil state(int state) {
    if (enable()) {
      switch (state) {
        case Player.STATE_IDLE:
          builder.append(", state idle:").append(state);
          break;
        case Player.STATE_BUFFERING:
          builder.append(", state buffer:").append(state);
          break;
        case Player.STATE_READY:
          builder.append(", state ready:").append(state);
          break;
        case Player.STATE_ENDED:
          builder.append(", state ended:").append(state);
          break;
        default:
          builder.append(", state unknown:").append(state);
          break;
      }
    }
    return this;
  }

  public DebugUtil track(int type) {
    if (enable()) {
      switch (type) {
        case C.TRACK_TYPE_AUDIO:
          builder.append(", track audio:").append(type);
          break;
        case C.TRACK_TYPE_VIDEO:
          builder.append(", track video:").append(type);
          break;
        case C.TRACK_TYPE_TEXT:
          builder.append(", track text:").append(type);
          break;
        case C.TRACK_TYPE_METADATA:
          builder.append(", track meta:").append(type);
          break;
        default:
          builder.append(", track other:").append(type);
          break;
      }
    }
    return this;
  }

  public DebugUtil read(int result) {
    if (enable()) {
      switch (result) {
        case C.RESULT_END_OF_INPUT:
          builder.append(", read ended:").append(result);
          break;
        case C.RESULT_MAX_LENGTH_EXCEEDED:
          builder.append(", read exceed:").append(result);
          break;
        case C.RESULT_NOTHING_READ:
          builder.append(", read nothing:").append(result);
          break;
        case C.RESULT_BUFFER_READ:
          builder.append(", read buffer:").append(result);
          break;
        case C.RESULT_FORMAT_READ:
          builder.append(", read format:").append(result);
          break;
        default:
          builder.append(", read unknown:").append(result);
          break;
      }
    }
    return this;
  }

  public DebugUtil buffer(int type) {
    if (enable()) {
      switch (type) {
        case C.BUFFER_FLAG_KEY_FRAME:
          builder.append(", buffer keyframe:").append(type);
          break;
        case C.BUFFER_FLAG_END_OF_STREAM:
          builder.append(", buffer endofstream:").append(type);
          break;
        case C.BUFFER_FLAG_LAST_SAMPLE:
          builder.append(", buffer lastsample:").append(type);
          break;
        case C.BUFFER_FLAG_ENCRYPTED:
          builder.append(", buffer encrypted:").append(type);
          break;
        case C.BUFFER_FLAG_DECODE_ONLY:
          builder.append(", buffer decodeonly:").append(type);
          break;
        default:
          builder.append(", buffer unknown:").append(type);
          break;
      }
    }
    return this;
  }

  @Override
  public DebugUtil add(Object obj) {
    super.add(obj);
    return this;
  }
}
