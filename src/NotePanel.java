import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.awt.BorderLayout;

class NotePanel extends JPanel
{
	GUI MainGUI;
	boolean Empty;
	
	// A subclass that displays a single note with save and delete buttons
	class SingleNote extends JPanel implements ActionListener
	{
		String NoteID;
		JTextArea NoteText;
		JButton SaveButton;
		JButton DeleteButton;
		JPanel ButtonPanel;
		
		SingleNote(IDTitle Note)
		{
			NoteID = Note.getID();
			NoteText = new JTextArea(Note.toString());
			SaveButton = new JButton("Save");
			DeleteButton = new JButton("Delete");
			SaveButton.addActionListener(this);
			SaveButton.setActionCommand("Save");
			DeleteButton.addActionListener(this);
			SaveButton.setActionCommand("Save");
			ButtonPanel = new JPanel();
			ButtonPanel.setLayout(new BorderLayout());
			this.setLayout(new BorderLayout());
			ButtonPanel.add(SaveButton, BorderLayout.EAST);
			ButtonPanel.add(DeleteButton, BorderLayout.WEST);
			this.add(NoteText, BorderLayout.CENTER);
			this.add(ButtonPanel, BorderLayout.SOUTH);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Record TempRecord = MainGUI.mainImageDB.getImageToNoteTable().getRecord(NoteID, 0);;
			if (e.getActionCommand() == "Save")
			{
				MainGUI.mainImageDB.deleteNote(NoteID);
				NoteID = MainGUI.mainImageDB.addImageNote(TempRecord.getField(1), NoteText.getText(),  Integer.parseInt(TempRecord.getField(3)), Integer.parseInt(TempRecord.getField(4)), Integer.parseInt(TempRecord.getField(5)), Integer.parseInt(TempRecord.getField(6)));
			}
			if (e.getActionCommand() == "Delete")
			{
				MainGUI.mainImageDB.deleteNote(NoteID);
				MainGUI.mainPanel.repaint();
				RemoveNote(this);
			}
		}
	}
	
	// Create a panel showing the notes for a certain point
	NotePanel(GUI Gui, String ImageID, int X, int Y, int XOffset, int YOffset, double Scale)
	{
		MainGUI = Gui;
		Empty = true;
		IDTitle[] PointNotes = MainGUI.mainImageDB.getNoteStringsFromImagePoint(ImageID, X, Y, XOffset, YOffset, Scale);
		this.setLayout(new GridLayout(0,1) );
		if (PointNotes.length > 0)
			Empty = false;
		for (int i=0; i<PointNotes.length; i++)
		{
			this.add(new SingleNote(PointNotes[i]));
		}
	}
	
	void RemoveNote(SingleNote NoteToRemove)
	{
		this.remove(NoteToRemove);
		this.validate();
		this.repaint();
	}
	
	boolean isEmpty() { return Empty; }

}