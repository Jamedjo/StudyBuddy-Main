
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
public class LoadingAnimationPane extends JPanel {//Use enum with switch statements- would allow same class to be used for other icons and animations

    BufferedImage blank = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    private boolean shouldRepaint = false;
    BufferedImage currentImage = blank;
    AffineTransform affine = new AffineTransform();
    double angle=0;
    double scale=2;
    Dimension loadingWH=new Dimension(0,0);
    boolean useParentSize;

    LoadingAnimationPane(boolean useParentWH) {
        useParentSize=useParentWH;
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
        if(useParentSize) setPreferredSize(getParent().getPreferredSize());

        int anchorX=(getPaneWidth()/2);
        int anchorY=(getPaneHeight()/2);

        affine= new AffineTransform(((Graphics2D)getGraphics()).getTransform());
        affine.rotate(angle,anchorX, anchorY);
        repaint();
    }

    public int getPaneWidth(){
        if(useParentSize) return getParent().getParent().getWidth();
        return getWidth();
    }

    public int getPaneHeight(){
        if(useParentSize) return getParent().getParent().getHeight();
        return getHeight();
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (shouldRepaint) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setTransform(affine);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            currentImage = ErrorImages.getLoading();
            loadingWH = ImageUtils.scaleToMax(currentImage.getWidth(), currentImage.getHeight(), getPaneWidth(), getPaneHeight());
            int leftOffset=((getPaneWidth() - ((int)(loadingWH.width/scale))) / 2);
            int topOffset=((getPaneHeight() - ((int)(loadingWH.height/scale))) / 2);
            g2.drawImage(currentImage,leftOffset,topOffset, ((int)(loadingWH.width/scale)), ((int)(loadingWH.height/scale)), this);
        }
        //g2.dispose();?
    }
}
