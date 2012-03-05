package com.coremedia.iso.gui;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.util.Path;
import org.jdesktop.application.session.PropertySupport;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 11/4/11
 * Time: 10:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class BoxJTree extends JTree implements PropertySupport {
    public BoxJTree() {
        setCellRenderer(new BoxNodeRenderer());
        setRootVisible(false);
        setLargeModel(true);
        setName("boxTree");
    }

    public Object getSessionState(Component c) {
        Enumeration<TreePath> treePathEnumeration = this.getExpandedDescendants(new TreePath(this.getModel().getRoot()));
        java.util.List<String> openPath = new LinkedList<String>();
        Path oldMp4Path = null;
        if (treePathEnumeration != null) {
            oldMp4Path = new Path((IsoFile) this.getModel().getRoot());

            while (treePathEnumeration.hasMoreElements()) {
                TreePath treePath = treePathEnumeration.nextElement();
                openPath.add(oldMp4Path.createPath((com.coremedia.iso.boxes.Box) treePath.getLastPathComponent()));
            }
        }
        return openPath;

    }

    public void setSessionState(Component c, Object state) {
        LinkedList<String> openPath = (LinkedList<String>) state;
        Path nuMp4Path = new Path((IsoFile) this.getModel().getRoot());
        if (!openPath.isEmpty()) {

            for (String s : openPath) {
                Box expanded = nuMp4Path.getPath(s);
                List path = new LinkedList();
                while (expanded != null) {
                    path.add(expanded);
                    expanded = expanded.getParent();
                }
                if (path.size() > 0) {
                    Collections.reverse(path);
                    TreePath tp = new TreePath(path.toArray());
                    this.expandPath(tp);
                }
            }
        }
    }
}
