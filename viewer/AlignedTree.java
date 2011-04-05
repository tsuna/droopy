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

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class AlignedTree extends Tree {

  private final int spacing;

  public AlignedTree() {
    this(5);
  }

  public AlignedTree(final int spacing) {
    if (spacing < 0) {
      throw new IllegalArgumentException("negative spacing: " + spacing);
    }
    this.spacing = spacing;
  }

  /**
   * Aligns all the columns of the horizontal boxes within this tree.
   * This essentially helps make the tree look like a table with columns.
   */
  public void align() {
    final HorizontalPanel first = findFirstHBox();
    if (first == null) {
      return;
    }

    // 1st pass: find the max width for each "column".
    final int widths[] = new int[first.getWidgetCount()];
    for (final Widget w : this) {
      if (w instanceof HorizontalPanel) {
        final HorizontalPanel h = (HorizontalPanel) w;
        for (int i = 0; i < Math.min(widths.length, h.getWidgetCount()); i++) {
          final int width = h.getWidget(i).getOffsetWidth();
          if (width > widths[i]) {
            widths[i] = width;
          }
        }
      }
    }

    // Add the spacing we were asked to add.
    for (int i = 0; i < widths.length; i++) {
      widths[i] += spacing;
    }

    // 2nd pass: set the width on every cell in each row.
    for (final Widget w : this) {
      if (w instanceof HorizontalPanel) {
        final HorizontalPanel h = (HorizontalPanel) w;
        for (int i = 0; i < Math.min(widths.length, h.getWidgetCount()); i++) {
          h.setCellWidth(h.getWidget(i), widths[i] + "px");
        }
      }
    }
  }

  private HorizontalPanel findFirstHBox() {
    for (final Widget w : this) {
      if (w instanceof HorizontalPanel) {
        return (HorizontalPanel) w;
      }
    }
    return null;
  }

}
