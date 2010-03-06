
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.ImageIcon;

public enum DragMode {
    //Mode(hoverCursor,clickCursor),
    None(Cursor.getDefaultCursor(),Cursor.getDefaultCursor()),//Used instead of drag in 'zoom fit'
    Drag(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),SysIcon.DragPan.Icon,true),
    Link(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR),Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)),
    Note(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR),Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

    Cursor open,closed;

    DragMode(Cursor openCur, Cursor closedCur) {
        open = openCur;
        closed = closedCur;
    }
    DragMode(ImageIcon openCur,Cursor closedCur){
        open = cursorFromIcon(openCur,false);
        closed = closedCur;
    }
    DragMode(ImageIcon openCur,boolean centrePoint,Cursor closedCur){
        open = cursorFromIcon(openCur,centrePoint);
        closed = closedCur;
    }
    DragMode(Cursor openCur, ImageIcon closedCur){
        open = openCur;
        closed = cursorFromIcon(closedCur,false);
    }
    DragMode(Cursor openCur,ImageIcon closedCur,boolean centrePoint){
        open = openCur;
        closed = cursorFromIcon(closedCur,centrePoint);
    }
    DragMode(ImageIcon openCur,ImageIcon closedCur){
        open = cursorFromIcon(openCur,false);
        closed = cursorFromIcon(closedCur,false);
    }
    DragMode(ImageIcon openCur,boolean centrePointO,ImageIcon closedCur,boolean centrePointC){
        open = cursorFromIcon(openCur,centrePointO);
        closed = cursorFromIcon(closedCur,centrePointC);
    }
    //allow one or the other or both to be a string.
    //get currsor from icons folder. Or alow icon?

    Cursor cursorFromIcon(ImageIcon icon,boolean centrePoint){
        Cursor cur;
        Point hotSpot = new Point(0,0);
        Image img = icon.getImage();
        if (centrePoint) hotSpot.move(img.getWidth(null)/2,img.getHeight(null)/2);
        Toolkit tk = Toolkit.getDefaultToolkit();
        cur = tk.createCustomCursor(img,hotSpot,"IconCursor");
        return cur;
    }
}
