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
import java.util.NoSuchElementException;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Helper to iterate on {@link JsArray} objects.
 * See GWT bug #3997.
 */
public final class JsArrayIterator<T extends JavaScriptObject> implements Iterable<T>, Iterator<T> {

  private final JsArray<T> array;
  private int current;

  public JsArrayIterator(final JsArray<T> array) {
    this.array = array;
  }

  public static <T extends JavaScriptObject> JsArrayIterator<T> iter(final JsArray<T> array) {
    return new JsArrayIterator<T>(array);
  }

  public Iterator<T> iterator() {
    return this;
  }

  public boolean hasNext() {
    return current < array.length();
  }

  public T next() {
    final T element = array.get(current++);
    if (element == null && current > array.length()) {
      throw new NoSuchElementException("current=" + current
                                       + ", array.length=" + array.length());
    }
    return element;
  }

  public void remove() {
    throw new UnsupportedOperationException("Cannot remove() from a JsArray");
  }

}
