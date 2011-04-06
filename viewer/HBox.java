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

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Really just a {@link HorizontalPanel} with some more methods.
 */
public class HBox extends HorizontalPanel {

  /** Shorthand to align things to the right without typing too much.  */
  public static final HasHorizontalAlignment.HorizontalAlignmentConstant
    ALIGN_RIGHT = HasHorizontalAlignment.ALIGN_RIGHT;

  /**
   * Adds a widget and also specify its alignment.
   */
  public void add(final Widget w,
                  // They couldn't make names longer than that...
                  final HasHorizontalAlignment.HorizontalAlignmentConstant align) {
    super.add(w);
    super.setCellHorizontalAlignment(w, align);
  }

  /** Adds the given widget and aligns it to the right.  */
  public void addRight(final Widget w) {
    add(w, ALIGN_RIGHT);
  }

}
