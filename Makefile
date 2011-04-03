# Copyright 2011 Benoit Sigoure.
#
# This library is free software: you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as published
# by the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this library.  If not, see <http://www.gnu.org/licenses/>.

all: viewer

viewer: gwtc staticroot

top_builddir = build

viewer_JAVA = \
	viewer/DateTimeBox.java	\
	viewer/ESResponse.java	\
	viewer/EventsHandler.java	\
	viewer/JsArrayIterator.java	\
	viewer/Json.java	\
	viewer/Main.java	\
	viewer/QueryStringDecoder.java	\
	viewer/Summary.java	\
	viewer/ValidatedTextBox.java	\

viewer_DEPENDENCIES = viewer.gwt.xml

dist_pkgdata_DATA = \
	viewer/index.html	\

GWT_VERSION = 2.3.0
GWT_DEV = third_party/gwt/gwt-dev-$(GWT_VERSION).jar
GWT_SDK = third_party/gwt/gwt-user-$(GWT_VERSION).jar
GWT_DEPS = third_party/gwt/gwt-servlet-deps-$(GWT_VERSION).jar
JAVAX_VALIDATION_VERSION = 1.0.0.GA
JAVAX_VALIDATION = third_party/javax/validation-api-$(JAVAX_VALIDATION_VERSION).jar
JAVAX_VALIDATION_SRC = third_party/javax/validation-api-$(JAVAX_VALIDATION_VERSION)-sources.jar
DEPS = $(GWT_DEV) $(GWT_SDK) $(GWT_DEPS) $(JAVAX_VALIDATION) $(JAVAX_VALIDATION_SRC)
CP = $(GWT_DEV):$(GWT_SDK):$(GWT_DEPS):$(JAVAX_VALIDATION):$(JAVAX_VALIDATION_SRC):.
GWTC_JVM_ARGS =  # add jvmarg -Xss16M or similar if you see a StackOverflowError
GWTC_ARGS = -ea  # Additional arguments like -style PRETTY or -logLevel DEBUG

# The GWT compiler is way too slow, that's not very Googley.  So we save the
# MD5 of the files we compile in the stamp file and everytime `make' things it
# needs to recompile the GWT code, we verify whether the code really changed
# or whether it's just a file that was touched (which happens frequently when
# using Git while rebasing and whatnot).
gwtc: $(top_builddir)/.gwtc-stamp
MD5 = md5  # TODO(tsuna): Detect the right command to use at configure time.
$(top_builddir)/.gwtc-stamp: $(viewer_JAVA) $(viewer_DEPENDENCIES) $(DEPS)
	@mkdir -p $(top_builddir)/gwt
	cat $(viewer_JAVA) $(viewer_DEPENDENCIES) | $(MD5) >"$@-t"
	cmp -s "$@" "$@-t" || \
          java $(GWTC_JVM_ARGS) -cp $(CP) com.google.gwt.dev.Compiler \
            $(GWTC_ARGS) -war $(top_builddir)/gwt viewer
	@rm -f $(top_builddir)/.staticroot-stamp
	mv "$@-t" "$@"
# Note: we have to remove the staticroot-stamp because the GWT compiler will
# nuke everything in that directory when it produces its output.

GWT_DEV_ARGS = -Xmx512m  # The development mode is a memory hog.
GWT_DEV_URL = file://`pwd`/$(top_builddir)/gwt/droopy/index.html
gwtdev: staticroot
	java $(GWT_DEV_ARGS) -ea -cp $(CP) com.google.gwt.dev.DevMode \
	  -startupUrl $(GWT_DEV_URL) -noserver -war $(top_builddir)/gwt viewer

staticroot: $(top_builddir)/.staticroot-stamp

$(top_builddir)/.staticroot-stamp: $(dist_pkgdata_DATA)
	mkdir -p $(top_builddir)/gwt/droopy
	cp $(dist_pkgdata_DATA) $(top_builddir)/gwt/droopy
	@touch $(top_builddir)/.staticroot-stamp

clean:
	@rm -f $(top_builddir)/.gwtc-stamp* $(top_builddir)/.staticroot-stamp
	rm -rf $(top_builddir)/gwt $(top_builddir)/staticroot

distclean: clean
	test ! -d $(top_builddir) || rmdir $(top_builddir)

.PHONY: all clean distclean gwtc gwtdev staticroot viewer
