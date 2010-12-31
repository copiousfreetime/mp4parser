/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package com.coremedia.iso.gui;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.MediaDataBox;
import com.coremedia.iso.mdta.Chunk;
import com.coremedia.iso.mdta.Sample;
import com.coremedia.iso.mdta.Track;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.UnsupportedEncodingException;

/**
 * Adapter for an <code>IsoFile</code> to act as a <code>TreeModel</code>
 *
 * @see IsoFile
 * @see TreeModel
 */
public class IsoFileTreeModel implements TreeModel {
    private IsoFile file;

    public IsoFileTreeModel(IsoFile file) {
        this.file = file;
    }

    public Object getRoot() {
        return new IsoFileTreeNode(file);
    }

    public int getChildCount(Object parent) {
        parent = ((IsoFileTreeNode) parent).getObject();
        if (parent != null) {
            if (parent instanceof ContainerBox) {
                ContainerBox container = (ContainerBox) parent;
                return container.getBoxes() == null ? 0 : container.getBoxes().length;
            } else if (parent instanceof MediaDataBox) {
                return ((MediaDataBox) parent).getTracks() == null ? 0 : ((MediaDataBox) parent).getTracks().size();
            } else if (parent instanceof Track) {
                return ((Track) parent).getChunks() == null ? 0 : ((Track) parent).getChunks().size();
            } else if (parent instanceof Chunk) {
                return ((Chunk) parent).getSamples() == null ? 0 : ((Chunk) parent).getSamples().size();
            }
        }
        return 0;
    }


    public boolean isLeaf(Object node) {
        return !(((IsoFileTreeNode) node).getObject() instanceof ContainerBox)
                && !(((IsoFileTreeNode) node).getObject() instanceof MediaDataBox)
                && !(((IsoFileTreeNode) node).getObject() instanceof Chunk)
                && !(((IsoFileTreeNode) node).getObject() instanceof Track);
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }

    public Object getChild(Object parent, int index) {
        parent = ((IsoFileTreeNode) parent).getObject();
        if (parent instanceof ContainerBox) {
            ContainerBox container = (ContainerBox) parent;
            return new IsoFileTreeNode(container.getBoxes()[index]);

        } else if (parent instanceof MediaDataBox) {
            return new IsoFileTreeNode(((MediaDataBox) parent).getTracks().get(index));

        } else if (parent instanceof Chunk) {
            return new IsoFileTreeNode(((Chunk) parent).getSamples().get(index));

        } else if (parent instanceof Track) {
            return new IsoFileTreeNode(((Track) parent).getChunks().get(index));

        }
        return null;
    }

    public int getIndexOfChild(Object parent, Object child) {

        parent = ((IsoFileTreeNode) parent).getObject();
        child = ((IsoFileTreeNode) child).getObject();
        if (parent instanceof ContainerBox) {
            ContainerBox container = (ContainerBox) parent;
            Box[] boxes = container.getBoxes();
            for (int i = 0; i < boxes.length; i++) {
                if (boxes[i].equals(child)) {
                    return i;
                }
            }
        } else if (parent instanceof MediaDataBox) {
            MediaDataBox container = (MediaDataBox) parent;
            //noinspection SuspiciousMethodCalls
            return container.getTracks().indexOf(child);

        } else if (parent instanceof Track) {
            Track track = (Track) parent;
            //noinspection SuspiciousMethodCalls
            return track.getChunks().indexOf(child);

        } else if (parent instanceof Chunk) {
            Chunk chunk = (Chunk) parent;
            //noinspection SuspiciousMethodCalls
            return chunk.getSamples().indexOf(child);

        }

        return 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException();
    }

    public static class IsoFileTreeNode {
        private Object object;

        public IsoFileTreeNode(Object object) {
            this.object = object;
        }

        public Object getObject() {
            return object;
        }

        public String toString() {
            if (object instanceof IsoFile) {
                return "ISO Base Media File";
            } else if (object instanceof AbstractBox) {
                AbstractBox box = (AbstractBox) object;
                try {
                    return new String(box.getType(), "ISO-8859-1") + " (" + box.getDisplayName() + ")";
                } catch (UnsupportedEncodingException e) {
                    return new String(box.getType()) + " (" + box.getDisplayName() + ")";
                }
            } else if (object instanceof Track) {
                return "Track (trackId=" + Long.toString(((Track) object).getTrackId()) + ")";
            } else if (object instanceof Chunk) {
                return "Chunk at " + ((Chunk) object).calculateOffset();
            } else if (object instanceof Sample) {
                return "Sample";
            }
            throw new UnsupportedOperationException();
        }
    }
}
