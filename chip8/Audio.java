package chip8;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Audio {
    static int i =0;
    public static void playSound(String file){
        try{
            Clip clip = AudioSystem.getClip();
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(file));
            clip.open(audio);
            clip.start();
        }
        catch(Exception e){
            System.err.println("error in audio");
        }
        
    }
}