package com.googlecode.mp4parser.isoviewer;


import com.coremedia.iso.gui.IsoViewerPanel;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IsoViewer extends SingleFrameApplication {
    IsoViewerPanel isoViewerPanel;
    String sessionFile = "sessionState.xml";
    ApplicationContext ctx = getContext();
    Logger logger = Logger.getLogger("IsoViewer");

    @Override
    protected void startup() {
        ResourceMap resource = ctx.getResourceMap();
        isoViewerPanel = new IsoViewerPanel();
        resource.injectFields(isoViewerPanel);
        isoViewerPanel.createLayout();
        try {
            ctx.getSessionStorage().restore(this.getMainFrame(), sessionFile);
        } catch (IOException e) {
            logger.log(Level.WARNING, "couldn't restore session", e);
        }
        // isoViewerPanel.open();
        ApplicationActionMap map = ctx.getActionMap(isoViewerPanel);
        this.getMainFrame().setJMenuBar(createMenu(map));
        show(isoViewerPanel);

    }

    @Override
    protected void shutdown() {
        super.shutdown();
        try {
            ctx.getSessionStorage().save(this.getMainFrame(), sessionFile);
        } catch (IOException e) {
            logger.log(Level.WARNING, "couldn't save session", e);
        }
    }

    public static void main(String[] args) {
        Application.launch(IsoViewer.class, args);
    }


    protected JMenuBar createMenu(ApplicationActionMap map) {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        JMenuItem open = new JMenuItem();
        open.setAction(map.get("open-iso-file"));
        menu.add(open);
        return menuBar;
    }


}