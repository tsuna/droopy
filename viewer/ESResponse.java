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

import java.util.Iterator;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;

/**
 * JavaScript overlay for ElasticSearch's responses.
 */
public final class ESResponse<T extends JavaScriptObject> extends JavaScriptObject {

  protected ESResponse() {
  }

  public static <T extends JavaScriptObject> ESResponse<T> fromJson(final JSONObject json) {
    return json.getJavaScriptObject().cast();
  }
  public static native <T extends JavaScriptObject> ESResponse<T> fromJson(final String json) /*-{
    return eval('(' + json + ')');
  }-*/;

  public native int took() /*-{ return this.took }-*/;
  public native boolean timedOut() /*-{ return this.timed_out }-*/;
  //public static final native Shards hits() /*-{ return this._shards }-*/;
  public native Hits<T> hits() /*-{ return this.hits }-*/;
  public native <F extends Facet> Facets<F> facets(final String name)
  /*-{ return this.facets == null ? null : this.facets[name] }-*/;

  public static final class Hits<T extends JavaScriptObject> extends JavaScriptObject {
    protected Hits() {}
    public native int total() /*-{ return this.total }-*/;
    public native JsArray<Hit<T>> hits() /*-{ return this.hits }-*/;

    // Can't implement the Iterable interface due to GWT bug #4864.
    public Iterable<Hit<T>> iterator() {
      return new JsArrayIterator(hits());
    }
  }

  public static final class Hit<T extends JavaScriptObject> extends JavaScriptObject {
    public static <T extends JavaScriptObject> Hit<T> fromJson(final JSONObject json) {
      return json.getJavaScriptObject().cast();
    }

    protected Hit() {}
    public native String index() /*-{ return this._index }-*/;
    public native String type() /*-{ return this._type }-*/;
    public native String id() /*-{ return this._id }-*/;
    public native T source() /*-{ return this._source }-*/;
  }

  public static abstract class Facet extends JavaScriptObject {
    protected Facet() {}
  }

  public static final class Facets<T extends Facet> extends JavaScriptObject {
    protected Facets() {}
    public native int missing() /*-{ return this.missing }-*/;
    public native JsArray<T> terms() /*-{ return this.terms }-*/;

    // Can't implement the Iterable interface due to GWT bug #4864.
    public Iterable<T> iterator() {
      return new JsArrayIterator(terms());
    }
  }

  public static final class TermFacet extends Facet {
    protected TermFacet() {}
    public native String term() /*-{ return this.term }-*/;
    public native int count() /*-{ return this.count }-*/;
  }

}
