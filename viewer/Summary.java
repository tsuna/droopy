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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * JavaScript overlay for Droopy trace summaries.
 */
public final class Summary extends JavaScriptObject {

  protected Summary() {
  }

  public native String method() /*-{ return this.method }-*/;
  public native String resource() /*-{ return this.resource }-*/;
  public native double endToEnd() /*-{ return this.end_to_end }-*/;
  public native int numSyscalls() /*-{ return this.num_syscalls }-*/;

  public Widget widget() {
    return new SummaryWidget();
  }

  private final class SummaryWidget extends InlineLabel {
    SummaryWidget() {
      super(method() + ' ' + resource() + " in " + endToEnd() + "ms");
    }
  }

}
