package clientebj;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import comunes.Carta;

public class PanelJugador extends JPanel {
	//constantes de clase
	private static final int ANCHO = 226;
	private static final int ALTO = 135; //89
	
	//variables para control del graficado
	private ArrayList<Recuerdo> dibujoRecordar;
	//private ArrayList<Carta> dibujoCartas;
	private int x;
	private boolean limpiar;
	    
	public PanelJugador(String nombreJugador) {
		//this.setBackground(Color.GREEN);
		dibujoRecordar = new ArrayList<Recuerdo>();
		//dibujoCartas = new ArrayList<>();
		this.setPreferredSize(new Dimension(ANCHO,ALTO));
		TitledBorder bordes;
		bordes = BorderFactory.createTitledBorder(nombreJugador);
		this.setBorder(bordes);
		this.limpiar=false;
	}
	
	public void pintarCartasInicio(ArrayList<Carta> manoJugador) {
		x=5;
	    for(int i=0;i<manoJugador.size();i++) {
	    	dibujoRecordar.add(new Recuerdo(manoJugador.get(i),x));
	    	x+=27;
	    }			
	    repaint();
	}
	
	public void pintarLaCarta (Carta carta) {
		dibujoRecordar.add(new Recuerdo(carta,x));
		x+=27;
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Font font =new Font(Font.DIALOG,Font.BOLD,12);
		g.setFont(font);
		
		if(limpiar==false) {
		//pinta la mano inicial
			for(int i=0;i<dibujoRecordar.size();i++) {
				//g.drawString(dibujoRecordar.get(i).getCartaRecordar().toString(), dibujoRecordar.get(i).getxRecordar(),35);
				
				g.drawImage(dibujoRecordar.get(i).getCartaRecordar().getImageCard().getImage(), dibujoRecordar.get(i).getxRecordar(), 30, null);				
			}
		}
		else {
			g.setColor(this.getBackground());
			g.fillRect(0, 0, ANCHO-2, ALTO-2);
			System.out.println("limpiado panel");
		}
	}
	
	public void limpiarPanel(boolean bool) {
		limpiar=bool;
		dibujoRecordar.clear();
		repaint();
	}
	
	public int getXCardCoord() {
		return x;
	}

	public void setXCardCoord(int x) {
		this.x = x;
	}
	
	public boolean getLimpiar() {
		return limpiar;
	}

	public void setLimpiar(boolean limpiar) {
		this.limpiar = limpiar;
	}


	private class Recuerdo{
		private Carta cartaRecordar;
		private int xRecordar;

		public Recuerdo(Carta cartaRecordar, int xRecordar) {
			this.cartaRecordar = cartaRecordar;
			this.xRecordar = xRecordar;
		}

		public Carta getCartaRecordar() {
			return cartaRecordar;
		}

		public int getxRecordar() {
			return xRecordar;
		}
	}

}
