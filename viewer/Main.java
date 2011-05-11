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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import static viewer.Json.object;

/**
 * Main class for the Droopy UI.
 */
final class Main implements EntryPoint {

  private static final DateTimeFormat FULLDATE =
    DateTimeFormat.getFormat("yyyy/MM/dd-HH:mm:ss");

  private String server;

  private final VerticalPanel root = new VerticalPanel();
  private final InlineLabel status = new InlineLabel();
  private final AlignedTree traces = new AlignedTree();

  private final DateTimeBox start_datebox = new DateTimeBox();
  private final DateTimeBox end_datebox = new DateTimeBox();

  private abstract class AjaxCallback implements RequestCallback/*AsyncCallback<JavaScriptObject>*/ {
    public void onError(final Request req, final Throwable e) {
      status.setText("AJAX query failed: " + e.getMessage());
    }
    public final void onResponseReceived(final Request request, final Response response) {
      final String text = response.getText();
      if (text.isEmpty()) {
        onError(request, new RuntimeException("Empty response from server: "
                                              + response.getStatusCode()
                                              + " " + response.getStatusText()));
      } else {
        JSONValue value;
        try {
          value = JSONParser.parseStrict(text);
        } catch (JSONException e) {
          onError(request, e);
          return;
        }
        onSuccess(value);
      }
    }
    protected abstract void onSuccess(final JSONValue response);
  }

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    server = getServer();
    if (server == null) {
      promptForServerUi();
      return;
    }
    status.setText("Checking server health...");
    root.add(status);
    removeLoadingMessage();
    RootPanel.get().add(root);
    ajax("/droopy/_status", new AjaxCallback() {
      public void onSuccess(final JSONValue response) {
        final JSONObject resp = response.isObject();
        if (resp.containsKey("ok")
            && resp.get("ok").isBoolean().booleanValue()) {
          status.setText("Server is ready, "
                         + (resp.get("indices").isObject()
                            .get("droopy").isObject()
                            .get("docs").isObject()
                            .get("num_docs")) + " traces available."
                         + "  Loading ...");
          setupUi();
          setupHistory();
        } else if (resp.containsKey("error") && resp.containsKey("status")) {
          status.setText("Server health check failed: error "
                         + resp.get("status") + ": " + resp.get("error"));
        } else {
          status.setText("Incomprehensible response from server: " + resp);
        }
      }
    });
  }

  private static void removeLoadingMessage() {
    // Remove the static "Loading..." message.
    final Element loading = DOM.getElementById("loading");
    DOM.removeChild(DOM.getParent(loading), loading);
  }

  private static String getServer() {
    final String server = Window.Location.getParameter("srv");
    if (server == null || server.isEmpty()) {
      return null;
    }
    return "http://" + server;
  }

  private void promptForServerUi() {
    final VerticalPanel vbox = new VerticalPanel();
    vbox.add(new InlineLabel("I need to know the address of the ElasticSearch"
                             + " server where Droopy traces are stored."));
    final HorizontalPanel hbox = new HorizontalPanel();
    hbox.setSpacing(5);
    hbox.add(new InlineLabel("Address (host:port):"));
    final ValidatedTextBox textbox = new ValidatedTextBox();
    textbox.setValidationRegexp("^[-_.a-zA-Z0-9]+:[1-9][0-9]*$");
    final EventsHandler setsrv = new EventsHandler() {
      protected <H extends EventHandler> void onEvent(final DomEvent<H> event) {
        final String srv = textbox.getValue();
        if (srv == null || srv.isEmpty()) {
          return;
        }
        final StringBuilder url = new StringBuilder();
        url.append(Window.Location.getPath());
        String tmp = Window.Location.getQueryString();
        if (tmp != null && !tmp.isEmpty()) {
          url.append(tmp).append("&srv=");
        } else {
          url.append("?srv=");
        }
        url.append(srv);
        tmp = Window.Location.getHash();
        if (tmp != null && !tmp.isEmpty()) {
          url.append(tmp);
        }
        Window.Location.assign(url.toString());
      }
    };
    textbox.addBlurHandler(setsrv);
    textbox.addKeyPressHandler(setsrv);
    hbox.add(textbox);
    vbox.add(hbox);
    root.add(vbox);
    removeLoadingMessage();
    RootPanel.get().add(root);
  }

  private void ajax(final String resource, final AjaxCallback callback) {
    ajax(resource, null, callback);
  }

  private void ajax(final String resource, final String body,
                    final AjaxCallback callback) {
    final boolean has_body = body != null;
    final RequestBuilder builder =
      new RequestBuilder(has_body ? RequestBuilder.POST : RequestBuilder.GET,
                         server + resource);
    // Doesn't work on Chrome due to ES bug #828.
    //if (has_body) {
    //  builder.setHeader("Content-Type", "application/json");
    //}
    try {
      builder.sendRequest(body, callback);
    } catch (RequestException e) {
      status.setText("Failed to setup AJAX call to " + server + resource
                     + ": " + e);
    }
  }

  private void setupUi() {
    setupChangeHandlers();
    final HorizontalPanel hbox = new HorizontalPanel();
    hbox.setSpacing(5);
    hbox.add(new InlineLabel("From"));
    hbox.add(start_datebox);
    // If we're not trying to see anything already, look at some recent
    // traces by default.
    if (History.getToken().isEmpty()) {
      start_datebox.setValue(new Date(System.currentTimeMillis() - 3600000), true);
    }
    hbox.add(new InlineLabel("To"));
    hbox.add(end_datebox);
    root.add(hbox);
    traces.setAnimationEnabled(true);
    root.add(traces);
  }

  private void setupChangeHandlers() {
    final EventsHandler refresh = new EventsHandler() {
      protected <H extends EventHandler> void onEvent(final DomEvent<H> event) {
        refresh();
      }
    };

    {
      final ValueChangeHandler<Date> vch = new ValueChangeHandler<Date>() {
        public void onValueChange(final ValueChangeEvent<Date> event) {
          refresh();
        }
      };
      TextBox tb = start_datebox.getTextBox();
      tb.addKeyPressHandler(refresh);
      start_datebox.addValueChangeHandler(vch);
      tb = end_datebox.getTextBox();
      tb.addKeyPressHandler(refresh);
      end_datebox.addValueChangeHandler(vch);
    }
  }

  private void setupHistory() {
    final ValueChangeHandler<String> handler = new ValueChangeHandler<String>() {
      public void onValueChange(final ValueChangeEvent<String> event) {
        final Map<String, List<String>> params = parseQueryString(event.getValue());
        List<String> value;
        if ((value = params.get("start")) != null) {
          start_datebox.getTextBox().setValue(value.get(0));
        }
        if ((value = params.get("end")) != null) {
          end_datebox.getTextBox().setValue(value.get(0));
        }
        loadTraces();
      }
    };
    History.addValueChangeHandler(handler);
    History.fireCurrentHistoryState();
  }

  private void refresh() {
    final Date start = start_datebox.getValue();
    if (start == null) {
      status.setText("Please specify a start time.");
      return;
    }
    final Date end = end_datebox.getValue();
    if (end != null && end.getTime() <= start.getTime()) {
        end_datebox.addStyleName("dateBoxFormatError");
        status.setText("End time must be after start time!");
        return;
    }
    final StringBuilder token = new StringBuilder();
    token.append("start=").append(FULLDATE.format(start));
    if (end != null) {
      token.append("&end=").append(FULLDATE.format(end));
    }
    final String tok = token.toString();
    History.newItem(token.toString());
  }

  private void loadTraces() {
    status.setText("Loading...");
    final Json request_ts = object()
      .add("from", toMillis(start_datebox));
    if (end_datebox.getValue() != null) {
      request_ts.add("to", toMillis(end_datebox));
    }
    final String json = object()
      .add("size", 10)  // XXX
      .add("sort", Json.array()
           .add("request_ts", object("order", "desc")))
      .add("query",
           object("constant_score",
                  object("filter",
                         object("numeric_range",
                                object("request_ts", request_ts)
                               )
                        )
                 )
          )
      .toString();
    ajax("/droopy/summary/_search", json,
          new AjaxCallback() {
      public void onSuccess(final JSONValue response) {
        final ESResponse<Summary> resp = ESResponse.fromJson(response.isObject());
        status.setText("Found " + resp.hits().total() + " traces in "
                       + resp.took() + "ms");
        renderTraces(resp.hits());
      }
    });
  }

  private void renderTraces(final ESResponse.Hits<Summary> summaries) {
    traces.clear();
    final HashSet<String> expanded = new HashSet(getHistoryTokens("trace"));
    for (final ESResponse.Hit<Summary> hit : summaries.iterator()) {
      final String id = hit.id();
      final Summary summary = hit.source();
      final TreeItem trace = new LazyTreeItem(summary.widget()) {
        protected void onFirstOpen() {
          expandTrace(this, id, summary);
          onOpen();
        }
        protected void onOpen() {
          appendHistoryToken("trace", id);
        }
        protected void onClose() {
          removeHistoryToken("trace", id);
        }
      };
      traces.addItem(trace);
      if (expanded.remove(id)) {  // If this trace ID should be expanded...
        trace.setState(true);     // then expand it now.
      }
    }
    traces.align();
    // IDs that haven't been expanded are no longer displayed,
    // so remove them from the URL.
    for (final String id : expanded) {
      removeHistoryToken("trace", id);
    }
  }

  private void expandTrace(final TreeItem parent, final String traceid,
                           final Summary summary) {
    ajax("/droopy/trace/" + traceid, new AjaxCallback() {
      public void onSuccess(final JSONValue response) {
        final ESResponse.Hit<Trace> hit = ESResponse.Hit.fromJson(response.isObject());
        parent.removeItems();
        parent.addItem(hit.source().widget(summary));
      }
    });
  }

  // ---------------- //
  // History helpers. //
  // ---------------- //

  private static void appendHistoryToken(final String key, final String value) {
    appendHistoryToken(key, value, false);
  }

  private static void appendHistoryToken(final String key, final String value,
                                         final boolean fire_event) {
    final String current = History.getToken();
    if (current.isEmpty()) {
      History.newItem(key + '=' + value, fire_event);
      return;
    }
    final Map<String, List<String>> params = parseQueryString(current);
    final List<String> values = params.get(key);
    if (values != null) {
      for (final String existing : values) {
        if (value.equals(existing)) {
          return;
        }
      }
    }
    History.newItem(current + '&' + key + '=' + value, fire_event);
  }

  private static void removeHistoryToken(final String key, final String value) {
    removeHistoryToken(key, value, false);
  }

  private static void removeHistoryToken(final String key, final String value,
                                         final boolean fire_event) {
    final String current = History.getToken();
    if (current.isEmpty()) {
      return;
    }
    final String search = key + '=' + value;
    if (current.startsWith(search)) {
      History.newItem(current.substring(search.length()));
      return;
    }
    final Map<String, List<String>> params = parseQueryString(current);
    final List<String> values = params.get(key);
    if (values != null) {
      for (final String existing : values) {
        if (value.equals(existing)) {
          final int index = current.indexOf('&' + search);
          if (index < 0) {  // Should never happen.  Be loud.
            Window.alert("WTF? Failed to find history token "
                         + search + " in URL");
          } else {
            History.newItem(current.substring(0, index)
                            + current.substring(index + search.length() + 1),
                            fire_event);
          }
          return;
        }
      }
    }
  }

  private static final List<String> getHistoryTokens(final String key) {
    final String token = History.getToken();
    if (token.isEmpty()) {
      return Collections.emptyList();
    }
    final Map<String, List<String>> params = parseQueryString(token);
    final List<String> values = params.get(key);
    // OK, this is an interesting peculiarity of the Java language.
    // I originally wrote the following line, but it doesn't compile:
    //return values == null ? Collections.emptyList() : values;
    // Because of this somewhat cryptic error:
    //   Type mismatch: cannot convert from
    //   List<capture#1-of ? extends Object> to List<String
    // This is because the type of the conditional expression is the "lub"
    // (lower upper bound) of the two operands.  So instead we have to help
    // the compiler and disambiguate the code with this C++-like syntax:
    return values == null ? Collections.<String>emptyList() : values;
  }

  private static final Map<String, List<String>> parseQueryString(final String querystring) {
    return QueryStringDecoder.getParameters(querystring);
  }

  // ------------- //
  // Misc helpers. //
  // ------------- //

  private static final long toMillis(final DateTimeBox box) {
    return box.getValue().getTime();
  }

}
