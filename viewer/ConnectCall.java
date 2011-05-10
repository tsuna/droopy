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

/**
 * JavaScript overlay for calls to the syscall `connect'.
 */
public final class ConnectCall extends JavaScriptObject {

  protected ConnectCall() {
  }

  /**
   * Whether this FD was connected to the client.
   * If this returns true, other fields aren't set.
   */
  public native boolean client() /*-{ return this.hasOwnProperty("client") && this.client }-*/;

  public native String peer() /*-{ return this.peer }-*/;
  // This field was added later, so it might be undefined (null).
  public native String host() /*-{ return this.host }-*/;
  // This field was added later, so it might be undefined (null).
  public native String type() /*-{ return this.type }-*/;
  public native int retv() /*-{ return this.retv }-*/;
  public native String call() /*-{ return this.call }-*/;
  public native int timestamp() /*-{ return this.timestamp }-*/;

}
