
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ImageSelectPane extends JScrollPane {
    private JPanel imageGrid;
    private String[] imageIDs;
    private ImageReference[] imageList;
    private final int thumbSize = 80;
    final int noColumns;

    public ImageSelectPane(GUI mainGUI,int columns) {
        noColumns=columns;
        imageIDs = mainGUI.mainImageDB.getAllImageIDs();
        imageList = new ImageReference[imageIDs.length];
        for (int i = 0;i<imageIDs.length;i++) {
            imageList[i] = new ImageReference(mainGUI.mainImageDB.getImageFilename(imageIDs[i]), mainGUI);
        }

        GridLayout gridLayout = new GridLayout(0, noColumns,1,1);
        imageGrid = new JPanel(gridLayout);
        imageGrid.setBackground(Color.darkGray);
        gridLayout.minimumLayoutSize(imageGrid);

        for (int i=0;i<imageIDs.length;i++){
            imageGrid.add(new ThumbIcon(this, thumbSize, i, imageList[i].pathFile.toString()));
        }

        this.setViewportView(imageGrid);
    }
    BufferedImage imageFromNumber(int thumbNumber) {
        return imageList[thumbNumber].getImage(ImgRequestSize.Thumb, false);
    }
    public int numberImages() {
        return imageIDs.length;
    }

}

class ThumbIcon extends JPanel implements MouseListener {
    Log log = new Log();
    ImageSelectPane parent;
    int size;
    int thumbNumber;
    boolean isSelected = false;
    BufferedImage selectedImg = SysIcon.Tick.getBufferedImage(1.5, BufferedImage.TYPE_INT_ARGB);

    ThumbIcon(ImageSelectPane parentPane, int squareSize, int imgNo, String altText) {
        parent = parentPane;
        size = squareSize;
        thumbNumber = imgNo;
        setToolTipText(altText);
        //setBackground(Color.darkGray);
        addMouseListener(this);
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
    }

    public void paintComponent(java.awt.Graphics g) {
        Dimension useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int thumbOfsetW = 0;
        int thumbOfsetH = 0;
        if (thumbNumber<parent.numberImages()) {
            BufferedImage img = parent.imageFromNumber(thumbNumber);

            useWH = ImageUtils.scaleToMax(img.getWidth(), img.getHeight(), size, size);
            g2.drawImage(img, thumbOfsetW + ((size - useWH.width) / 2), thumbOfsetH + ((size - useWH.height) / 2), useWH.width, useWH.height, this);
            if (isSelected) {
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);
                g2.setComposite(ac);
                g2.drawImage(selectedImg, 0, 0,size, size, this);
            }
        }
    }

    public void mouseClicked(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){
        isSelected ^= true;
        repaint();
    }
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e) {}
}
