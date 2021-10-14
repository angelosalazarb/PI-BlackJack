package servidorbj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import comunes.Baraja;
import comunes.Carta;
import comunes.DatosBlackJack;

/* Clase encargada de realizar la gesti�n del juego, esto es, el manejo de turnos y estado del juego.
 * Tambi�n gestiona al jugador Dealer. 
 * El Dealer tiene una regla de funcionamiento definida:
 * Pide carta con 16 o menos y Planta con 17 o mas.
 */
public class ServidorBJ implements Runnable{
	//constantes para manejo de la conexion.
	public static final int PUERTO=7377;
	public static final String IP="127.0.0.1";
	public static final int LONGITUD_COLA=3;

	// variables para funcionar como servidor
	private ServerSocket server;
	private Socket conexionJugador;
	
	//variables para manejo de hilos
	private ExecutorService manejadorHilos;
	private Lock bloqueoJuego; 
	private Condition esperarInicio, esperarTurno, finalizar;
	private Jugador[] jugadores;
	
	//variables de control del juego
	private String[] idJugadores;
	private int jugadorEnTurno;
	//private boolean iniciarJuego;
	private Baraja mazo;
	private ArrayList<ArrayList<Carta>> manosJugadores;
	private ArrayList<Carta> manoJugador1;
	private ArrayList<Carta> manoJugador2;
	private ArrayList<Carta> manoJugador3;
	private ArrayList<Carta> manoDealer;
	private int[] valorManos;
	private ArrayList<Integer> valorApuestas;
	private DatosBlackJack datosEnviar;
	
