package clienteChat;

import java.awt.EventQueue;

public class PrincipalclienteChat {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ClienteChat clienteChat = new ClienteChat();
				//clienteChat.ejecutarCliente();
				Thread cliente = new Thread(clienteChat);
				cliente.start();
			}
        });
	}

}
