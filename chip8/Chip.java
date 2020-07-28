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

        //decode opcode

        //excute opcode
    switch(opcode & 0xf000)
    {
        case 0x0000 : {
            switch(opcode & 0x00ff)
            {
                case 0x00E0:
                { // Clears the screen.
                    for(int i =0; i< display.length; i++){
                        display[i] = 0;
                    }
                    pc += 2;
                    needRedraw = true;
                    break;
                }
                case 0x00EE: //return from subroutine
                    stackPointer--;
                    pc = (char)(stack[stackPointer] + 2);
                    break;
                default:
                    System.err.print("Undefinded function! ");
                    System.exit(0);
                    break;
            }
            break;
        }
        case 0x1000 : // 1NNN
        { // Jumps to address NNN
            pc = (char)(opcode & 0x0fff);
            break;
        }
        case 0x2000 : // 2NNN
        { // calls subroutine at 0NNN
            stack[stackPointer++] = pc;
            pc = (char)(opcode & 0x0fff); 
            break;
        }
        case 0x3000 : // 3XNN
        { // Skips the next instruction if VX equals NN. (Usually the next instruction is a jump to skip a code block)
            int x = (opcode & 0x0f00) >> 8;
            int nn = (opcode & 0x00ff);
            if(V[x] == nn)
                pc += 4;
            else
                pc += 2;
            break;
        }
        case 0x4000 : // 4XNN
        { // Skips the next instruction if VX doesn't equal NN. 
            int x = (opcode & 0x0f00) >> 8;
            int nn = (opcode & 0x00ff);
            if(V[x] != nn)
                pc += 4;
            else
                pc += 2;
            break;
        }
        case 0x5000 : // 5XY0
        { // Skips the next instruction if VX equals VY.
            int x = (opcode & 0x0f00) >> 8;
            int y = (opcode & 0x00f0) >> 4;
            if(V[x] == V[y])
                pc += 4;
            else
                pc += 2;
            break;
        }
        case 0x6000 : // 6XNN
        { // Sets VX to NN.
            int x = (opcode & 0x0f00) >> 8;
            V[x] = (char)(opcode & 0x00ff);
            pc += 2;
            break;
        }
        case 0x7000 : // 7XNN
        { // Adds NN to VX.
            int x = (opcode & 0x0f00) >> 8;
            char nn = (char)(opcode & 0x00ff);
            V[x] = (char)((V[x] + nn) & 0xff);
            pc += 2;
            break;
        }
        case 0x8000 : 
        {
        switch(opcode & 0x000f){
            case 0x0000: // 8XY0
            { // Sets VX to the value of VY.
                int x = (opcode & 0x0f00) >> 8;
                int y = (opcode & 0x00f0) >> 4;
                V[x] =  V[y];
                pc += 2;
                break;
            }
            case 0x0001: //8XY1
            { // Sets VX to VX or VY.
                int x = (opcode & 0x0f00) >> 8;
                int y = (opcode & 0x00f0) >> 4;
                V[x] = (char)(V[x] | V[y]);
                pc += 2;
                break;
            }
            case 0x0002: // 8XY2
            { // Sets VX to VX and VY.
                int x = (opcode & 0x0f00) >> 8;
                int y = (opcode & 0x00f0) >> 4;
                V[x] = (char)(V[x] & V[y]);
                pc += 2;
                break;
            }
            case 0x0003: // 8XY3
            { // Sets VX to VX or VY.
                int x = (opcode & 0x0f00) >> 8;
                int y = (opcode & 0x00f0) >> 4;
                V[x] = (char)(V[x] ^ V[y]);
                pc += 2;
                break;
            }
            case 0x0004: // 8XY4
            { // Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                int x = (opcode & 0x0f00) >> 8;
                int y = (opcode & 0x00f0) >> 4;
                if(V[y] > 0xff - V[x])
                    V[0xf] = 1;
                else
                    V[0xf] = 0;

                V[x] = (char)(V[x] + V[y] & 0xff);
                pc += 2;
                break;
            }
            case 0x0005: // 8XY5
            { // VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                int x = (opcode & 0x0f00) >> 8;
                int y = (opcode & 0x00f0) >> 4;
                if(V[x] > V[y])
                    V[0xf] = 1;
                else
                    V[0xf] = 0;
                
                V[x] = (char)(V[x] - V[y] & 0xff);
                pc += 2;
                break;
            }
            case 0x0006: // 8XY6
            { // Stores the least significant bit of VX in VF and then shifts VX to the right by 1.
                int x = (opcode & 0x0f00) >> 8;
                V[0xf] = (char)(V[x] & 0x1);
                V[x] = (char)(V[x] >> 1);
                pc += 2;
                break;
            }
            case 0x0007: // 8XY7
            { // Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                int x = (opcode & 0x0f00) >> 8;
                int y = (opcode & 0x00f0) >> 4;
                if(V[x] > V[y])
                    V[0xf] = 0;
                else
                    V[0xf] = 1;

                V[x] = (char)(V[y] - V[x] & 0xff);
                pc += 2;
                break;
            }
            case 0x000E: // 8XYE
            { // Stores the most significant bit of VX in VF and then shifts VX to the left by 1.
                int x = (opcode & 0x0f00) >> 8;
                V[0xf] = (char)(V[x] & 0x80);
                V[x] = (char)(V[x] << 1);
                pc += 2;
                break;
            }
            default :
                System.out.println("undefined function in 0x8000!");
                System.exit(0);
                break;
            }
            break;
        }
        case 0x9000 :  // 9XY0
        { // Skips the next instruction if VX doesn't equal VY.
            int x = (opcode & 0x0f00) >> 8;
            int y = (opcode & 0x00f0) >> 4;
            if(V[x] != V[y])
                pc += 4;
            else
                pc += 2;
            break;
        }
        case 0xA000 : // ANNN
        //Sets I to the address NNN.
            I = (char)(opcode & 0x0fff);
            pc += 2;
            break;
        case 0xB000 : // BNNN
        { // Jumps to the address NNN plus V0.
            int nnn = (opcode & 0x0fff);
            int extra = V[0] & 0xff;
            pc = (char)(extra+ nnn);
            break;
        }
        case 0xC000 : //CNNN
        { // Sets VX to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
            int x = (opcode & 0x0f00) >> 8;
            int nn = (opcode & 0x00ff);
            int randonnumber = new Random().nextInt(256);
            V[x] = (char)(randonnumber & nn);
            pc += 2;
            break;
        }
        
        case 0xD000 :  // DXYN
        { // Display
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

                if(display[index] ==1) V[0xf] =1;

                display[index] ^= 1;
                }
            }
            }
            pc += 2;
            needRedraw = true;
            break;
        }
        case 0xE000 : 
        {
            switch(opcode & 0x00ff)
            {
                case 0x009E: // EX9E
                { // Skips the next instruction if the key stored in VX is pressed.
                    int x = (opcode & 0x0f00) >> 8;
                    
                    if(keys[V[x]] == 1)
                        pc += 4;
                    else
                        pc += 2;
                    break;
                }
                case 0x00A1: // EXA1
                { // Skips the next instruction if the key stored in VX isn't pressed.
                    int x = (opcode & 0x0f00) >> 8;

                    if(keys[V[x]] == 0)
                        pc += 4;
                    else
                        pc += 2;
                    break;
                }
                default:
                    System.out.println("undefined funcion!");
                    System.exit(0);;
            } 
            break;
        }
        case 0xF000 : 
        {
            switch(opcode & 0x00ff)
            {
                case 0x0007: // FX07
                { // Sets VX to the value of the delay timer.
                    V[(opcode & 0x0f00) >> 8] = (char)delay_timer;
                    pc += 2;
                    break; 
                } 
                case 0x000A: // FX0A
                { // A key press is awaited, and then stored in VX.
                    int x = (opcode & 0x0f00) >> 8;
                    for(int i =0; i<keys.length; i++){
                    if(keys[i] == 1){
                        V[x] = (char)i;
                        pc += 2;
                        break;
                        }
                    }
                    break;
                }
                case 0x0015: // FX15
                { // Sets the delay timer to VX.
                    delay_timer = V[(opcode & 0x0f00)>>8]; 
                    pc += 2;   
                    break;
                }
                case 0x0029: // FX29
                { // Sets I to the location of the sprite for the character in VX. 
                    int charater = V[(opcode & 0x0f00) >> 8];
                    I = (char)(0x50 + (charater * 5));
                    pc += 2;
                    break;
                }
                case 0x0018: // FX18
                { // Sets the sound timer to VX.
                    sound_timer = V[(opcode & 0x0f00)>>8]; 
                    pc += 2;   
                    break;
                }
            case 0x001E: // FX1E
                { // Adds VX to I. VF is not affected.
                    int x = (opcode & 0x0f00) >> 8;
                    I = (char)(V[x] + I);
                    pc += 2;
                    break;
                }
            case 0x0033: // FX33
            { // Stores the binary-coded decimal representation of VX.
                int x = V[(opcode & 0x0f00) >> 8];
                int hunders = (x - (x%100)) /100;
                x -= hunders * 100;
                int tens = (x - (x%10)) /10;
                x -= tens * 10;

                memory[I] = (char)(hunders);
                memory[I + 1] = (char)(tens);
                memory[I + 2] = (char)(x);

                pc += 2;
                break;
            }
            case 0x0055: // FX55
            { // Stores V0 to VX (including VX) in memory starting at address I.
                int x = (opcode & 0x0f00) >> 8;
                for(int i = 0; i<= x ; i++){
                memory[I+i] = V[i];
                }
                pc += 2;
                break;
            }
            case 0x0065: // FX65
            { // Fills V0 to VX (including VX) with values from memory starting at address I. 
                int x = (opcode & 0x0f00) >> 8;
                for(int i = 0; i<= x ; i++){
                V[i] = memory[I + i];
                }
                I = (char)(I + x + 1);
                pc += 2;
                break;
            }
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
        if(sound_timer == 5){
            sound_timer =0;
            Audio.playSound("./program/Gun.wav");
        }
        if(sound_timer == 4){
            sound_timer =0;
            Audio.playSound("./program/Ping.wav");
        }
        if(sound_timer == 32){
            sound_timer =0;
            Audio.playSound("./program/beep.wav");
        }
        if(delay_timer > 0)
            delay_timer--;
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
            if(input != null) 
                try{ input.close(); } catch(IOException e){}
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
        while(offset < FontData.fontset.length){
            memory[0x50 + offset] = (char)(FontData.fontset[offset] & 0xff);
            offset++;
        }
    }

    public void setKey(int[] keyBuffer){
        for(int i=0; i<keys.length; i++)
            keys[i] = (byte)keyBuffer[i];
    }
}