	public ServidorBJ() {
	    //inicializar variables de control del juego
		inicializarVariablesControlRonda();
	    //inicializar las variables de manejo de hilos
		inicializareVariablesManejoHilos();
		//crear el servidor
    	try {
    		mostrarMensaje("Iniciando el servidor...");
			server = new ServerSocket(PUERTO,LONGITUD_COLA);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	
    private void inicializareVariablesManejoHilos() {
		// TODO Auto-generated method stub
    	manejadorHilos = Executors.newFixedThreadPool(LONGITUD_COLA);
		bloqueoJuego = new ReentrantLock();
		esperarInicio = bloqueoJuego.newCondition();
		esperarTurno = bloqueoJuego.newCondition();
		finalizar = bloqueoJuego.newCondition(); //Puede servir para la din�mica y l�gica del juego
		jugadores = new Jugador[LONGITUD_COLA];	
	}

	private void inicializarVariablesControlRonda() {
		// TODO Auto-generated method stub
    	 //Variables de control del juego.
		
		idJugadores = new String[LONGITUD_COLA];
		valorManos = new int[LONGITUD_COLA+1];
		valorApuestas = new ArrayList<>();
		
		mazo = new Baraja();
		Carta carta;
		
		manoJugador1 = new ArrayList<Carta>();
		manoJugador2 = new ArrayList<Carta>();
		manoJugador3 = new ArrayList<Carta>();
		manoDealer = new ArrayList<Carta>();
		
		//reparto inicial jugadores 1, 2 y 3, este bucle itera en la cantidad de cartas con las que inicia el jugador
		for(int i=1;i<=2;i++) {
		  carta = mazo.getCarta();
		  manoJugador1.add(carta);
		  calcularValorMano(carta,0);
		  carta = mazo.getCarta();
		  manoJugador2.add(carta);
		  calcularValorMano(carta,1);
		  carta = mazo.getCarta();
		  manoJugador3.add(carta);
		  calcularValorMano(carta,2);
		}
		//Carta inicial Dealer
		carta = mazo.getCarta();
		manoDealer.add(carta);
		calcularValorMano(carta,3);
		
		//gestiona las tres manos en un solo objeto para facilitar el manejo del hilo
		manosJugadores = new ArrayList<ArrayList<Carta>>(LONGITUD_COLA + 1);
		manosJugadores.add(manoJugador1);
		manosJugadores.add(manoJugador2);
		manosJugadores.add(manoJugador3);
		manosJugadores.add(manoDealer);
	}

	private void calcularValorMano(Carta carta, int i) {
		// TODO Auto-generated method stub
    	
			if(carta.getValor().equals("As")) {
				if(valorManos[i] + 11 > 21) {
					valorManos[i]+= 1;
				}
				else {
					valorManos[i]+=11;
				}
				
			}else {
				if(carta.getValor().equals("J") || carta.getValor().equals("Q")
						   || carta.getValor().equals("K")) {
					valorManos[i]+=10;
				}else {
					valorManos[i]+=Integer.parseInt(carta.getValor()); 
				}
		}
	}
	
	public void iniciar() {
       	//esperar a los clientes
    	mostrarMensaje("Esperando a los jugadores...");
    	
    	for(int i=0; i<LONGITUD_COLA;i++) {
    		try {
				conexionJugador = server.accept();
				jugadores[i] = new Jugador(conexionJugador,i);
	    		manejadorHilos.execute(jugadores[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
    	} 	
    }
    
	private void mostrarMensaje(String mensaje) {
		System.out.println(mensaje);
	}
	
	private void iniciarRondaJuego() {
		
		this.mostrarMensaje("bloqueando al servidor para despertar al jugador 1");
    	bloqueoJuego.lock();
    	
    	//despertar al jugador 1 porque es su turno
    	try {
    		this.mostrarMensaje("Despertando al jugador 1 para que inicie el juego");
        	jugadores[0].setSuspendido(false);
        	jugadores[1].setSuspendido(false);
        	esperarInicio.signal();
    	}catch(Exception e) {
    		
    	}finally {
    		this.mostrarMensaje("Desbloqueando al servidor luego de despertar al jugador 1 para que inicie el juego");
    		bloqueoJuego.unlock();
    	}			
	}
	
    private boolean seTerminoRonda() {
    	//Determinar aqu� si el dealer paga o recoge ganancias
       return false;	
    }
    
    private void analizarMensaje(String entrada, int indexJugador) {
		// TODO Auto-generated method stub
        //garantizar que solo se analice la peticion del jugador en turno.
    	while(indexJugador!=jugadorEnTurno) {
    		bloqueoJuego.lock();
    		try {
				esperarTurno.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		bloqueoJuego.unlock();
    	}
    	
    	//valida turnos para jugador 0, 1 o 2
        	
    	if(entrada.equals("pedir")) {
    		//dar carta 
    		mostrarMensaje("Se envio carta al jugador "+idJugadores[indexJugador]);
    		Carta carta = mazo.getCarta();
    		//adicionar la carta a la mano del jugador en turno
    		manosJugadores.get(indexJugador).add(carta);
    		calcularValorMano(carta, indexJugador);
    		
    		datosEnviar = new DatosBlackJack();
    		datosEnviar.setIdJugadores(idJugadores);
			datosEnviar.setValorManos(valorManos);
			datosEnviar.setCarta(carta);
			datosEnviar.setJugador(idJugadores[indexJugador]);
    		//determinar qu� sucede con la carta dada en la mano del jugador y 
			//mandar mensaje a todos los jugadores
    		if(valorManos[indexJugador]>21) {
    			//jugador Volo
	    		datosEnviar.setMensaje(idJugadores[indexJugador]+" tienes "+valorManos[indexJugador]+" estas fuera! :(");	
	    		datosEnviar.setJugadorEstado("volo");
	    		
	    		jugadores[0].enviarMensajeCliente(datosEnviar);
	    		jugadores[1].enviarMensajeCliente(datosEnviar);
	    		jugadores[2].enviarMensajeCliente(datosEnviar);
	    		
	    		//notificar a todos que jugador sigue
	    		if(jugadorEnTurno==0 || jugadorEnTurno == 1) {
	        		
	        		int jugadorSiguiente = jugadorEnTurno+1;
	        		datosEnviar = new DatosBlackJack();
		    		datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					datosEnviar.setJugador(idJugadores[jugadorSiguiente]);
					datosEnviar.setJugadorEstado("iniciar");
					datosEnviar.setMensaje(idJugadores[jugadorSiguiente]+" te toca jugar y tienes: "+valorManos[jugadorSiguiente]);
					
					jugadores[0].enviarMensajeCliente(datosEnviar);
					jugadores[1].enviarMensajeCliente(datosEnviar);
					jugadores[2].enviarMensajeCliente(datosEnviar);
					
					//levantar al jugador en espera de turno
					
					bloqueoJuego.lock();
		    		try {
						//esperarInicio.await();
						jugadores[jugadorSiguiente].setSuspendido(true);
						esperarTurno.signalAll();
						jugadorEnTurno++;
					}finally {
						bloqueoJuego.unlock();
					}
	    		}
		  			    		
	        	 else {//era el jugador 3 entonces se debe iniciar el dealer
	        		//notificar a todos que le toca jugar al dealer
	        		datosEnviar = new DatosBlackJack();
		    		datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					datosEnviar.setJugador("dealer");
					datosEnviar.setJugadorEstado("iniciar");
					datosEnviar.setMensaje("El Dealer se repartir� carta.");
					
					jugadores[0].enviarMensajeCliente(datosEnviar);
					jugadores[1].enviarMensajeCliente(datosEnviar);
					jugadores[2].enviarMensajeCliente(datosEnviar);
					
					iniciarDealer();
	        	}		
    		}else {//jugador no se pasa de 21 puede seguir jugando
    			datosEnviar.setCarta(carta);
    			datosEnviar.setJugador(idJugadores[indexJugador]);
    			datosEnviar.setMensaje(idJugadores[indexJugador]+" ahora tienes "+valorManos[indexJugador]);
	    		datosEnviar.setJugadorEstado("sigue");
	    		
	    		jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);
	    		
    		}
   
    	}
    	else if( entrada.equals("plantar") ) {
    		//jugador en turno planto
    		datosEnviar = new DatosBlackJack();
    		datosEnviar.setIdJugadores(idJugadores);
			datosEnviar.setValorManos(valorManos);
			datosEnviar.setJugador(idJugadores[indexJugador]);
    		datosEnviar.setMensaje(idJugadores[indexJugador]+" se planto");
    		datosEnviar.setJugadorEstado("planto");
    		
    		jugadores[0].enviarMensajeCliente(datosEnviar);
			jugadores[1].enviarMensajeCliente(datosEnviar);
			jugadores[2].enviarMensajeCliente(datosEnviar);
    		
    		//REVISAR ESTE
    		//notificar a todos el jugador que sigue en turno
    		if(jugadorEnTurno == 0 || jugadorEnTurno == 1) {
        		
    			int jugadorSiguiente = jugadorEnTurno+1;
        		datosEnviar = new DatosBlackJack();
	    		datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				datosEnviar.setJugador(idJugadores[jugadorSiguiente]);
				datosEnviar.setJugadorEstado("iniciar");
				datosEnviar.setMensaje(idJugadores[jugadorSiguiente]+" te toca jugar y tienes "+valorManos[jugadorSiguiente]);
				
				jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);
				
				//levantar al jugador en espera de turno
				
				bloqueoJuego.lock();
	    		try {
					//esperarInicio.await();
					jugadores[indexJugador].setSuspendido(true);
					esperarTurno.signalAll();
					jugadorEnTurno++;
				}finally {
					bloqueoJuego.unlock();
				}
        	} 
    		else {
        		//notificar a todos que le toca jugar al dealer
        		datosEnviar = new DatosBlackJack();
	    		datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				datosEnviar.setJugador("Dealer");
				datosEnviar.setJugadorEstado("iniciar");
				datosEnviar.setMensaje("El Dealer se repartir� carta");
				
				jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);
				
				iniciarDealer();
        		}	
    		
    		}
    		else {
    			//el jugador realizo su apuesta
    			
    			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&");
    			
    			
    			
    		}
    	
    	}

    
    public void iniciarDealer() {
       //le toca turno al dealer.
    	Thread dealer = new Thread(this);
    	dealer.start();
    }
    
    /*The Class Jugador. Clase interna que maneja el servidor para gestionar la comunicaci�n
     * con cada cliente Jugador que se conecte
     */
    private class Jugador implements Runnable{
        
    	//varibles para gestionar la comunicaci�n con el cliente (Jugador) conectado
        private Socket conexionCliente;
    	private ObjectOutputStream out;
    	private ObjectInputStream in;
    	private String entrada;
    	
    	//variables de control
    	private int indexJugador;
    	private boolean suspendido;
  
		public Jugador(Socket conexionCliente, int indexJugador) {
			this.conexionCliente = conexionCliente;
			this.indexJugador = indexJugador;
			suspendido = true;
			//crear los flujos de E/S
			try {
				out = new ObjectOutputStream(conexionCliente.getOutputStream());
				out.flush();
				in = new ObjectInputStream(conexionCliente.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}	 
				
		private void setSuspendido(boolean suspendido) {
			this.suspendido = suspendido;
		}
	   
		@Override
		public void run() {
			// TODO Auto-generated method stub	
			//procesar los mensajes eviados por el cliente
			
			//ver cual jugador es 
			if(indexJugador==0) {
				//es jugador 1, debe ponerse en espera a la llegada del otro jugador
				
				try { // Excepci�n por el flujo de salida
					//guarda el nombre del primer jugador
					idJugadores[0] = (String)in.readObject();
					mostrarMensaje("Hilo establecido con jugador (1) "+idJugadores[0]);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				mostrarMensaje("bloquea servidor para poner en espera de inicio al jugador 1");
				bloqueoJuego.lock(); //bloquea el servidor
				
				while(suspendido) {
					mostrarMensaje("Parando al Jugador 1 en espera del otro jugador...");
					try {
						esperarInicio.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally {
						mostrarMensaje("Desbloquea Servidor luego de bloquear al jugador 1");
						jugadores[1].setSuspendido(false);
						esperarInicio.signal();
						bloqueoJuego.unlock();
					}
				}
				
				//ya se conect� el otro jugador, 
				//le manda al jugador 1 todos los datos para montar la sala de Juego
				//le toca el turno a jugador 1
				
				mostrarMensaje("manda al jugador 1 todos los datos para montar SalaJuego");
				datosEnviar = new DatosBlackJack();
				datosEnviar.setManoDealer(manosJugadores.get(3));
				datosEnviar.setManoJugador1(manosJugadores.get(0));
				datosEnviar.setManoJugador2(manosJugadores.get(1));
				datosEnviar.setManoJugador3(manosJugadores.get(2));	
				datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				datosEnviar.setMensaje("Inicias "+idJugadores[0]+" ,tienes "+valorManos[0]);
				enviarMensajeCliente(datosEnviar);
				jugadorEnTurno=0;
			}
			 
			else if(indexJugador==1) {
				//es jugador 2, debe ponerse en espera a la llegada del otro jugador
				
				try {
					//guarda el nombre del segundo jugador
					idJugadores[1] = (String)in.readObject();
					mostrarMensaje("Hilo establecido con jugador (2) "+idJugadores[1]);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				mostrarMensaje("bloquea servidor para poner en espera de inicio al jugador 2");
				bloqueoJuego.lock(); //bloquea el servidor
				
				while(suspendido) {
					mostrarMensaje("Parando al Jugador 2 en espera del otro jugador...");
					try {
						esperarInicio.await();
					} catch (InterruptedException e) { 
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally {
						mostrarMensaje("Desbloquea Servidor luego de bloquear al jugador 2");
						bloqueoJuego.unlock();
					}
				}
				
				//ya se conect� el otro jugador, 
				//le manda al jugador 1 todos los datos para montar la sala de Juego
				//le toca el turno a jugador 2
				
				mostrarMensaje("manda al jugador 1 todos los datos para montar SalaJuego");
				datosEnviar = new DatosBlackJack();
				datosEnviar.setManoDealer(manosJugadores.get(3));
				datosEnviar.setManoJugador1(manosJugadores.get(0));
				datosEnviar.setManoJugador2(manosJugadores.get(1));
				datosEnviar.setManoJugador3(manosJugadores.get(2));	
				datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				datosEnviar.setMensaje("Inicia "+idJugadores[0]+" ,tienes "+valorManos[1]);
				enviarMensajeCliente(datosEnviar);
			}else {
				   //Es jugador 3
				   //le manda al jugador 3 todos los datos para montar la sala de Juego
				   //jugador 3 debe esperar su turno
				try {
					idJugadores[2]=(String)in.readObject();
					mostrarMensaje("Hilo jugador (3)"+idJugadores[2]);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				mostrarMensaje("manda al jugador 3 el nombre de los jugadores 2 y 1");
				
				datosEnviar = new DatosBlackJack();
				
				System.out.println("Jugador 1 " + idJugadores[0] + ":"  + manosJugadores.get(0).toString());
				System.out.println("Jugador 2 " + idJugadores[1] + ":"  + manosJugadores.get(1).toString());
				System.out.println("Jugador 3 " + idJugadores[2] + ":"  + manosJugadores.get(2).toString());
				
				datosEnviar.setManoDealer(manosJugadores.get(3));
				datosEnviar.setManoJugador1(manosJugadores.get(0));
				datosEnviar.setManoJugador2(manosJugadores.get(1));	
				datosEnviar.setManoJugador3(manosJugadores.get(2));	
				
				datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				
				datosEnviar.setMensaje("Inicia "+idJugadores[0]+" ,tienes "+valorManos[2]);
				enviarMensajeCliente(datosEnviar);
				
				iniciarRondaJuego(); //despertar al jugador 1 para iniciar el juego
				mostrarMensaje("Bloquea al servidor para poner en espera de turno al jugador 3");
				bloqueoJuego.lock();
				try {
					mostrarMensaje("Pone en espera de turno al jugador 3");
					esperarTurno.await();
					mostrarMensaje("Despierta de la espera de inicio del juego al jugador 1 y 2 ");
                    //
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					bloqueoJuego.unlock();
				}	
			}
			
			while(!seTerminoRonda()) {
				try { 
					entrada = (String) in.readObject();
					analizarMensaje(entrada,indexJugador);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//controlar cuando se cierra un cliente
				}
			}
			//cerrar conexi�n
		}
		
		public void enviarMensajeCliente(Object mensaje) {
			try {  
				out.writeObject(mensaje);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}			
    }//fin inner class Jugador      

    
    //Jugador dealer emulado por el servidor
	@Override
	public void run() {
		// TODO Auto-generated method stub
		mostrarMensaje("Inicia el dealer ...");
        boolean pedir = true;
        
        while(pedir) {
		  	Carta carta = mazo.getCarta();
			//adicionar la carta a la mano del dealer
			manosJugadores.get(3).add(carta);
			calcularValorMano(carta, 3);
			
			mostrarMensaje("El dealer recibe "+carta.toString()+" suma "+ valorManos[3]);
			

    		datosEnviar = new DatosBlackJack();
			datosEnviar.setCarta(carta);
			datosEnviar.setJugador("dealer");
				
			if(valorManos[3]<=16) {
				datosEnviar.setJugadorEstado("sigue");
				datosEnviar.setMensaje("Dealer ahora tiene "+valorManos[3]);
				mostrarMensaje("El dealer sigue jugando");
			}else {
				if(valorManos[3]>21) {
					datosEnviar.setJugadorEstado("vol�");
					datosEnviar.setMensaje("Dealer ahora tiene: "+valorManos[3]+" . Esta fuera");
					pedir=false;
					mostrarMensaje("El dealer vol�");
				}else {
					datosEnviar.setJugadorEstado("plant�");
					datosEnviar.setMensaje("Dealer ahora tiene "+valorManos[3]+" plant�");
					pedir=false;
					mostrarMensaje("El dealer plant�");
				}
			}
			//envia la jugada a los otros jugadores
			datosEnviar.setCarta(carta);
			jugadores[0].enviarMensajeCliente(datosEnviar);
			jugadores[1].enviarMensajeCliente(datosEnviar);
			jugadores[2].enviarMensajeCliente(datosEnviar);
				
        }//fin while
        
	}
    
}//Fin class ServidorBJ
