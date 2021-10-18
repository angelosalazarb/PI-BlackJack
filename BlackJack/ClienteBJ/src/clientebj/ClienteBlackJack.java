package clientebj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import comunes.DatosBlackJack;

// TODO: Auto-generated Javadoc
/**
 * The Class ClienteBlackJack. 
 * 
 */
public class ClienteBlackJack extends JFrame implements Runnable{
	//Constantes de Interfaz Grafica
	public static final int WIDTH=770;
	public static final int HEIGHT=560;
	
	//Constantes de conexi�n con el Servidor BlackJack
	public static final int PUERTO=7377;
	public static final String IP="127.0.0.1";
	
	//variables de control del juego
	private String idYo, otroJugador, otroJugador2;
	private boolean turno, apuestaRealizada;
	private DatosBlackJack datosRecibidos;
	
	//variables para manejar la conexi�n con el Servidor BlackJack
	private Socket conexion;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	//Componentes Graficos
	private JDesktopPane containerInternalFrames;
	private VentanaEntrada ventanaEntrada;
	private VentanaEspera ventanaEspera;
	private VentanaSalaJuego ventanaSalaJuego;
	
	/**
	 * Instantiates a new cliente black jack.
	 */
	public ClienteBlackJack() {
		initGUI();
		
		//default window settings
		this.setTitle("Juego BlackJack");
		this.setSize(WIDTH, HEIGHT);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setVisible(true);
		//this.setBackground(Color.yellow);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Inits the GUI.
	 */
	private void initGUI() {
		//set up JFrame Container y Layout
        
		//Create Listeners objects
		
		//Create Control objects
		turno=false;
		apuestaRealizada=false;
		//Set up JComponents
	
		this.setBackground(SystemColor.activeCaption);
		containerInternalFrames = new JDesktopPane();
		containerInternalFrames.setOpaque(false);
		this.setContentPane(containerInternalFrames);
		adicionarInternalFrame(new VentanaEntrada(this));
	}
	
	public void adicionarInternalFrame(JInternalFrame nuevoInternalFrame) {
		add(nuevoInternalFrame);
	}
	
	public void iniciarHilo() {
		ExecutorService hiloCliente = Executors.newFixedThreadPool(1);
		hiloCliente.execute(this);
		//Thread hilo = new Thread(this);
		//hilo.start();
	}
	
	public void setIdYo(String id) {
		idYo=id;
	}
	
	public void mostrarMensajes(String mensaje) {
		System.out.println(mensaje);
	}
	
	public void enviarMensajeServidor(String mensaje) {
		try {
			out.writeObject(mensaje);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 
	public void buscarServidor() {
		mostrarMensajes("Jugador buscando al servidor...");
		
		try {
			//buscar el servidor
			conexion = new Socket(IP,PUERTO);
			//obtener flujos E/S
			out = new ObjectOutputStream(conexion.getOutputStream());
			out.flush();
			in = new ObjectInputStream(conexion.getInputStream());
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mostrarMensajes("----------------------------------------------");
		mostrarMensajes("Jugador conectado al servidor");
		mostrarMensajes("Jugador establecio Flujos E/S");
		//mandar nombre jugador
		mostrarMensajes("Jugador envio nombre "+idYo);
		mostrarMensajes("----------------------------------------------");
		enviarMensajeServidor(idYo);
		//procesar comunicacion con el ServidorBlackJack
		iniciarHilo();	
	}
	
	@Override
	public void run() {
		//datosRecibidos = new DatosBlackJack();
		// TODO Auto-generated method stub
		//mostrar bienvenida al jugador	
		   
			try {
				datosRecibidos = new DatosBlackJack();
				datosRecibidos = (DatosBlackJack) in.readObject();
				if(datosRecibidos.getIdJugadores()[0].equals(idYo)) {
					otroJugador=datosRecibidos.getIdJugadores()[1];
					otroJugador2=datosRecibidos.getIdJugadores()[2];
					turno=true;
				}else if(datosRecibidos.getIdJugadores()[1].equals(idYo)){
					otroJugador=datosRecibidos.getIdJugadores()[2];
					otroJugador2=datosRecibidos.getIdJugadores()[0];
				}
				else {
					otroJugador=datosRecibidos.getIdJugadores()[0];
					otroJugador2=datosRecibidos.getIdJugadores()[1];
				}
				this.habilitarSalaJuego(datosRecibidos);
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//buscando nombre del OtroJugador
			
			//procesar turnos
			
			while(true) {
				try {
					datosRecibidos = new DatosBlackJack();
					datosRecibidos = (DatosBlackJack)in.readObject();
					mostrarMensajes("----------------------------------------------");
					mostrarMensajes("Cliente hilo run recibiendo mensaje servidor ");
					mostrarMensajes("----------------------------------------------");
					mostrarMensajes(datosRecibidos.getJugador()+" "+datosRecibidos.getJugadorEstado());
					
					if( datosRecibidos.getJugadorEstado().equals("aposto") ) {
						System.out.println("%%%%%%%%%%%%%%%%___________");
						ventanaSalaJuego.activarBotonApostar(false);			
						ventanaSalaJuego.appendTextoAreaMensajes(datosRecibidos);
					}
					else if( datosRecibidos.getJugadorEstado().equals("todos apostaron") ) {
						//ventanaSalaJuego.appendTextoAreaMensajes(datosRecibidos);						
						ventanaSalaJuego.pintarCartasInicio(datosRecibidos);
						
						if(turno)
							ventanaSalaJuego.activarBotones(true);
					}
					else if( datosRecibidos.getJugadorEstado().equals("iniciar siguiente ronda") ) {
						
						ventanaSalaJuego.limpiarGUI(true);		
					}
					else if( datosRecibidos.getJugadorEstado().equals("desactivar limpiar") ) {
						System.out.println("ÑÑÑÑÑÑÑÑÑÑÑÑÑÑLLLL");
						ventanaSalaJuego.limpiarGUI(false);
						
						enviarMensajeServidor("iniciar nueva ronda");
					}
					else {
						ventanaSalaJuego.pintarTurno(datosRecibidos);						
					}
					
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		
	}
	
	
	private void pintarCartasInicio(DatosBlackJack datosRecibidos) {
		ventanaSalaJuego.pintarCartasInicio(datosRecibidos);
	}

	private void habilitarSalaJuego(DatosBlackJack datosRecibidos) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ventanaEspera = (VentanaEspera)containerInternalFrames.getComponent(0);
				ventanaEspera.cerrarSalaEspera();
				ventanaSalaJuego = new VentanaSalaJuego(idYo,otroJugador,otroJugador2);
				//ventanaSalaJuego.pintarCartasInicio(datosRecibidos);
				adicionarInternalFrame(ventanaSalaJuego);
                if(turno) {
                	
                	ventanaSalaJuego.activarBotonApostar(turno);
                	
                	//ventanaSalaJuego.activarBotones(turno);
                }
			}
			
		});
	}

	private void cerrarConexion() {
		// TODO Auto-generated method stub
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
  
	public void setTurno(boolean turno) {
		this.turno=turno;
	}
	
	public boolean getApuestaRealizada() {
		return this.apuestaRealizada;
	}
	
	public void setApuestaRealizada(boolean apuestaRealizada) {
		this.apuestaRealizada = apuestaRealizada;
	}
}
