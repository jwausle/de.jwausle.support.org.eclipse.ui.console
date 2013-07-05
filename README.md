de.jw.equinox.osgi.console.withhistory
======================================

Enhancement for org.eclipse.pde.ui.internal.util.OSGiConsole ( Host OSGi Console) with command history


## HOWTO-install:
- update-site: https://github.com/jwausle/de.jw.equinox.osgi.console.withhistory/raw/master/de.jw.equinox.osgi.console.withhistory.updatesite

OR

- download: https://github.com/jwausle/de.jw.equinox.osgi.console.withhistory/archive/master.zip
- unzip: master.zip
- eclipse: install as local-update site 'file:/.../de.jw.equinox.osgi.console.withhistory/de.jw.equinox.osgi.console.withhistory.updatesite'
- eclipse: restart 

![show console/select-first-arrow-down-from-console-toolbar and find 'Host OSGiConsoleWithHistory' beside 'Host OSGiConsole'](https://github.com/jwausle/de.jw.equinox.osgi.console.withhistory/raw/master/img/screenshot-successful-installation.png)

## HOWTO-contribute
- contact me: jan.winter.leipzig+git@gmail.com
- git clone https://github.com/jwausle/de.jw.equinox.osgi.console.withhistory.git


**Bundle-info**: de.jw.equinox.osgi.console.withhistory.fragment
- contains implementation to react of Key-Events (arrow-up, arrow-down)
- some parts of code have to copied from org.eclipse.pde.ui.internal.console.OSGiConsole 

**Bundle-info**: de.jw.equinox.osgi.console.withhistory.feature
- eclipse-pde-feature 

**Bundle-info**: de.jw.equinox.osgi.console.withhistory.updatesite
- last successful tested update site (for indigo and juno)
