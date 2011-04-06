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

import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public abstract class LazyTreeItem extends TreeItem {

  private boolean loaded = false;

  public LazyTreeItem(final Widget w) {
    super(w);
    super.addItem("Loading...");
  }

  protected abstract void onFirstOpen();

  protected abstract void onOpen();

  protected abstract void onClose();

  public void setState(final boolean open, final boolean fire_event) {
    if (open) {
      if (!loaded) {
        onFirstOpen();
        loaded = true;
      } else {
        onOpen();
      }
    } else if (!open) {
      onClose();
    }
    super.setState(open, fire_event);
  }

}
