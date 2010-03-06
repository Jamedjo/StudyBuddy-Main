
import java.awt.Cursor;

public enum DragMode {
    //Mode(hoverCursor,clickCursor),
    None(Cursor.getDefaultCursor(),Cursor.getDefaultCursor()),//Used instead of drag in 'zoom fit'
    Drag(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)),
    Link(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR),Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)),
    Note(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR),Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

    Cursor open,closed;

    DragMode(Cursor openCur, Cursor closedCur) {
        open = openCur;
        closed = closedCur;
    }
}
