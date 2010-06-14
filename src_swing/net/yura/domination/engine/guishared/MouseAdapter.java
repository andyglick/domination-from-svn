/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.yura.domination.engine.guishared;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * this class is needed as in java 1.4 MouseAdapter is broekn
 * @author Yura Mamyrin
 */
public class MouseAdapter implements MouseListener,MouseMotionListener {
    public void mouseClicked(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mouseDragged(MouseEvent e) { }
    public void mouseMoved(MouseEvent e) { }
}
