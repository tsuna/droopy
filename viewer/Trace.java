// This file is part of Droopy.
// Copyright (C) 2011  Benoit Sigoure.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// General Public License for more details.  You should have received a copy
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.
package viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HasTreeItems;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * JavaScript overlay for Droopy traces.
 */
public final class Trace extends JavaScriptObject {

  protected Trace() {
  }

  public native int reqSize() /*-{ return this.req_size }-*/;
  public native int respSize() /*-{ return this.resp_size }-*/;
  public native int numCliRead() /*-{ return this.num_cli_read }-*/;
  public native double readTime() /*-{ return this.read_time }-*/;
  public native int numCliWrite() /*-{ return this.num_cli_write }-*/;
  public native double writeTime() /*-{ return this.write_time }-*/;
  public native JsArray<SyscallTime> syscallsTimes() /*-{ return this.syscalls_times }-*/;
  public native JsArray<BackendReqs.JSO> backendReqs() /*-{ return this.backend_reqs }-*/;

  private native void sortSyscallTimes() /*-{
    this.syscalls_times.sort(function(a, b) { return b.time - a.time })
  }-*/;

  public ArrayList<BackendReqs> backendRequests() {
    final JsArray<BackendReqs.JSO> jsos = backendReqs();
    if (jsos == null) {
      return null;
    }
    final ArrayList<BackendReqs> be_reqs = new ArrayList<BackendReqs>(jsos.length());
    for (final BackendReqs.JSO jso : JsArrayIterator.iter(jsos)) {
      be_reqs.add(new BackendReqs(jso));
    }
    return be_reqs;
  }

  /** Returns the widget associated with this trace.  */
  public Widget widget(final Summary summary) {
    return new TraceWidget(summary);
  }

  private static final NumberFormat FMT = NumberFormat.getFormat("0.00");
  private static final NumberFormat PCT = NumberFormat.getFormat("0.0%");

  /** Format a timing in a human readable fashion.  */
  private static String fmt(final double timing) {
    return FMT.format(timing) + "ms";
  }

  /** Format a percentage in a human readable fashion.  */
  private static String percent(final double part, final double total) {
    if (total == 0) {
      return "0%";
    }
    return PCT.format(part / total);
  }

  private static String plural(final int n, final String text) {
    return n + " " + (n > 1 ? text + 's' : text);
  }

  private static String pluralnz(final int n, final String text) {
    return n == 0 ? "" : plural(n, text);
  }

