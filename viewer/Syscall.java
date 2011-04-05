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
 * JavaScript overlay to represent syscalls.
 */
public final class Syscall extends JavaScriptObject {

  protected Syscall() {
  }

  public native double duration() /*-{ return this.duration }-*/;
  public native String name() /*-{ return this.name }-*/;
  public native int retv() /*-{ return this.retv }-*/;
  public native String call() /*-{ return this.call }-*/;
  public native int timestamp() /*-{ return this.timestamp }-*/;

}
