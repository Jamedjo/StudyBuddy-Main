
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PreviewAccessory extends JPanel implements PropertyChangeListener{
    ImageReference currentImageR=null;
    ThumbPreview thumb = new ThumbPreview(this,100);
    boolean isDir = false;
    BufferedImage dirImage = ErrorImages.directoryIcon;
    
    PreviewAccessory() {
        setLayout(new BorderLayout());
        add(new JLabel("Preview: "), BorderLayout.NORTH);
        add(thumb);
        RepaintManager.initPrevew(thumb);
    }

    public void propertyChange(PropertyChangeEvent e) {
        if(e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            File newFile =(File)e.getNewValue();
            if (newFile != null){
                if ((newFile.isFile()) && (FileDialogs.isImage(newFile))) {
                    String newPath = newFile.getAbsolutePath();
                    currentImageR = new ImageReference(newPath);
                    isDir=false;
                }
                else if(newFile.isDirectory()) {
                    currentImageR=null;
                    isDir=true;
                }
                thumb.setToolTipText(newFile.getName());
            }
            else {
                currentImageR=null;
                isDir=true;
                thumb.setToolTipText("");
            }
        }
        RepaintManager.repaint(RepaintType.Preview);
    }
    
    BufferedImage getCurrentImage() {
        if(isDir) return dirImage;
        if(currentImageR==null)return null;
        return currentImageR.getImage(ImgRequestSize.Thumb, false);
    }
}

class ThumbPreview extends JPanel {
    Log log = new Log();
    PreviewAccessory parent;
    int size;

    ThumbPreview(PreviewAccessory parentAccessory, int squareSize) {
        parent = parentAccessory;
        size = squareSize;
        //setBackground(Color.darkGray);
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        BufferedImage img = parent.getCurrentImage();
        if(img==null){
            g2.setBackground(Color.yellow);
            return;
        }

        Dimension useWH;
        useWH = ImageUtils.scaleToMax(img.getWidth(), img.getHeight(), size, size);

        g2.drawImage(img, 0 + ((size - useWH.width) / 2), 0 + ((size - useWH.height) / 2), useWH.width, useWH.height, this);

    }
}