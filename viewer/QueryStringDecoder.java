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

// I originally wrote this code for Netty.  Surprisingly, GWT has nothing to
// manually parse query string parameters...

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.http.client.URL;

/**
 * Splits an HTTP query string into a path string and key-value parameter pairs.
 */
public final class QueryStringDecoder {

  /**
   * Returns the decoded key-value parameter pairs of the URI.
   */
  public static Map<String, List<String>> getParameters(final String s) {
    final Map<String, List<String>> params = new HashMap<String, List<String>>();
    String name = null;
    int pos = 0; // Beginning of the unprocessed region
    int i;       // End of the unprocessed region
    char c = 0;  // Current character
    for (i = 0; i < s.length(); i++) {
      c = s.charAt(i);
      if (c == '=' && name == null) {
        if (pos != i) {
          name = URL.decodeComponent(s.substring(pos, i));
        }
        pos = i + 1;
      } else if (c == '&') {
        if (name == null && pos != i) {
          // We haven't seen an `=' so far but moved forward.
          // Must be a param of the form '&a&' so add it with
          // an empty value.
          addParam(params, URL.decodeComponent(s.substring(pos, i)), "");
        } else if (name != null) {
          addParam(params, name, URL.decodeComponent(s.substring(pos, i)));
          name = null;
        }
        pos = i + 1;
      }
    }

    if (pos != i) {  // Are there characters we haven't dealt with?
      if (name == null) {     // Yes and we haven't seen any `='.
        addParam(params, URL.decodeComponent(s.substring(pos, i)), "");
      } else {                // Yes and this must be the last value.
        addParam(params, name, URL.decodeComponent(s.substring(pos, i)));
      }
    } else if (name != null) {  // Have we seen a name without value?
      addParam(params, name, "");
    }

    return params;
  }

  private static void addParam(Map<String, List<String>> params, String name, String value) {
    List<String> values = params.get(name);
    if (values == null) {
      values = new ArrayList<String>(1);  // Often there's only 1 value.
      params.put(name, values);
    }
    values.add(value);
  }

}
