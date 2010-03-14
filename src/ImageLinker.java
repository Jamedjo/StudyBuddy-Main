//This should be replaced with a GUI box showing thumbnails.
//It could be driven by the same panel as the quicktagger when that is donw with thumbnails.
public class ImageLinker {
    static boolean SelectingImage = false; // For selecting an image link
    static String DummyLinkID;

    static void setSelectingImage(boolean IsSelecting) {
        SelectingImage = IsSelecting;
    }
    static boolean getSelectingImage() {
        return SelectingImage;
    }
    static void setDummyLinkID(String LinkID) {
        DummyLinkID = LinkID;
    }
    static String getDummyLinkID() {
        return DummyLinkID;
    }
}
