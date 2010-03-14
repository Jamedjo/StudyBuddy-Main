
import java.awt.geom.AffineTransform;

public class Transformer {
    private boolean flip =false;
    private boolean  mirror=false;
    private int rotate90 =0;
    
    void flip(){
        flip ^=true;
    }
    void mirror(){
        mirror ^=true;
    }
    void rotate90(){
       rotate90= (rotate90==3)? 0: rotate90+1;
    }
    void rotate180(){
        flip();mirror();
    }
    void rotate270(){
        rotate90= (rotate90==0)? 3: rotate90-1;
    }
    boolean isRotated(){
        return (rotate90==0)? false : true;
    }

    //gets an affine transform centred around the given width and height
    AffineTransform getAffine(AffineTransform inAffine,int width,int height){
        //if mirror or rotate: centre horizonatally
        //if flip or rotate centre vertically
        //transform ?in any order?
        
        AffineTransform affine = new AffineTransform(inAffine);

        if(mirror||isRotated()) affine.translate(width/2, 0);
        if(flip||isRotated()) affine.translate(0,height/2);

        if(mirror) affine.scale(-1, 1);
        if(flip) affine.scale(1, -1);
        if(isRotated()) affine.quadrantRotate(rotate90);

        if(mirror||isRotated()) affine.translate(-width/2, 0);
        if(flip||isRotated()) affine.translate(0,-height/2);

        return affine;
    }
}


//    private boolean preTransform(){//returns true if it is safe to transform
//        if(originalImage.fullDimensions==null){
//            Log.Print(LogType.DebugError, "Cannot rotate/flip/transform before image has loaded");
//            return false;
//        }
//        //transform.translate(-originalImage.fullDimensions.width/2, -originalImage.fullDimensions.height/2);
//        return true;
//    }
//    private void postTransform(){
//        //transform.translate(originalImage.fullDimensions.width/2, originalImage.fullDimensions.height/2);
//    }
//    void flip(){
//        if(!preTransform()) return;
//        transform.scale( 1.0, -1.0 );
//        transform.translate(0,originalImage.fullDimensions.height);
//        postTransform();
//    }
//    void mirror(){
//        if(!preTransform()) return;
//        transform.translate(-originalImage.fullDimensions.width,0);
//        transform.scale( -1.0, 1.0 );
//        postTransform();
//    }
//    void rotate90(){
//        if(!preTransform()) return;
//        //transform.quadrantRotate(1, anchorx, anchory);
//        //transform.rotate(Math.toRadians(90), anchorx, anchory);//rotate around w/2,h/2
//        postTransform();
//    }