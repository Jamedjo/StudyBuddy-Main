
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/*
 *
 * This class is a JPanel which draws the loading animation in the middle
 *
 */
public class LoadingAnimationPane extends JPanel {

    BufferedImage blank = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    private boolean shouldRepaint = false;
    BufferedImage currentImage = blank;

    LoadingAnimationPane() {
        this.setOpaque(false);
        ErrorImages.addPanel(this);//should have finalizer to remove this from via ErrorImages.removePanel(this) which should be created
    }
    public void stopAnimation(){
        boolean wasPlaying = shouldRepaint;
        shouldRepaint = false;
        currentImage = blank;
        if(wasPlaying) repaint();
    }
    public void startAnimation(){
        shouldRepaint=true;
    }
    boolean shouldRepaint(){
        return shouldRepaint;
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (shouldRepaint) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            currentImage = ErrorImages.getLoading();
            Dimension loadingWH = ImageUtils.scaleToMax(currentImage.getWidth(), currentImage.getHeight(), getWidth(), getHeight());
            g2.drawImage(currentImage, ((getWidth() - loadingWH.width) / 2), ((getHeight() - loadingWH.height) / 2), loadingWH.width, loadingWH.height, this);
        }
        //g2.dispose();?
    }
}
