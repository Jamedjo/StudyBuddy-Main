
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
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
    AffineTransform affine = new AffineTransform();
    AffineTransform graphicsAffine = new AffineTransform();
    double angle=0;
    double scale=2;
    Dimension loadingWH=new Dimension(0,0);

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

    void updatetAffine(){
        angle+=Math.toRadians(10);
        onResize();
    }

    void onResize(){
        int anchorX=(getWidth()/2);//seperate getWidth for different panels. e.g. using getParent().getWidth() for main loadingPane, but not for bluetooth loadingPane
        int anchorY=(getHeight()/2);

        affine= new AffineTransform(graphicsAffine);
        affine.rotate(angle,anchorX, anchorY);

        repaint();
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (shouldRepaint) {
            Graphics2D g2 = (Graphics2D) g;
            graphicsAffine=g2.getTransform();
            //AffineTransform originalAffine = g2.getTransform();
            g2.setTransform(affine);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            currentImage = ErrorImages.getLoading();
            loadingWH = ImageUtils.scaleToMax(currentImage.getWidth(), currentImage.getHeight(), getWidth(), getHeight());
            int leftOffset=((getWidth() - ((int)(loadingWH.width/scale))) / 2);
            int topOffset=((getHeight() - ((int)(loadingWH.height/scale))) / 2);
            g2.drawImage(currentImage,leftOffset ,topOffset , ((int)(loadingWH.width/scale)), ((int)(loadingWH.height/scale)), this);
        }
        //g2.dispose();?
    }
}
