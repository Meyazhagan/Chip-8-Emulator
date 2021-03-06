package Emulator;

import javax.swing.*;
import java.awt.*;

import chip8.Chip;

public class ChipPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private Chip chip;
    private Color c;
    
    public ChipPanel(Chip chip){
        this.chip = chip;
    }

    public void paint(Graphics g){
        byte[] display = chip.getDisplay();
        for(int i = 0; i < display.length; i++){
            if(display[i] == 0)
                g.setColor(Color.BLACK);
            else
                g.setColor(c);
            int x = i % 64;
            int y = (int)(Math.floor(i / 64));
            g.fillRect( x *10, y*10, 10, 10);
        }
    }

    public void setCol(Color c){
        this.c =  c;
    }
  
}