package Emulator;

import chip8.Chip;

public class Main extends Thread {
  Chip chip8;
  ChipFrame frame;

  public Main(){
    chip8 = new Chip();
    frame = new ChipFrame(chip8);
  }

  public void run(){
    while(true){
      chip8.init();
      chip8.loadProgram(frame.getProgram());
      while(frame.getStopFlag()){
        chip8.setKey(frame.getkeyBuffer());
        chip8.run();
        if(chip8.needRedraw()){
          frame.repaint();
          chip8.removeDrawFlag();
        }
        try{
          Thread.sleep(3);
        }
        catch(InterruptedException e){}
        }
      }
    }

  public static void main(String[] args) {
    Main main = new Main();
    main.start();
  }
}