package clientebj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import comunes.DatosBlackJack;

public class VentanaSalaJuego extends JInternalFrame {
	    
		private PanelJugador dealer, yo, jugador2, jugador3;
		private JTextArea areaMensajes;
		private JTextField valorApuesta;
		
		private JButton pedir, plantar, apostar, reiniciar;
		private JPanel panelYo, panelBotones, yoFull, panelDealer,panelJugador2, panelJugador3;
		
		private String yoId, jugador2Id, jugador3Id;
		//private DatosBlackJack datosRecibidos;
		private Escucha escucha;
		
		public VentanaSalaJuego(String yoId, String jugador2Id, String jugador3Id) {
			this.yoId = yoId;
			this.jugador2Id = jugador2Id;
			this.jugador3Id = jugador3Id;
			//this.datosRecibidos=datosRecibidos;
						
			initGUI();
			
			//default window settings
			this.setTitle("Sala de juego BlackJack - Jugador: "+yoId);
			this.pack();
			this.setLocation((ClienteBlackJack.WIDTH-this.getWidth())/2, 
			         (ClienteBlackJack.HEIGHT-this.getHeight())/2);
			this.setResizable(false);
			this.show();
		}

		private void initGUI() {
			// TODO Auto-generated method stub
			//set up JFrame Container y Layout
	        this.setBackground(Color.GREEN.darker() );
			
			
			//Create Listeners objects
			escucha = new Escucha();
			//Create Control objects
			
			GridBagConstraints constraints2 = new GridBagConstraints();
			
			//Set up JComponents
			panelDealer = new JPanel();
			panelDealer.setLayout( new GridBagLayout() );
			//panelDealer.setPreferredSize(new Dimension(206, 100 ));
			dealer = new PanelJugador("Dealer");
			constraints2.gridx=0;
			constraints2.gridy=0;
			constraints2.gridwidth=1;
			constraints2.gridheight=2;
			constraints2.fill = GridBagConstraints.VERTICAL;
			constraints2.insets = new Insets(0, 206-15, 0, 10);
			panelDealer.add(dealer, constraints2);
			add(panelDealer, BorderLayout.NORTH);		
			
			reiniciar = new JButton("Reiniciar");
			constraints2.gridx=1;
			constraints2.gridy=0;
			constraints2.gridwidth=1;
			constraints2.gridheight=1;
			constraints2.insets = new Insets(10, 80, 0, 10);
			//constraints2.fill = GridBagConstraints.VERTICAL;
			panelDealer.add(reiniciar, constraints2);
			
			panelJugador2 = new JPanel();
			jugador2= new PanelJugador(jugador2Id);	
			panelJugador2.add(jugador2);
			add(panelJugador2,BorderLayout.EAST);
			
			panelJugador3 = new JPanel();
			jugador3= new PanelJugador(jugador3Id);	
			panelJugador3.add(jugador3);
			add(panelJugador3,BorderLayout.SOUTH);	
			
			
			areaMensajes = new JTextArea(8,18);
			JScrollPane scroll = new JScrollPane(areaMensajes);	
			Border blackline;
			blackline = BorderFactory.createLineBorder(Color.black);
			TitledBorder bordes;
			bordes = BorderFactory.createTitledBorder(blackline, "Area de Mensajes");
	        bordes.setTitleJustification(TitledBorder.CENTER);
			scroll.setBorder(bordes);
			areaMensajes.setOpaque(false);
			areaMensajes.setBackground(new Color(0, 0, 0, 0));
			areaMensajes.setEditable(false);

			scroll.getViewport().setOpaque(false);
			scroll.setOpaque(false);
			add(scroll,BorderLayout.CENTER);
			
			
			
			panelYo = new JPanel();
			panelYo.setLayout(new BorderLayout());
			yo = new PanelJugador(yoId);
			panelYo.add(yo);
				
			panelBotones = new JPanel( new GridBagLayout() );
			GridBagConstraints constraints = new GridBagConstraints();
			
			pedir = new JButton("Carta");
			pedir.setEnabled(false);
			pedir.addActionListener(escucha);
			constraints.gridx=0;
			constraints.gridy=0;
			constraints.gridwidth=1;
			constraints.gridheight=1;
			panelBotones.add(pedir, constraints);
			
			plantar = new JButton("Plantar");
			plantar.setEnabled(false);
			plantar.addActionListener(escucha);
			constraints.gridx=1;
			constraints.gridy=0;
			constraints.gridwidth=1;
			constraints.gridheight=1;
			panelBotones.add(plantar, constraints);
			
			
			valorApuesta = new JTextField("0" , 7);
			constraints.gridx=0;
			constraints.gridy=1;
			constraints.gridwidth=1;
			constraints.gridheight=1;
			constraints.insets = new Insets(10, 0, 0, 5);
			panelBotones.add(valorApuesta, constraints);
			
			apostar = new JButton("Apostar");
			apostar.setEnabled(false);
			constraints.gridx=1;
			constraints.gridy=1;
			constraints.gridwidth=1;
			constraints.gridheight=1;
			constraints.insets = new Insets(10, 0, 0, 0);
			apostar.addActionListener(escucha);
			panelBotones.add(apostar, constraints);
			
			yoFull = new JPanel();
			yoFull.setLayout(new BoxLayout(yoFull, BoxLayout.Y_AXIS));
			yoFull.setPreferredSize(new Dimension(206, 200 ));
			yoFull.add(panelYo);
			yoFull.add(panelBotones);
			add(yoFull,BorderLayout.WEST);
			
			
		}
		
