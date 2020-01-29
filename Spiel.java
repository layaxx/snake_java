/**
 * Spielsteuerungsklasse 
 * known bugs:
 * - Main Menu requires ENTER to be pressed twice if accessed from the death screen
 * features to be implemented:
 * - Sounds
 * - auf Klassen aufteilen ??
 */
import ea.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
public class Spiel extends Game implements Ticker
{
    private Raum[][] raum;
    private int[][] matrix;
    /**   Zum Speichern von Game Informationen:
     *    0 = leeres Feld   
     *    1 = Schlange
     *    2 = Apfel
     */  
    private int score;
    private Text t;                         //  Score-Anzeige
    private Text hscore;                    //  Highscore-Anzeige
    private Text nHS;                       //  Anzeige für neuen Highscore
    private int highscore;
    private int ax;
    private int ay;
    private ArrayList<Vektor> schlange;     //  verwaltet alle von der Schlange belegten Felder in richtiger Reihenfolge
    private Vektor akt;                     //  aktuelle Bewegeungsrichtung    
    private int speed;                      //  1000 = 1 Aufruf/1000ms  
    private Knoten go;
    private Knoten spielfeld;
    private boolean ended;
    private Knoten mm;                      // Main Menu
    private boolean menu;
    private boolean pause;
    private Knoten psymbol;

    public Spiel() {
        super(400, 410, "Snake | V2");
        matrix = new int[17][17];   
        akt = new Vektor(1, 0);
        speed = 130;                        //  Speed anpassbar. kleinerer Wert => schneller
        // manager.anmelden(this, speed); siehe starten()
        ended = false;
        pause = false;
        //  Spielfeld
        raum = new Raum[17][17];
        spielfeld = new Knoten();
        for(int i = 0; i < 17; i++){
            for(int j = 0; j < 17; j++){
                raum[i][j] = new Rechteck(20 + i * 20 + i, 30 + j * 20 + j, 20, 20);
            }
        }
        for(int i = 0; i < 17; i++){
            for(int j = 0; j < 17; j++){
                spielfeld.add(raum[i][j]);
            }
        }
        // wurzel.add(spielfeld); siehe starten()
        //  Score-Anzeige
        score = 0;
        t = new Text(20, 0, 25, String.valueOf(score));
        // wurzel.add(t); siehe starten()
        //  Highscore-Anzeige
        int[] load = DateiManager.integerArrayEinlesen("highscore.eaa");
        highscore = load[0];
        hscore = new Text(100, 0, 25, "HS: " + String.valueOf(highscore));
        // wurzel.add(hscore); siehe starten()
        nHS = new Text(60, 60, 40, "new Highscore!");
        nHS.farbeSetzen("rot");
        //  Game-over Anzeige
        Text g = new Text(30, 100, 115, "GAME");
        Text o = new Text(30, 230, 120, "OVER");
        Text h = new Text(85, 385, 19, "press ENTER to continue");
        g.farbeSetzen("schwarz");
        o.farbeSetzen("schwarz");
        h.farbeSetzen("weiß");
        go = new Knoten();        
        go.add(g);
        go.add(o);
        go.add(h);
        //  Schlange
        schlange = new ArrayList<Vektor>();
        Rechteck r = null;
        for(int i = 0; i < 3; i++){
            matrix[i][9] = 1;
            r = (Rechteck) raum[i][9];
            r.farbeSetzen(new Farbe(0, 100, 0));
            schlange.add(0, new Vektor(i, 9));
        }    
        //  Apfel generieren
        ax = ThreadLocalRandom.current().nextInt(0, 17);
        ay = ThreadLocalRandom.current().nextInt(0, 17);
        while(matrix[ax][ay] != 0){
            ax = ThreadLocalRandom.current().nextInt(0, 17);
            ay = ThreadLocalRandom.current().nextInt(0, 17);
        }
        // System.out.println("Apfel 1: " + ax + " " + ay); siehe starten()
        r = (Rechteck) raum[ax][ay];
        r.farbeSetzen("Rot");
        matrix[ax][ay] = 2;
        // Main Menu
        menu = true;
        mm = new Knoten();
        mm.add(new Rechteck(30, 250, 340, 100));
        Text start = new Text(70, 245, 80, "START");
        start.farbeSetzen("schwarz");
        mm.add(start);
        Text press = new Text(155, 330, 15, "press ENTER");
        press.farbeSetzen("schwarz");
        mm.add(press);
        Text snake = new Text(30, 80, 60, "SNAKE");
        snake.fontSetzen("Snake in the Boot");
        mm.add(snake);
        mm.add(new Text(30, 370, 15, "V2 | 08.07.2018"));
        mm.add(new Text(30, 390, 12, "created by Jonas Degel, Benedikt Beck & Yannick Lang"));
        wurzel.add(mm);
        // Pause Symbol
        psymbol = new Knoten();
        psymbol.add(new Rechteck(10, 10, 8, 30));
        psymbol.add(new Rechteck(25, 10, 8, 30));
    }

