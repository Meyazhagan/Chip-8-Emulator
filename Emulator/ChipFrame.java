package Emulator;

import javax.swing.*;
import java.awt.*;
import chip8.*;

public class ChipFrame extends JFrame{

  private static final long serialVersionUID = 1L;
  private ChipPanel panel;

  ChipFrame(Chip c){
    setPreferredSize(new Dimension(640, 320));
    pack();
    setPreferredSize(new Dimension(640 + getInsets().right + getInsets().left,
     320 + getInsets().top + getInsets().bottom));
    panel = new ChipPanel(c);
    setLayout(new BorderLayout());
    add(panel, BorderLayout.CENTER);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle("Chip 8 Emulator");
    pack();
    setVisible(true);
    
  }
}