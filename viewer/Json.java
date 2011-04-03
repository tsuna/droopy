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

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public final class Json {

  private final JSONValue root;  // Either a JSONObject or a JSONArray.

  /** Private ctor, must use factory methods instead.  */
  private Json(final JSONValue root) {
    this.root = root;
  }

  public static Json object() {
    return new Json(new JSONObject());
  }

  public static Json object(final String field, final String value) {
    return object().add(field, value);
  }

  public static Json object(final String field, final long value) {
    return object().add(field, value);
  }

  public static Json object(final String field, final double value) {
    return object().add(field, value);
  }

  public static Json object(final String field, final Json value) {
    return object().add(field, value.root);
  }

  public static Json array() {
    return new Json(new JSONArray());
  }

  private Json add(final String field, final JSONValue value) {
    JSONObject obj;
    if ((obj = root.isObject()) != null) {
      obj.put(field, value);
    } else {
      final JSONArray array = root.isArray();
      obj = new JSONObject();
      obj.put(field, value);
      array.set(array.size(), obj);
    }
    return this;
  }

  public Json add(final String field, final Json value) {
    return add(field, value.root);
  }

  public Json add(final String field, final long value) {
    return add(field, new JSONNumber(value));
  }

  public Json add(final String field, final double value) {
    return add(field, new JSONNumber(value));
  }

  public Json add(final String field, final String value) {
    return add(field, new JSONString(value));
  }

  public String toString() {
    return root.toString();
  }

}