  private final class TraceWidget extends AlignedTree {
    TraceWidget(final Summary summary) {
      super.setWidth("100%");
      super.addItem(row("Request time: " + summary.readableTime()));
      if (readTime() > 0.1) {
        super.addRow("Request size: ", reqSize() + " bytes",
                     "read in " + plural(numCliRead(), "chunk"),
                     "in " + fmt(readTime()));
      } else {
        super.addRow("Request size: ", reqSize() + " bytes");
      }
      if (writeTime() > 0.1) {
        super.addRow("Response size: ", respSize() + " bytes",
                     "written in " + plural(numCliWrite(), "chunk"),
                     "in " + fmt(writeTime()));
      } else {
        super.addRow("Response size: ", respSize() + " bytes");
      }

      // Time spent doing system calls.
      {
        // First compute the total amount of time spent doing system calls.
        double syscalls_times = 0;
        sortSyscallTimes();  // Make sure we sort them by time spent.
        for (final SyscallTime call : JsArrayIterator.iter(syscallsTimes())) {
          syscalls_times += call.time();
        }
        // Do another loop and find the top N system calls.
        double cumul_time = 0;  // How much cumulative time we have so far.
        final TreeItem timings = new TreeItem();  // The important calls.
        final TreeItem negligible_timings = new TreeItem();  // The long tail.
        double negligible_time = 0;  // How much time spent in the long tail.
        for (final SyscallTime call : JsArrayIterator.iter(syscallsTimes())) {
          // Make sure we grab at least the top 3 calls by time spent,
          // and then consider any of the top N% calls to be important.
          final TreeItem chosen;
          if (timings.getChildCount() > 3
              && cumul_time / syscalls_times > 0.8) {
            chosen = negligible_timings;
            negligible_time += call.time();
          } else {
            chosen = timings;
            cumul_time += call.time();
          }
          chosen.addItem(row(call.name(), fmt(call.time()), pluralnz(call.count(), "call")));
        }
        negligible_timings.setWidget(row(plural(negligible_timings.getChildCount(),
                                                "negligible syscall"), fmt(negligible_time)));
        timings.addItem(negligible_timings);
        timings.setWidget(row("Time spent executing system calls:",
                              fmt(syscalls_times),
                              plural(summary.numSyscalls(), "call"),
                              percent(syscalls_times, summary.endToEnd())
                              + " of total time"));
        super.addItem(timings);
      }

      // Slowest system call.
      {
        final Syscall slowest = summary.slowestSyscall();
        final TreeItem tree = new TreeItem();
        tree.setWidget(row("Slowest syscall: " + slowest.name(),
                           fmt(slowest.duration())));
        tree.addItem(new FixedWidth(slowest.call()));
        if (summary.hasSlowBackend()) {
          final ConnectCall connect = summary.prevConnect();
          final TreeItem item = new TreeItem(row("This FD is connected to",
                                                 summary.slowestBackend()));
          if (connect.call() != null) {
            item.addItem(new FixedWidth(connect.call()));
          }
          tree.addItem(item);
        }
        if (summary.hasPrevSlowest()) {
          final Syscall prev = summary.prevSlowest();
          tree.addItem(row("Previous call on that FD", fmt(prev.duration())));
          tree.addItem(new FixedWidth(prev.call()));
        }
        super.addItem(tree);
      }

      // Backend calls.
      {
        final ArrayList<BackendReqs> be_reqs = backendRequests();
        if (be_reqs != null) {
          Collections.sort(be_reqs);
          int num_be_calls = 0;
          double total_time = 0;
          for (final BackendReqs reqs : be_reqs) {
            num_be_calls += reqs.calls().length();
            total_time += reqs.totalTime();
          }

          final TreeItem important = new TreeItem();
          important.setWidget(row("Time spent interacting with "
                                  + plural(be_reqs.size(), "backend"),
                                  fmt(total_time),
                                  plural(num_be_calls, "call"),
                                  percent(total_time, summary.endToEnd())
                                  + " of total time"));
          final TreeItem negligible = new TreeItem();
          double cumul_time = 0;  // How much cumulative time we have so far.
          double negligible_time = 0;  // How much time spent in the long tail.
          int negligible_calls = 0;
          for (final BackendReqs reqs : be_reqs) {
            // Make sure we grab at least the top 3 calls by time spent,
            // and then consider any of the top N% calls to be important.
            final TreeItem chosen;
            if (important.getChildCount() > 3
                && cumul_time / total_time > 0.8) {
              chosen = negligible;
              negligible_time += reqs.totalTime();
              negligible_calls += reqs.calls().length();
            } else {
              chosen = important;
              cumul_time += reqs.totalTime();
            }
            final TreeItem be = new LazyTreeItem(row(reqs.peer(), fmt(reqs.totalTime()),
                                                     plural(reqs.calls().length(), "call"))) {
              protected void onFirstOpen() {
                removeItems();
                for (final Syscall req : reqs) {
                  final HBox call = row(fmt(req.duration()));
                  call.add(new FixedWidth(" " + req.call()));
                  addItem(call);
                }
                deferredAlign(this);
              }

              protected void onOpen() {
              }

              protected void onClose() {
              }
            };
            chosen.addItem(be);
          }
          negligible.setWidget(row(plural(negligible.getChildCount(),
                                          "negligible backend"), fmt(negligible_time),
                                   plural(negligible_calls, "call")));
          important.addItem(negligible);
          super.addItem(important);
        }
      }
    }

    protected void doAttachChildren() {
      super.doAttachChildren();
      deferredAlign(this);
    }

  }

  private static void deferredAlign(final HasTreeItems tree) {
    // We need to align the tree outside of the processing of this event,
    // otherwise the browser won't have actually computed the layout of the
    // Tree, and the nested widgets will appear to have a size of 0.
    // It's kind of ugly because it means the browser does the layout once,
    // then we "fix" it by aligning widgets, so users can see the layout
    // flicker a bit after we "fix" it, but I don't know of a better way...
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      public void execute() {
        AlignedTree.align(tree);
      }
    });
  }

}

final class SyscallTime extends JavaScriptObject {

  protected SyscallTime() {
  }

  public native String name() /*-{ return this.name }-*/;
  public native double time() /*-{ return this.time }-*/;
  // This field was added later, so it might not always be present.
  public native int count() /*-{ return this.hasOwnProperty("count") && this.count || 0 }-*/;

}

final class BackendReqs implements Comparable<BackendReqs>, Iterable<Syscall> {

  private final double total_time;
  private final JSO jso;

  public BackendReqs(final JSO jso) {
    this.jso = jso;
    double t = 0;
    for (final Syscall call : JsArrayIterator.iter(calls())) {
      t += call.duration();
    }
    this.total_time = t;
  }

  // Needs to be wrapped because we can't add extra members into JSOs.
  static final class JSO extends JavaScriptObject {

    protected JSO() {
    }

    native String peer() /*-{ return this.peer }-*/;
    native JsArray<Syscall> calls() /*-{ return this.calls }-*/;

  }

  public String peer() {
    return jso.peer();
  }

  public JsArray<Syscall> calls() {
    return jso.calls();
  }

  public double totalTime() {
    return total_time;
  }

  public Iterator<Syscall> iterator() {
    return JsArrayIterator.iter(calls());
  }

  /** Compares in descending order of total time spent with this backend.  */
  public int compareTo(final BackendReqs other) {
    return Double.compare(other.total_time, total_time);
  }

}
