
/**
 * Spielsteuerungsklasse 
 * known bugs:
 * - Main Menu requires ENTER to be pressed twice if accessed from the death screen
 * features to be implemented:
 * - Sounds
 */
import ea.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Spiel extends Game implements Ticker {
    private Raum[][] raum;
    private int[][] matrix;
    /**
     * To manage Information about current Game 0 = field is empty 1 = field is
     * occupied by snake 2 = field is occupied by apple
     */

    private int lengthOfPlayingField; // length of matrix & raum
    private int score;
    private int highscore;
    private int speed; // ms per tick; recommended is 130

    private Text textScore; // Displays current Score
    private Text hscore; // Displays current Highscore
    private Text textNewHighScore; // Displays new Highscore

    private ArrayList<Vektor> snake; // verwaltet alle von der Schlange belegten Felder in richtiger Reihenfolge
    private Vektor currentMovementVector;
    private Apple apple;

    private Knoten screenMainMenu; // Main Menu
    private Knoten symbolPause;
    private Knoten screenGameOver;
    private Knoten spielfeld;

    private int status; // 0 = Menu | 1 = Paused | 2 = Ended | 3 = running

    public Spiel() {
        super(400, 410, "Snake | V2.2");
        lengthOfPlayingField = 17;
        matrix = new int[lengthOfPlayingField][lengthOfPlayingField];
        currentMovementVector = new Vektor(1, 0);
        speed = 130; // Speed anpassbar. kleinerer Wert => schneller
        status = 0;

        // prepare Spielfeld
        raum = new Raum[lengthOfPlayingField][lengthOfPlayingField];
        spielfeld = new Knoten();
        for (int i = 0; i < lengthOfPlayingField; i++) {
            for (int j = 0; j < lengthOfPlayingField; j++) {
                raum[i][j] = new Rechteck(20 + i * 20 + i, 30 + j * 20 + j, 20, 20);
                spielfeld.add(raum[i][j]);
            }
        }

        // prepare Score display
        score = 0;
        textScore = new Text(20, 0, 25, String.valueOf(score));

        // prepare Highscore display
        int[] load = DateiManager.integerArrayEinlesen("highscore.eaa");
        highscore = load[0];
        hscore = new Text(100, 0, 25, "HS: " + String.valueOf(highscore));
        textNewHighScore = new Text(60, 60, 40, "new Highscore!");
        textNewHighScore.farbeSetzen("rot");

        // prepare Game-over display
        Text textGame = new Text(30, 100, 115, "GAME");
        Text textOver = new Text(30, 230, 120, "OVER");
        Text textPressEnterToContinue = new Text(85, 385, 19, "press ENTER to continue");
        textGame.farbeSetzen("schwarz");
        textOver.farbeSetzen("schwarz");
        textPressEnterToContinue.farbeSetzen("weiß");
        screenGameOver = new Knoten();
        screenGameOver.add(textGame);
        screenGameOver.add(textOver);
        screenGameOver.add(textPressEnterToContinue);

        // snake
        snake = new ArrayList<Vektor>();
        Rechteck r;
        for (int i = 0; i < 3; i++) {
            matrix[i][9] = 1;
            r = (Rechteck) raum[i][9];
            r.farbeSetzen(new Farbe(0, 100, 0));
            snake.add(0, new Vektor(i, 9));
        }

        generateNewApple();

        // prepare Main Menu
        status = 0;
        screenMainMenu = new Knoten();
        screenMainMenu.add(new Rechteck(30, 250, 340, 100));
        Text textStart = new Text(70, 245, 80, "START");
        textStart.farbeSetzen("schwarz");
        screenMainMenu.add(textStart);
        Text textPressEnter = new Text(155, 330, 15, "press ENTER");
        textPressEnter.farbeSetzen("schwarz");
        screenMainMenu.add(textPressEnter);
        Text textSnake = new Text(30, 80, 60, "SNAKE");
        textSnake.fontSetzen("Snake in the Boot");
        screenMainMenu.add(textSnake);
        screenMainMenu.add(new Text(30, 370, 15, "V2.2 | 31.01.2020"));
        screenMainMenu.add(new Text(30, 390, 12, "created by Yannick Lang"));
        wurzel.add(screenMainMenu);

        // prepar ePause Symbol
        symbolPause = new Knoten();
        symbolPause.add(new Rechteck(10, 10, 8, 30));
        symbolPause.add(new Rechteck(25, 10, 8, 30));
    }

    public void tasteReagieren(int keyCode) {
        switch (keyCode) {
        case 26: // Arrow Up
            if (currentMovementVector.dY() != 1) { // required to prevent instant death if button is pressed again
                currentMovementVector = new Vektor(0, -1);
            }
            break;
        case 27: // Arrow right
            if (currentMovementVector.dX() != -1) { // required to prevent instant death if button is pressed again
                currentMovementVector = new Vektor(1, 0);
            }
            break;
        case 28: // Arrow down
            if (currentMovementVector.dY() != -1) { // required to prevent instant death if button is pressed again
                currentMovementVector = new Vektor(0, 1);
            }
            break;
        case 29: // Arrow left
            if (currentMovementVector.dX() != 1) { // required to prevent instant death if button is pressed again
                currentMovementVector = new Vektor(-1, 0);
            }
            break;
        case 31: // Enter
            if (status == 2) { // prevents Reset while Game is running
                this.reset();
            } else if (status == 0) {
                this.starten();
            }
            break;
        case 38: // 5
            if (status == 2) { // prevents Reset while Game is Running
                this.zumMenue();
            } else if (status == 3) {
                this.pause();
            } else if (status == 1) {
                this.unpause();
            }
            break;
        }
    }

    // Method to start Game from mainMenu
    private void starten() {
        wurzel.add(hscore);
        wurzel.add(textScore);
        wurzel.add(spielfeld);
        wurzel.entfernen(screenMainMenu);
        status = 3;
        manager.anmelden(this, speed);
    }

    private void pause() {
        manager.anhalten(this);
        wurzel.add(symbolPause);
        status = 1;
    }

    private void unpause() {
        manager.starten(this, speed);
        wurzel.entfernen(symbolPause);
        status = 3;
    }

    private void zumMenue() {
        wurzel.add(screenMainMenu);
        wurzel.entfernen(hscore);
        wurzel.entfernen(textScore);
        wurzel.entfernen(spielfeld);
        wurzel.entfernen(screenGameOver);
        status = 0;
        manager.anhalten(this);
    }



    public void tick() {
        Vektor kopf = snake.get(0);
        Vektor neu = kopf.summe(currentMovementVector);
        int x_neu = neu.dX();
        int y_neu = neu.dY();
        Vektor ende = snake.get(score + 2);
        int x_ende = ende.dX();
        int y_ende = ende.dY();
        if (x_neu >= 0 && x_neu < lengthOfPlayingField && y_neu >= 0 && y_neu < lengthOfPlayingField) {
            if (matrix[x_neu][y_neu] == 0) {
                // Schlange bewegen
                matrix[x_neu][y_neu] = 1;
                Rechteck r = (Rechteck) raum[x_neu][y_neu];
                r.farbeSetzen(new Farbe(0, 100, 0));
                snake.add(0, new Vektor(x_neu, y_neu));
                // Ende entfernen
                r = (Rechteck) raum[x_ende][y_ende];
                r.farbeSetzen(new Farbe(255, 255, 255));
                matrix[x_ende][y_ende] = 0;
                snake.remove(score + 3);
            } else if (matrix[x_neu][y_neu] == 2) {
                // Schlange bewegen
                matrix[x_neu][y_neu] = 1;
                Rechteck r = (Rechteck) raum[x_neu][y_neu];
                r.farbeSetzen(new Farbe(0, 100, 0));
                snake.add(0, new Vektor(x_neu, y_neu));
                // Score anpassen
                score++;
                textScore.inhaltSetzen(String.valueOf(score));
                // neuen Apfel generieren
                generateNewApple();
            } else {
                wurzel.add(screenGameOver);
                manager.anhalten(this);
                // Highscore?
                if (score > highscore) {
                    wurzel.add(textNewHighScore);
                    this.saveHighscore();
                }
                status = 2;
            }
        } else {
            wurzel.add(screenGameOver);
            manager.anhalten(this);
            // Highscore?
            if (score > highscore) {
                wurzel.add(textNewHighScore);
                this.saveHighscore();
            }
            status = 2;
        }
    }

    public static void main(String[] args) {
        new Spiel();
    }

    // Methode zum Speichern des Highscores
    private void saveHighscore() {
        int[] save = { score };
        DateiManager.integerArraySchreiben(save, "highscore.eaa");
    }

    // Method to reset game
    private void reset() {
        System.out.println("new Game started");
        currentMovementVector = new Vektor(1, 0);
        status = 3;
        Rechteck r;
        matrix = new int[lengthOfPlayingField][lengthOfPlayingField];
        // reset Spielfeld
        for (int i = 0; i < lengthOfPlayingField; i++) {
            for (int j = 0; j < lengthOfPlayingField; j++) {
                r = (Rechteck) raum[i][j];
                r.farbeSetzen(new Farbe(255, 255, 255));
            }
        }
        // Score Display
        score = 0;
        textScore.inhaltSetzen(String.valueOf(score));
        // Highscore Display
        int[] load = DateiManager.integerArrayEinlesen("highscore.eaa");
        highscore = load[0];
        hscore.inhaltSetzen("HS: " + String.valueOf(highscore));
        // Game-over Display
        wurzel.entfernen(screenGameOver);
        wurzel.entfernen(textNewHighScore);
        // Schlange
        snake = new ArrayList<Vektor>();
        for (int i = 0; i < 3; i++) {
            matrix[i][9] = 1;
            r = (Rechteck) raum[i][9];
            r.farbeSetzen(new Farbe(0, 100, 0));
            snake.add(0, new Vektor(i, 9));
        }
        // generate new Apple
        generateNewApple();
        manager.starten(this, speed);
    }

    private void generateNewApple() {
        apple = new Apple(lengthOfPlayingField);
        while (matrix[apple.getXPosition()][apple.getYPosition()] != 0) {
            apple = new Apple(lengthOfPlayingField);
        }
        System.out.println("generated new Apple at: " + apple.getXPosition() + " " + apple.getYPosition());
        Rechteck r;
        r = (Rechteck) raum[apple.getXPosition()][apple.getYPosition()];
        r.farbeSetzen("Rot");
        matrix[apple.getXPosition()][apple.getYPosition()] = 2;
    }
}
