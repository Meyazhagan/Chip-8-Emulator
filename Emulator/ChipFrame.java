package Emulator;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import chip8.*;

public class ChipFrame extends JFrame implements KeyListener{

    private static final long serialVersionUID = 1L;
    private ChipPanel panel;
    private int[] keyBuffer;
    private int[] KeyIDtoKey;
    JToggleButton startButton;
    JComboBox<String> programList;
    JComboBox<String> colorList;
    
    Color c;

    ChipFrame(Chip c) {
        String[] pList = { "pong2", "tetris", "invaders" };

        startButton = new JToggleButton("Start");
        startButton.setSelected(true);

        String[] color = { "WHITE", "RED", "BLUE" , "GREEN" };

        programList = new JComboBox<>(pList);
        colorList = new JComboBox<>(color);

        setPreferredSize(new Dimension(640, 320));
        pack();
        setPreferredSize(
        new Dimension(640 + getInsets().right + getInsets().left, 320 + getInsets().top + getInsets().bottom));
        panel = new ChipPanel(c);

        setLayout(new BorderLayout());

        JPanel jb = new JPanel();
        jb.setLayout(new FlowLayout(FlowLayout.CENTER));

        jb.add(programList);
        jb.add(startButton);
        jb.add(colorList);

        add(jb, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);

        setFocusable(true);
        addKeyListener(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Chip 8 Emulator");
        pack();
        setSize(660, 400);

        keyBuffer = new int[16];
        KeyIDtoKey = new int[256];
        setVisible(true);
        fillKeys();
    }

    public void fillKeys() {
        for(int i=0; i< KeyIDtoKey.length; i++){
            KeyIDtoKey[i] = -1;
        }
        KeyIDtoKey['1'] = 1;
        KeyIDtoKey['2'] = 2;
        KeyIDtoKey['3'] = 3;
        KeyIDtoKey['Q'] = 4;
        KeyIDtoKey['W'] = 5;
        KeyIDtoKey['E'] = 6;
        KeyIDtoKey['A'] = 7;
        KeyIDtoKey['S'] = 8;
        KeyIDtoKey['D'] = 9;
        KeyIDtoKey['Z'] = 0XA;
        KeyIDtoKey['X'] = 0;
        KeyIDtoKey['C'] = 0XB;
        KeyIDtoKey['4'] = 0XC;
        KeyIDtoKey['R'] = 0XD;
        KeyIDtoKey['F'] = 0XE;
        KeyIDtoKey['V'] = 0XF;
    }
  
    public int[] getkeyBuffer(){
        return keyBuffer;
    }
    public String getProgram(){
        String data = "./Program/"+ programList.getItemAt(programList.getSelectedIndex()) + ".c8";
        return data;           
    }
    public void setColor(){
        switch(colorList.getSelectedIndex()){
            case 0 : 
                c = Color.WHITE;
                break;
            case 1 :
                c = Color.RED;
                break;
            case 2 : 
                c = Color.BLUE;
                break;
            case 3 : 
                c = Color.GREEN;
                break;
        }
        panel.setCol(c);
    }
    public boolean getStopFlag(){
        if(startButton.isSelected()){
            startButton.setText("Start");
            return true;
        }
        else{
            startButton.setText("Stop");
            return false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(KeyIDtoKey[e.getKeyCode()] != -1){
            keyBuffer[KeyIDtoKey[e.getKeyCode()]] = 1;
            // System.out.println("pressed");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(KeyIDtoKey[e.getKeyCode()] != -1){
            keyBuffer[KeyIDtoKey[e.getKeyCode()]] = 0;
            // System.out.println("released");
        }
    }
}