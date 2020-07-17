package chip8;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

public class Chip{
  private char[] memory; //memory 4k
  private char[] V; //register
  private char I; //address pointer
  private char pc; //program counter

  private char[] stack;
  private int stackPointer;

  private int sound_timer;
  private int delay_timer;

  private byte[] keys;
  private byte[] display;

  private boolean needRedraw;

  public void init(){
    memory = new char[4096];
    V = new char[16];
    I = 0x0;
    pc = 0x200;

    stack = new char[16];
    stackPointer = 0;

    delay_timer = 0;
    sound_timer = 0;

    keys = new byte[16];
    display = new byte[64 * 32];

    needRedraw = false;
    loadFont();
  }
  public void run(){
    //fetch data - our char is 16bit, we adding two 8bits into one.
    char opcode = (char)((memory[pc] << 8) | memory[pc+1]);
    // System.out.println("hexcode " + Integer.toHexString(opcode) + " : ");
    //decode opcode
    //excute opcode
    switch(opcode & 0xf000){
      case 0x0000 : {
        switch(opcode & 0x00ff){
            // case 0x00E0: break;
            case 0x00EE: //return from subroutine
              stackPointer--;
              pc = (char)(stack[stackPointer] + 2);
              // pc +=2;
              break;
            default:
              System.err.print("Undefinded function! ");
              System.exit(0);
              break;
        }
        break;
      }
      case 0x1000 : {//Jumps to address NNN
        pc = (char)(opcode & 0x0fff);
        // System.out.println(Integer.toHexString(opcode) + " jumping to address " + Integer.toHexString(pc).toUpperCase());
        break;}
      case 0x2000 :{//calls subroutine at 0NNN
        stack[stackPointer++] = pc;
        pc = (char)(opcode & 0x0fff); 
        // System.out.println(Integer.toHexString(opcode) + " calls subroutine address " + Integer.toHexString(pc).toUpperCase());
        break;}
      case 0x3000 :{ //Skips the next instruction if VX equals NN
        int x = (opcode & 0x0f00) >> 8;
        int nn = (opcode & 0x00ff);
        if(V[x] == nn){
          pc += 4;
          // System.out.println(Integer.toHexString(opcode) + " Skips the next instruction if " + (int)V[x] + "==" + nn);
        }
        else{
          pc += 2;
          // System.out.println(Integer.toHexString(opcode) + " not Skips the next instruction if " + (int)V[x] + "!=" + nn);
        }
        break;}
      case 0x4000 : {
        int x = (opcode & 0x0f00) >> 8;
        int nn = (opcode & 0x00ff);
        if(V[x] != nn)
          pc += 4;
        else
          pc += 2;
        break;}
      case 0x5000 : {
        int x = (opcode & 0x0f00) >> 8;
        int y = (opcode & 0x00f0) >> 4;
        if(V[x] == V[y])
          pc += 4;
        else
          pc += 2;
        break;}
      case 0x6000 : {//Sets VX to NN.
        int x = (opcode & 0x0f00) >> 8;
        V[x] = (char)(opcode & 0x00ff);
        pc += 2;
        // System.out.println(Integer.toHexString(opcode) + " sets v[" + x + "]=" + (int)V[x]);
        break;}
      case 0x7000 : {//add NN to x
        int x = (opcode & 0x0f00) >> 8;
        char nn = (char)(opcode & 0x00ff);
        V[x] = (char)((V[x] + nn) & 0xff);
        pc += 2;
        // System.out.println(Integer.toHexString(opcode) + " adds nn to x" + nn );
        break;}
      case 0x8000 : 
          {
            switch(opcode & 0x000f){
              case 0x0000:
                  {
                    int x = (opcode & 0x0f00) >> 8;
                    int y = (opcode & 0x00f0) >> 4;
                    V[x] =  V[y];
                    pc += 2;
                    break;
                  }
              case 0x0002:
                  {
                    int x = (opcode & 0x0f00) >> 8;
                    int y = (opcode & 0x00f0) >> 4;
                    V[x] = (char)(V[x] & V[y]);
                    pc += 2;
                    break;
                  }
              case 0x0004:
                  {
                    int x = (opcode & 0x0f00) >> 8;
                    int y = (opcode & 0x00f0) >> 4;
                    if(V[y] > 0xff - V[x]){
                      V[0xf] = 1;
                    }
                    else{
                      V[0xf] = 0;
                    }
                    V[x] = (char)(V[x] + V[y] & 0xff);
                    pc += 2;
                    break;
                  }
              case 0x0005:
                  {
                    int x = (opcode & 0x0f00) >> 8;
                    int y = (opcode & 0x00f0) >> 4;
                    if(V[x] > V[y]){
                      V[0xf] = 1;
                    }
                    else{
                      V[0xf] = 0;
                    }
                    V[x] = (char)(V[x] - V[y] & 0xff);
                    pc += 2;
                    break;
                  }
              default : System.out.println("undefined function in 0x8000!");
                        System.exit(0);
                        break;
            }
            break;
          }
      case 0x9000 :  {
        int x = (opcode & 0x0f00) >> 8;
        int y = (opcode & 0x00f0) >> 4;
        if(V[x] != V[y])
          pc += 4;
        else
          pc += 2;
        break;}
      case 0xA000 : //Sets I to the address NNN.
        I = (char)(opcode & 0x0fff);
        pc += 2;
        break;
      // case 0xB000 : break;
      case 0xC000 :
       {
         int x = (opcode & 0x0f00) >> 8;
         int nn = (opcode & 0x00ff);
         int randonnumber = new Random().nextInt(256);
         V[x] = (char)(randonnumber & nn);
         pc += 2;
         break;
       }
      
      case 0xD000 : {//Display
        int x = V[(opcode & 0x0f00) >> 8];
        int y = V[(opcode & 0x00f0) >> 4];
        int heigth = opcode & 0x000f;

        V[0xf] = 0;

        for(int _y =0 ; _y < heigth; _y++){
          int line = memory[I + _y];
          for(int _x =0; _x < 8; _x++){
            int pixel = line & (0x80 >> _x);
            if(pixel != 0){
              int totalX = x + _x;
              int totalY = y + _y;

              totalX = totalX % 64;
              totalY = totalY % 32;

              int index = totalY * 64 + totalX;
              //System.out.println("Display " + index);

              if(display[index] ==1) V[0xf] =1;

              display[index] ^= 1;
            }
          }
        }
        pc += 2;
        needRedraw = true;
       // System.out.println("Drawing at V[" + ((opcode & 0x0F00) >> 8) + "] = " + x + ", V[" + ((opcode & 0x00F0) >> 4) + "] = " + y);
        break;}
      case 0xE000 : 
          {
            switch(opcode & 0x00ff){
              case 0x009E: 
                {
                  int x = (opcode & 0x0f00) >> 8;
                  if(keys[V[x]] == 1){
                    pc += 4;
                    // System.out.println("if V[" + (int)V[x] + "] is pressed skips");
                  }
                  else{
                    pc += 2;
                    // System.out.println("if V[" + (int)V[x] + "] is not pressed not skips");
                  }
                  break;
                }
                case 0x00A1: 
                {
                  int x = (opcode & 0x0f00) >> 8;
                  if(keys[V[x]] == 0){
                    pc += 4;
                    // System.out.println("if V[" + (int)V[x] + "] is not pressed skips");
                  }
                  else{
                    pc += 2;
                    // System.out.println("if V[" + (int)V[x] + "] is pressed not skips");
                  }
                  break;
                }
            } 
            break;
          }
      case 0xF000 : {
        switch(opcode & 0x00ff){
          case 0x0007:{
            V[(opcode & 0x0f00) >> 8] = (char)delay_timer;
            pc += 2;
           // System.out.println("delay_timer = "+ delay_timer);
            break; 
          } 
          // case 0x000A: break;
          case 0x0015: 
            delay_timer = V[(opcode & 0x0f00)>>8]; 
            pc += 2;   
           // System.out.println("delay timer sets to " + delay_timer);
            break;
         
          case 0x0029: {
            int charater = V[(opcode & 0x0f00) >> 8];
            I = (char)(0x50 + (charater * 5));
            pc += 2;
            break;
          }
           case 0x0018: 
            sound_timer = V[(opcode & 0x0f00)>>8]; 
            pc += 2;   
           // System.out.println("sound timer sets to " + sound_timer);
            break;
          case 0x001E: 
              {
                int x = (opcode & 0x0f00) >> 8;
                I = (char)(V[x] + I);
                pc += 2;
                break;
              }
          case 0x0033: {
            int x = V[(opcode & 0x0f00) >> 8];
            int hunders = (x - (x%100)) /100;
            x -= hunders * 100;
            int tens = (x - (x%10)) /10;
            x -= tens * 10;
            memory[I] = (char)(hunders);
            memory[I + 1] = (char)(tens);
            memory[I + 2] = (char)(x);
            pc += 2;
            //System.out.println("value at V["+ x +"] sets memory as {" + hunders +"," + tens + "," + x +"}");
            break;
          }
          case 0x0055: break;
          case 0x0065: {
            int offset = 0;
            int x = (opcode & 0x0f00) >> 8;
            while(offset <= x){
              V[offset] = memory[I + offset];
              offset++;
            }
            I = (char)(I + x + 1);
            pc += 2;
            break;}
          default:
            System.err.print("Undefinded function! ");
            System.exit(0);
            break;
        }
        break;
      }
      default:
        System.err.print("Undefinded function! ");
        System.exit(0);
        break;
    }
    if(sound_timer == 4){
      sound_timer =0;
      Audio.playSound("./program/ATone.wav");
    }
    if(sound_timer == 32){
      sound_timer =0;
      Audio.playSound("./program/beep.wav");
    }
    if(delay_timer > 0){
      delay_timer--;
    }
  }

  public byte[] getDisplay(){
    return display;
  }

  public void loadProgram(String file){
    DataInputStream input = null;
    try{
    input = new DataInputStream(new FileInputStream(new File(file)));
    int offset = 0;
    while(input.available() > 0){
      memory[0x200 + offset] = (char)(input.readByte() & 0xff);
      offset++;
    }

    }
    catch(IOException e){
      e.printStackTrace();
      System.exit(0);
    }
    finally{
      if(input != null){ 
        try{ input.close(); } catch(IOException e){}
      }
    }
  }

  public boolean needRedraw(){
    return needRedraw;
  }

  public void removeDrawFlag(){
    needRedraw = false;
  }
  public void loadFont(){
    int offset = 0;
    while(offset < ChipData.fontset.length){
      memory[0x50 + offset] = (char)(ChipData.fontset[offset] & 0xff);
      offset++;
    }
  }

  public void setKey(int[] keyBuffer){
    for(int i=0; i<keys.length; i++){
      keys[i] = (byte)keyBuffer[i];
    }
  }
}