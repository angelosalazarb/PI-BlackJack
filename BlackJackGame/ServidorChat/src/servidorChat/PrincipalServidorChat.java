package servidorChat;

import java.awt.EventQueue;

public class PrincipalServidorChat {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
				
		  EventQueue.invokeLater(new Runnable() {
		  
		  @Override public void run() { 
			// TODO Auto-generated method stub ServidorChat
		     ServidorChat servidorChat = new ServidorChat(); 
		     Thread server = new Thread(servidorChat);
		     server.start();
		     }
		  });	
	}

}
