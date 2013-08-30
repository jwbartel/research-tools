package retriever.view;

import retriever.DataSender;
import retriever.EmailRetriever;
import util.annotations.StructurePattern;
import bus.uigen.ObjectEditor;
import bus.uigen.uiFrame;
import bus.uigen.introspect.AttributeNames;


@StructurePattern("Bean Pattern")
public class EmailDataRetriever{
	
	EmailRetriever retriever = new EmailRetriever();
	DataSender sender = new DataSender();
	
	uiFrame authenticationFrame;
	uiFrame sendingFrame;
	
	static{
		ObjectEditor.setPropertyAttribute(EmailRetriever.class, "Messages",
				AttributeNames.SCROLLED, true); 
		ObjectEditor.setPropertyAttribute(DataSender.class, "DataToSend",
				AttributeNames.SCROLLED, true); 
		
		//Changing widgets
		ObjectEditor.setPropertyAttribute(EmailRetriever.class, "Messages", 
				AttributeNames.UNPARSE_AS_TO_STRING, false); 
		ObjectEditor.setPreferredWidget(EmailRetriever.class, "Messages", javax.swing.JTextArea.class);
		ObjectEditor.setPreferredWidget(EmailRetriever.class, "Password", javax.swing.JPasswordField.class);


		ObjectEditor.setPropertyAttribute(DataSender.class, "DataToSend", 
				AttributeNames.UNPARSE_AS_TO_STRING, false); 
		ObjectEditor.setPreferredWidget(DataSender.class, "DataToSend", javax.swing.JTextArea.class);
		
		
		//Sizes
		ObjectEditor.setAttributeOfAllProperties(EmailRetriever.class,
				AttributeNames.LABEL_WIDTH, 115);
		ObjectEditor.setAttributeOfAllProperties(EmailRetriever.class,
				AttributeNames.COMPONENT_WIDTH, 200);
		ObjectEditor.setPropertyAttribute(EmailRetriever.class, "Messages", 
				AttributeNames.COMPONENT_WIDTH, 350);
		ObjectEditor.setPropertyAttribute(EmailRetriever.class, "Messages", 
				AttributeNames.COMPONENT_HEIGHT, 175);

		ObjectEditor.setAttributeOfAllProperties(DataSender.class,
				AttributeNames.LABEL_WIDTH, 115);
		ObjectEditor.setAttributeOfAllProperties(DataSender.class,
				AttributeNames.COMPONENT_WIDTH, 200);
		
		//Attribute positions in interface
		ObjectEditor.setPropertyAttribute(EmailRetriever.class, "ImapServer", 
				AttributeNames.POSITION, 0);
		ObjectEditor.setPropertyAttribute(EmailRetriever.class, "EmailAddress", 
				AttributeNames.POSITION, 1);
		ObjectEditor.setPropertyAttribute(EmailRetriever.class, "Password", 
				AttributeNames.POSITION, 2);
		ObjectEditor.setPropertyAttribute(EmailRetriever.class, "Messages", 
				AttributeNames.POSITION, 3);

		ObjectEditor.setPropertyAttribute(DataSender.class, "EmailAddress", 
				AttributeNames.POSITION, 0);
		ObjectEditor.setPropertyAttribute(DataSender.class, "DataToSend",
				AttributeNames.POSITION, 1);
	}
	
	public EmailDataRetriever() {
		sendingFrame = ObjectEditor.edit(sender);
		sendingFrame.setSize(375, 300);
		authenticationFrame = ObjectEditor.edit(retriever);
		authenticationFrame.setSize(375, 300);
		//sendingFrame.setVisible(false);
		//authenticationFrame.setVisible(false);
	}
	
	public void setAuthenticationFrame(uiFrame frame){
		this.authenticationFrame = frame;
	}
	
	public static void main(String[] args){
		
		EmailDataRetriever retreiver = new EmailDataRetriever();
	}
}
