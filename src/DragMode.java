
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

public enum DragMode {
    //Mode(hoverCursor,clickCursor),
    None(Cursor.getDefaultCursor(),Cursor.getDefaultCursor()),//Used instead of drag in 'zoom fit'
    Drag(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),SysIcon.DragPan,HotSpotPos.Centre),
    Link(SysIcon.LinkCursor,HotSpotPos.BottomLeft,Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)),
    Note(SysIcon.NoteCursor,HotSpotPos.BottomLeft,Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

    Cursor open,closed;
    static int count =0;
    Log log = new Log();

    DragMode(Cursor openCur, Cursor closedCur) {
        open = openCur;
        closed = closedCur;
    }
    DragMode(SysIcon openCur,Cursor closedCur){
        open = cursorFromIcon(openCur,HotSpotPos.TopLeft);
        closed = closedCur;
    }
    DragMode(SysIcon openCur,HotSpotPos spot,Cursor closedCur){
        open = cursorFromIcon(openCur,spot);
        closed = closedCur;
    }
    DragMode(Cursor openCur, SysIcon closedCur){
        open = openCur;
        closed = cursorFromIcon(closedCur,HotSpotPos.TopLeft);
    }
    DragMode(Cursor openCur,SysIcon closedCur,HotSpotPos spot){
        open = openCur;
        closed = cursorFromIcon(closedCur,spot);
    }
    DragMode(SysIcon openCur,SysIcon closedCur){
        open = cursorFromIcon(openCur,HotSpotPos.TopLeft);
        closed = cursorFromIcon(closedCur,HotSpotPos.TopLeft);
    }
    DragMode(SysIcon openCur,HotSpotPos spotO,SysIcon closedCur,HotSpotPos spotC){
        open = cursorFromIcon(openCur,spotO);
        closed = cursorFromIcon(closedCur,spotC);
    }
    //allow one or the other or both to be a string.
    //get currsor from icons folder. Or alow icon?

    Cursor cursorFromIcon(SysIcon icon,HotSpotPos spot){
        Cursor cur = null;
        Toolkit tk = Toolkit.getDefaultToolkit();
        try{
        if(icon.Icon != null){
        Image img = icon.Icon.getImage();
        Point hotSpot = spot.getPoint(img,tk);
        String name = icon.toString()+count;
        cur = tk.createCustomCursor(img,hotSpot,name);
        }
        } catch (IndexOutOfBoundsException e){
            log.print(LogType.Error,e);
        } catch (HeadlessException e){
            log.print(LogType.Error,e);
        } catch (NullPointerException e){
            log.print(LogType.Error,e);
        } finally{
            if(cur==null) cur = Cursor.getDefaultCursor();
            count++;
            return cur;
        }
    }
}

//The hot spot is the pixel in the cursor image which does the clicking
enum HotSpotPos{
    //name(x,y),
    TopLeft(SetPos.Min,SetPos.Min),
    TopRight(SetPos.Max,SetPos.Min),
    BottomLeft(SetPos.Min,SetPos.Max),
    BottomRight(SetPos.Max,SetPos.Min),
    Centre(SetPos.Middle,SetPos.Middle);
    //Could have middle of any side also, and custom.

    SetPos x,y;

    HotSpotPos(SetPos spx,SetPos spy){
        x=spx;
        y=spy;
    }

    Point getPoint(Image img,Toolkit tk){
        Dimension d = tk.getBestCursorSize(img.getWidth(null),img.getHeight(null));
        return new Point(x.getPos(d.width), y.getPos(d.height));
    }
}
enum SetPos{
    Min,Max,Middle;//could have custom which has int value

    int getPos(int i){
        switch (this) {
            case Min:
                return 0;
            case Max:
                return i-1;
            case Middle:
                return ((i + 1)/ 2)-1;
            default:
                return 0;
        }
    }
}