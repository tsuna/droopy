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

import java.util.Arrays;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
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

  private static InlineLabel label(final String s) {
    return new InlineLabel(s);
  }

  private static boolean needRightAlign(final String s) {
    return s.endsWith("ms")
      || (!s.isEmpty() && Character.isDigit(s.charAt(0)));
  }

  private static boolean addAutoAlign(final HBox row, final String s) {
    if (needRightAlign(s)) {
      row.addRight(label(s));
      return true;
    } else {
      row.add(label(s));
      return false;
    }
  }

  public void addRow(final String a, final String b) {
    addItem(row(a, b));
  }

  public static HBox row(final String a, final String b) {
    final HBox row = new HBox();
    row.add(label(a));
    addAutoAlign(row, b);
    return row;
  }

  public void addRow(final String a, final String b, final String c) {
    addItem(row(a, b, c));
  }

  public static HBox row(final String a, final String b, final String c) {
    final HBox row = new HBox();
    row.add(label(a));
    if (addAutoAlign(row, b)) {
      row.addRight(label(c));
    } else {
      addAutoAlign(row, c);
    }
    return row;
  }

  public void addRow(final String a, final String b, final String c, final String d) {
    addItem(row(a, b, c, d));
  }

  public static HBox row(final String a, final String b, final String c, final String d) {
    final HBox row = new HBox();
    row.add(label(a));
    boolean align_right = addAutoAlign(row, b);
    if (align_right) {
      row.addRight(label(c));
    } else {
      align_right = addAutoAlign(row, c);
    }
    if (align_right) {
      row.addRight(label(d));
    } else {
      addAutoAlign(row, d);
    }
    return row;
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
    int[] widths = new int[first.getWidgetCount()];
    for (final Widget w : this) {
      if (w instanceof HorizontalPanel) {
        final HorizontalPanel h = (HorizontalPanel) w;
        final int n = h.getWidgetCount();
        if (n > widths.length) {
          // Uh?  There's no Arrays.copyOf in GWT's emulated JRE.  WTF?
          //widths = Arrays.copyOf(widths, n);
          final int[] old = widths;
          widths = new int[n];
          System.arraycopy(old, 0, widths, 0, old.length);
        }
        for (int i = 0; i < n; i++) {
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
        final int n = h.getWidgetCount();
        for (int i = 0; i < n; i++) {
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
