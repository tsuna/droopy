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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
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

  private native void sortSyscallTimes() /*-{
    this.syscalls_times.sort(function(a, b) { return b.time - a.time })
  }-*/;

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
        tree.setWidget(row("Slowest syscall:", slowest.name(), fmt(slowest.duration())));
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
    }

    protected void doAttachChildren() {
      super.doAttachChildren();
      // We need to align the tree outside of the processing of this event,
      // otherwise the browser won't have actually computed the layout of the
      // Tree, and the nested widgets will appear to have a size of 0.
      // It's kind of ugly because it means the browser does the layout once,
      // then we "fix" it by aligning widgets, so users can see the layout
      // flicker a bit after we "fix" it, but I don't know of a better way...
      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        public void execute() {
          align();
        }
      });
    }
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
