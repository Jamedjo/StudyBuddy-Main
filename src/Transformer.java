
import java.awt.geom.AffineTransform;

public class Transformer {
    private boolean flip =false;
    private boolean  mirror=false;
    private int rotate90 =0;

    int getRot90(){
        if(flip&mirror) {
            return (rotate90+2)%4;
        }
        return rotate90;
    }
    
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
    boolean isNewOrientation(){
        if((rotate90==1)||(rotate90==3)) return true;
        return false;
    }
    boolean is180(){
        if (flip&mirror){
            return (((rotate90+2)%4)==2);
        }
        return (rotate90==2);
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
