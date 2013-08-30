package retriever.view.windowbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SubmitListener implements ActionListener {
	
	SendPane parent;
	
	public SubmitListener(SendPane parent){
		this.parent = parent;
	}
	

	@Override
	public void actionPerformed(ActionEvent arg0) {
		parent.sendData();
	}

}