    public void tasteReagieren(int tastencode) {
        switch(tastencode) {
            case 26: // Pfeil rauf
            if(akt.dY() != 1){    //  verhindert Insta-death bei "falscher" Eingabe
                akt = new Vektor(0 ,-1);
            }
            break;
            case 27: // Pfeil rechts
            if(akt.dX() != -1){   //  verhindert Insta-death bei "falscher" Eingabe
                akt = new Vektor(1 ,0);
            }
            break;
            case 28: // Pfeil runter
            if(akt.dY() != -1){   //  verhindert Insta-death bei "falscher" Eingabe
                akt = new Vektor(0 ,1);
            }
            break;
            case 29: // Pfeil links
            if(akt.dX() != 1){    //  verhindert Insta-death bei "falscher" Eingabe
                akt = new Vektor(-1 ,0);
            }
            break;
            case 31: // Enter
            if(ended){            //  verhindert Reset während Game läuft
                this.reset();
            }
            else if(menu){      
                this.starten();
            }
            break;
            case 38: // 5
            if(menu == false
            && ended){    //  verhindert Insta-death bei "falscher" Eingabe
                this.zumMenue();
            }
            else if(menu == false 
            && ended == false
            && pause == false){
                this.pause();
            }
            else if (pause){
                this.unpause();
            }
            break;
        }
    }

    // Methode zum Starten nach dem Main Menu
    public void starten(){
        System.out.println("Apfel 1: " + ax + " " + ay);
        wurzel.add(hscore);
        wurzel.add(t);
        wurzel.add(spielfeld);
        wurzel.entfernen(mm);
        menu = false;
        manager.anmelden(this, speed);
    }

    public void pause(){
        manager.anhalten(this);
        wurzel.add(psymbol);
        pause = true;
    }

    public void unpause(){
        manager.starten(this, speed);
        wurzel.entfernen(psymbol);
        pause = false;
    }
    
    public void zumMenue(){
        wurzel.add(mm);
        wurzel.entfernen(hscore);
        wurzel.entfernen(t);
        wurzel.entfernen(spielfeld);
        wurzel.entfernen(go);
        menu = true;
        manager.anhalten(this);
    }

