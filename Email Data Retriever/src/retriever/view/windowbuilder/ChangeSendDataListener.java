package retriever.view.windowbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.mail.MessagingException;

public class ChangeSendDataListener implements ActionListener {

	SendPane parent;
	
	public ChangeSendDataListener(SendPane parent){
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			parent.updateDataToSend();
		} catch (MessagingException e1) {
			e1.printStackTrace();
		}
		
	}
}
