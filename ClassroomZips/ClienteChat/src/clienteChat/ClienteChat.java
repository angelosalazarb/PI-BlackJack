package clienteChat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ClienteChat extends JFrame implements Runnable{
	private static final int ANCHO = 7;
	private static final int ALTO = 20;
	private static final int PUERTO = 6537; //Debe ser igual al del servidor
	private static final String IP="127.0.0.1"; 
	
	private JTextField campoMensaje;
	private JTextArea notificaciones;
	private Escucha escucha;
	
	//atributos para funcionar como cliente
	private Socket conexion;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public ClienteChat() {
        initGUI();
		
		this.setTitle("Cliente Chat");
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
		notificaciones.setText("Notificaciones: ");
		notificaciones.setEditable(false);
		JScrollPane scroll = new JScrollPane(notificaciones);
		
		this.add(campoMensaje,BorderLayout.NORTH);
		this.add(scroll,BorderLayout.CENTER);
		this.addWindowListener(escucha);
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
	
	public void run() {
		// TODO Auto-generated method stub
        try {
        	notificar("Buscando ServidorChat");
			conexion = new Socket(IP,PUERTO);
			
			notificar("Se econtró al servidor "+conexion.getInetAddress().getHostAddress());
			
			out = new ObjectOutputStream(conexion.getOutputStream());
			out.flush();
			in = new ObjectInputStream(conexion.getInputStream());
			notificar("Se tienen los flujos E/S");
			
			procesarComunicacion();
				
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) { 
			// TODO Auto-generated catch block
			  e.printStackTrace(); }
        finally {
			cerrarConexion();
		}

	}
   
	private void enviarDatos(String mensaje) {
		try {
			out.writeObject(mensaje);
			out.flush();
			notificar("CLIENTE>>"+mensaje);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void procesarComunicacion() throws ClassNotFoundException, IOException {
		String mensaje = "iniciando comunicación";
			
		while(!mensaje.equals("TERMINAR")){
			mensaje = (String) in.readObject();
			notificar("SERVER>>"+mensaje);
		}
		
		enviarDatos("TERMINAR");
		
	}
	
	private void cerrarConexion() {
		try {
			in.close();
			out.close();
			conexion.close();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private class Escucha extends WindowAdapter implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			// TODO Auto-generated method stub
			enviarDatos(actionEvent.getActionCommand());
			campoMensaje.setText("");
		}

		@Override
		public void windowClosing(WindowEvent e) {
			// TODO Auto-generated method stub
			enviarDatos("TERMINAR");
			System.out.print("cerrando cliente");
		}
	}
	
}

