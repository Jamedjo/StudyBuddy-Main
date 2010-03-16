import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class ImageSelectPane extends JPanel {
    private JScrollPane jScrollPane3;
    private JTable imageTable;
    private String[] imageIDs;
    private ImageReference[] imageList;
    private final int thumbSize =80;
    final int noColumns=3;

    public ImageSelectPane(GUI mainGUI) {
        setLayout(new BorderLayout());

        this.setBackground(Color.blue);
        imageIDs = mainGUI.mainImageDB.getAllImageIDs();
        imageList = new ImageReference[imageIDs.length];
	for(int i=0; i<imageIDs.length;i++){
	    imageList[i] = new ImageReference(mainGUI.mainImageDB.getImageFilename(imageIDs[i]),mainGUI);
	}
        jScrollPane3 = new JScrollPane();
        Object[][] thumbIcons = {
                    {0,1,2},
                    {3,4,5},
                    {6,7,8},
                    {9,10,11}
                };
        String[] headers = new String[thumbIcons[0].length];//or is it thumbIcons[0].length?
        for(int i=0;i<headers.length;i++) headers[i] = "Images";
        imageTable = new JTable(new DefaultTableModel(thumbIcons, headers) {
            public Class getColumnClass(int columnIndex) {
                return ThumbIcon.class;
            }
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        imageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imageTable.setSelectionModel(new DefaultListSelectionModel(){

        });
        imageTable.setRowSelectionAllowed(false);
        imageTable.setColumnSelectionAllowed(false);
        imageTable.setCellSelectionEnabled(true);
        imageTable.getColumnModel().getColumn(0).setMaxWidth(0);
        imageTable.getColumnModel().getColumn(0).setMinWidth(0);
        for(int i=0;i<headers.length;i++) {
            imageTable.getColumnModel().getColumn(i).setWidth(thumbSize);
            imageTable.getColumnModel().getColumn(i).setMinWidth(thumbSize);
            imageTable.getColumnModel().getColumn(i).setMaxWidth(thumbSize);
        }
        imageTable.setDefaultRenderer(ThumbIcon.class, new ThumbIcon(this,thumbSize,SysIcon.Tick.getBufferedImage(1.5, BufferedImage.TYPE_INT_ARGB),imageList));
        imageTable.setRowHeight(thumbSize);
        //imageTable
//      imageTable.setShowGrid(false);imageTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        imageTable.setPreferredScrollableViewportSize(imageTable.getPreferredSize());
        jScrollPane3.setViewportView(imageTable);
        add(jScrollPane3, BorderLayout.CENTER);
        imageTable.setTableHeader(null);
        
        this.setMinimumSize(new Dimension(noColumns*thumbSize,thumbSize));
        invalidate();
        this.repaint();
    }

    public String getImageID(int row,int column){
        return imageIDs[((ThumbIcon) imageTable.getValueAt(row, column)).thumbNumber];
    }
    BufferedImage imageFromNumber(int thumbNumber){
        return imageList[thumbNumber].getImage(ImgRequestSize.Thumb, false);
    }
    public int numberImages(){
        return imageIDs.length;
    }
}

class ThumbIcon extends JPanel implements TableCellRenderer{
    Log log = new Log();
    ImageSelectPane parent;
    int size;
    int thumbNumber;
    boolean isSelected = false;
    BufferedImage selectedImg = new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB);
    ImageReference[] imageList;

    ThumbIcon(ImageSelectPane parentPane, int squareSize,BufferedImage selectedImage,ImageReference[] imagelist){
        parent = parentPane;
        size = squareSize;
        thumbNumber = 0;
        selectedImg=selectedImage;
        imageList = imagelist;

        this.setMinimumSize(new Dimension(size,size));
    }
    
    public JPanel getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        thumbNumber= (Integer)value;
        isSelected=selected;
        setToolTipText(imageList[thumbNumber].pathFile.toString());
        return this;
    }

    //all scaling in terms of height. max size is 20 times minimum.????
    public void paintComponent(java.awt.Graphics g) {
	Dimension useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	int thumbOfsetW =0;
	int thumbOfsetH = 0;
	if(thumbNumber<parent.numberImages()){// use <= to show currentI too
            BufferedImage img = parent.imageFromNumber(thumbNumber);

	    useWH = ImageUtils.scaleToMax(img.getWidth(),img.getHeight(), size, size);
//	    thumbOfsetW= (size - useWH.width)/2;
//	    thumbOfsetH= (size - useWH.height)/2;
	    g2.drawImage(img, thumbOfsetW+((size-useWH.width)/2), thumbOfsetH+((size-useWH.height)/2),useWH.width,useWH.height, this);
            if(isSelected){
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f);
            g2.setComposite(ac);
            g2.drawImage(selectedImg, 0, 0,size,size, this);
            }
	}
    }

}