		public void activarBotones(boolean turno) {
			pedir.setEnabled(turno);
			plantar.setEnabled(turno);
			apostar.setEnabled(turno);
		}
		
		public void pintarCartasInicio(DatosBlackJack datosRecibidos) {
			if(datosRecibidos.getIdJugadores()[0].equals(yoId)) {
				yo.pintarCartasInicio(datosRecibidos.getManoJugador1());
				jugador2.pintarCartasInicio(datosRecibidos.getManoJugador2());
				jugador3.pintarCartasInicio(datosRecibidos.getManoJugador3());
			}
			else if(datosRecibidos.getIdJugadores()[1].equals(yoId)) {
				yo.pintarCartasInicio(datosRecibidos.getManoJugador2());
				jugador2.pintarCartasInicio(datosRecibidos.getManoJugador3());
				jugador3.pintarCartasInicio(datosRecibidos.getManoJugador1());
			}
			else {
				yo.pintarCartasInicio(datosRecibidos.getManoJugador3());
				jugador2.pintarCartasInicio(datosRecibidos.getManoJugador1());
				jugador3.pintarCartasInicio(datosRecibidos.getManoJugador2());
			}
			dealer.pintarCartasInicio(datosRecibidos.getManoDealer());
			
			areaMensajes.append(datosRecibidos.getMensaje()+"\n");
		}
		
		
		public void appendTextoAreaMensajes(DatosBlackJack datosRecibidos) {
			areaMensajes.append(datosRecibidos.getMensaje()+"\n");	
		}
		
		
		public void pintarTurno(DatosBlackJack datosRecibidos) {
			areaMensajes.append(datosRecibidos.getMensaje()+"\n");	
			ClienteBlackJack cliente = (ClienteBlackJack)this.getTopLevelAncestor();

			
			if(datosRecibidos.getJugador().contentEquals(yoId) ){
				if(datosRecibidos.getJugadorEstado().equals("iniciar")) {
					activarBotones(true);
				}else {
					if(datosRecibidos.getJugadorEstado().equals("planto") ){
						cliente.setTurno(false);
					}else {
						yo.pintarLaCarta(datosRecibidos.getCarta());
						if(datosRecibidos.getJugadorEstado().equals("volo")) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									activarBotones(false);
									cliente.setTurno(false);
								}});			
							}
						
						}
					} 
			}else {//movidas de los otros jugadores
				if(datosRecibidos.getJugador().equals(jugador2Id) ) {
					//mensaje para PanelJuego jugador2
					if(datosRecibidos.getJugadorEstado().equals("sigue")||
					   datosRecibidos.getJugadorEstado().equals("volo")) {
						jugador2.pintarLaCarta(datosRecibidos.getCarta());
					}
				}
				else if(datosRecibidos.getJugador().equals(jugador3Id) ) {
					//mensaje para PanelJuego jugador3
					if(datosRecibidos.getJugadorEstado().equals("sigue")||
						datosRecibidos.getJugadorEstado().equals("volo")) {
						jugador3.pintarLaCarta(datosRecibidos.getCarta());
						}
					}
				else {	
						
						//mensaje para PanelJuego dealer
							if(datosRecibidos.getJugadorEstado().equals("sigue") ||
							   datosRecibidos.getJugadorEstado().equals("volo")	||
							   datosRecibidos.getJugadorEstado().equals("planto")) {
								dealer.pintarLaCarta(datosRecibidos.getCarta());
							}
						
						
					}
				}
			
		}		
		
	   
	   private void enviarDatos(String mensaje) {
			// TODO Auto-generated method stub
		  ClienteBlackJack cliente = (ClienteBlackJack)this.getTopLevelAncestor();
		  cliente.enviarMensajeServidor(mensaje);
		}
	   
	   
	   private boolean esNumerico(String texto) {
		   
		   int intValue;
		   boolean flag=false;
		   
		   if(texto == null || texto.equals("")) {
		        System.out.println("String cannot be parsed, it is null or empty.");
		        return false;
		    }
		    
		    try {
		        intValue = Integer.parseInt(texto);
		        flag= true;
		    } catch (NumberFormatException e) {
		        System.out.println("Input String cannot be parsed to Integer.");
		    }
		   
		   return flag; 
	   }
	  
	   
	   private class Escucha implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			// TODO Auto-generated method stub
			if(actionEvent.getSource()==pedir) {
				//enviar pedir carta al servidor
				enviarDatos("pedir");				
			}
			else if( actionEvent.getSource()== plantar ) {
				//enviar plantar al servidor
				enviarDatos("plantar");
				activarBotones(false);
			}
			else {
				panelJugador2.setBackground(Color.orange);
				String temp = valorApuesta.getText();
				
				System.out.println( "____ jugador "+ yoId +" aposto $ " + temp);
				
				if( esNumerico(temp) )
					enviarDatos( temp );
				else
					JOptionPane.showMessageDialog(null, "LAS APUESTAS SOLO SON VALORES ENTEROS", "ERROR APUESTA", JOptionPane.ERROR_MESSAGE );
			}
		}
	   }
}