    public void tick() {
        Vektor kopf = schlange.get(0);
        Vektor neu = kopf.summe(akt);  
        int x_neu = neu.dX();
        int y_neu = neu.dY();
        Vektor ende = schlange.get(score + 2);
        int x_ende = ende.dX();
        int y_ende = ende.dY();
        if(x_neu >=0
        && x_neu < 17
        && y_neu >= 0
        && y_neu < 17)
        {
            if(matrix[x_neu][y_neu] == 0){
                // Schlange bewegen
                matrix[x_neu][y_neu] = 1;
                Rechteck r = (Rechteck) raum[x_neu][y_neu];
                r.farbeSetzen(new Farbe(0, 100, 0));
                schlange.add(0, new Vektor(x_neu, y_neu));
                //  Ende entfernen
                r = (Rechteck) raum[x_ende][y_ende];
                r.farbeSetzen(new Farbe(255, 255, 255));
                matrix[x_ende][y_ende] = 0;
                schlange.remove(score + 3);
            }
            else if(matrix[x_neu][y_neu] == 2){
                //  Schlange bewegen
                matrix[x_neu][y_neu] = 1;
                Rechteck r = (Rechteck) raum[x_neu][y_neu];
                r.farbeSetzen(new Farbe(0, 100, 0));
                schlange.add(0, new Vektor(x_neu, y_neu));
                //  Score anpassen
                score++;
                t.inhaltSetzen(String.valueOf(score));
                //  neuen Apfel generieren
                while(matrix[ax][ay] != 0){
                    ax = ThreadLocalRandom.current().nextInt(0, 17);
                    ay = ThreadLocalRandom.current().nextInt(0, 17);
                }
                int anzahl = score + 1;
                System.out.println("Apfel " + anzahl +": " + ax + " " + ay);
                r = (Rechteck) raum[ax][ay];
                r.farbeSetzen("Rot");
                matrix[ax][ay] = 2;
            }
            else{
                wurzel.add(go);
                manager.anhalten(this);
                // Highscore?
                if(score > highscore){
                    wurzel.add(nHS);
                    this.save();
                }
                ended = true;
            }
        }
        else{
            wurzel.add(go);
            manager.anhalten(this);
            // Highscore?
            if(score > highscore){
                wurzel.add(nHS);
                this.save();
            }
            ended = true;
        } 
    }

    public static void main(String[] args) {
        new Spiel();
    }

    // Methode zum Speichern des Highscores
    public void save(){
        int[] save = new int[1];
        save[0] = score;
        DateiManager.integerArraySchreiben(save, "highscore.eaa");
    }

    // Methode zum Zurücksetzen (für neues Spiel)
    public void reset(){
        System.out.println("new Game started");
        akt = new Vektor(1, 0);
        ended = false;
        Rechteck r = null;
        matrix =  new int[17][17];        
        //  Spielfeld
        for(int derder = 0; derder < 17; derder++){
            for(int dasdas = 0; dasdas < 17; dasdas++){
                r = (Rechteck) raum[derder][dasdas];
                r.farbeSetzen(new Farbe(255, 255, 255));
            }
        }
        //  Score-Anzeige
        score = 0;
        t.inhaltSetzen(String.valueOf(score));
        //  Highscore-Anzeige
        int[] load = DateiManager.integerArrayEinlesen("highscore.eaa");
        highscore = load[0];
        hscore.inhaltSetzen("HS: " + String.valueOf(highscore));
        //  Game-over Anzeige
        wurzel.entfernen(go);
        wurzel.entfernen(nHS);
        //  Schlange
        schlange = new ArrayList<Vektor>();
        for(int i = 0; i < 3; i++){
            matrix[i][9] = 1;
            r = (Rechteck) raum[i][9];
            r.farbeSetzen(new Farbe(0, 100, 0));
            schlange.add(0, new Vektor(i, 9));
        }    
        //  Apfel generieren
        ax = ThreadLocalRandom.current().nextInt(0, 17);
        ay = ThreadLocalRandom.current().nextInt(0, 17);
        while(matrix[ax][ay] != 0){
            ax = ThreadLocalRandom.current().nextInt(0, 17);
            ay = ThreadLocalRandom.current().nextInt(0, 17);
        }
        System.out.println("Apfel 1: " + ax + " " + ay);
        r = (Rechteck) raum[ax][ay];
        r.farbeSetzen("Rot");
        matrix[ax][ay] = 2;
        manager.starten(this, speed);
    }
}
