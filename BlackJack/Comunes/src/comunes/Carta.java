package comunes;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Carta implements Serializable{
    
	private static final String CARTAS_FILE="/images/cartas.png";
	private static int CARTA_WIDTH=68, CARTA_HEIGHT=90;
	private String valor;
    private String palo;
    private ImageIcon imageCard;
 	
    public Carta(String valor, String palo) {
		this.valor = valor;
		this.palo = palo;
		
		determineCardImage(valor, palo);
	}


    public void determineCardImage( String valor, String palo ) {
    	
    	int row, col, num=0;
    	
    	switch(palo) {
    	   case "T": row=0;break;
    	   case "P": row=1;break;
		   case "C": row=2;break;
		   case "D": row=3;break;
		   default: row=0;break;
		}
    	    	
    	switch(valor) {
		   case "J": col=10;break;
		   case "Q": col=11;break;
		   case "K": col=12;break;
		   case "As": col=0;break;
		   default: col = Integer.parseInt(valor) - 1 ;break;
		}
    	
    	try {
    		BufferedImage originalImage = ImageIO.read( this.getClass().getResource(CARTAS_FILE) );
    		BufferedImage card;
    		
    		if (col<=5) {
    			num=1;
				card = originalImage.getSubimage((CARTA_WIDTH+num)*col, (CARTA_HEIGHT+3)*row, CARTA_WIDTH, CARTA_HEIGHT );
			}else if(col<=7) {
				num=8;
				card = originalImage.getSubimage((CARTA_WIDTH*col)+num, (CARTA_HEIGHT+3)*row, CARTA_WIDTH, CARTA_HEIGHT );
			}else if(col<=9) {
				num=11;
				card = originalImage.getSubimage((CARTA_WIDTH*col)+num, (CARTA_HEIGHT+3)*row, CARTA_WIDTH, CARTA_HEIGHT );
			}else if(col<=11) {
				num=13;
				card = originalImage.getSubimage((CARTA_WIDTH*col)+num, (CARTA_HEIGHT+3)*row, CARTA_WIDTH, CARTA_HEIGHT );
			}
			else {
				num=14;
				card = originalImage.getSubimage((CARTA_WIDTH*col)+num, (CARTA_HEIGHT+3)*row, CARTA_WIDTH, CARTA_HEIGHT );
			}
    		    		
    		imageCard = new ImageIcon( card );
    		
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    }
    
	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getPalo() {
		return palo;
	}

	public void setPalo(String palo) {
		this.palo = palo;
	}
	
	public String toString() {
		return valor+palo;
	}

	public ImageIcon getImageCard() {
		return imageCard;
	}

	public void setImageCard(ImageIcon imageCard) {
		this.imageCard = imageCard;
	}


	public static int getCARTA_WIDTH() {
		return CARTA_WIDTH;
	}


	public static void setCARTA_WIDTH(int cARTA_WIDTH) {
		CARTA_WIDTH = cARTA_WIDTH;
	}


	public static int getCARTA_HEIGHT() {
		return CARTA_HEIGHT;
	}


	public static void setCARTA_HEIGHT(int cARTA_HEIGHT) {
		CARTA_HEIGHT = cARTA_HEIGHT;
	}

		
}
