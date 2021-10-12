package servidorChat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ServidorChat extends JFrame implements Runnable{
	private static final int ANCHO = 7;
	private static final int ALTO = 20;
	private static final int PUERTO = 6537;
	private static final int LONGITUD_COLA = 1;
	
	private JTextField campoMensaje;
	private JTextArea notificaciones;
	private Escucha escucha;
	
	//atributos para funcionar como servidor
	private ServerSocket server;
	private Socket conexionCliente;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String mensaje;
	
	public ServidorChat() {
		initGUI();
		
		this.setTitle("Servidor Chat");
		this.setResizable(false);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	private void initGUI() {
		//create Escucha
		escucha = new Escucha();
		campoMensaje = new JTextField(ANCHO);
		campoMensaje.addActionListener(escucha);
		
		notificaciones = new JTextArea(ANCHO,ALTO);
		notificaciones.setEditable(false);
		JScrollPane scroll = new JScrollPane(notificaciones);
		
		this.add(campoMensaje,BorderLayout.NORTH);
		this.add(scroll,BorderLayout.CENTER);
	}
	
	private void notificar(String mensaje) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				notificaciones.append(mensaje+"\n");
			}
		});
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
        try {
        	notificar("Creandose como servidor");
			server = new ServerSocket(PUERTO,LONGITUD_COLA);
			
			while(true) {
			   notificar("Esperando al cliente");
			   conexionCliente = server.accept();
			   
			   notificar("Estableciendo Flujos E/S");
			   notificar("con el cliente"+conexionCliente.getInetAddress().getHostAddress());
				  
			   out = new ObjectOutputStream(conexionCliente.getOutputStream()); 
			   out.flush();
			   in = new ObjectInputStream(conexionCliente.getInputStream());
				 
			   procesarComunicacionCliente();
			}
					
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			cerrarConexion();
		}
	}
	
	private void cerrarConexion() {
		notificar("Se fue el cliente");
		try {
			in.close();
			out.close();
			conexionCliente.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void enviarDatosCliente(String mensaje) {
		try {
			out.writeObject(mensaje);
			out.flush();
			notificar("SERVER>>"+mensaje);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void procesarComunicacionCliente() throws ClassNotFoundException, IOException {
	
		do {
			mensaje = (String)in.readObject();
			notificar("CLIENTE>>"+mensaje);
		}while(!mensaje.equals( "TERMINAR" )); 
		
		enviarDatosCliente("TERMINAR");
	}
	
	private class Escucha implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			// TODO Auto-generated method stub
			enviarDatosCliente(actionEvent.getActionCommand());
			campoMensaje.setText("");
		}	
	}
}