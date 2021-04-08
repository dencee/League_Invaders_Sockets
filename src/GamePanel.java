import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    final int MENU = 0;
    final int GAME = 1;
    final int END = 2;

    Font titleFont, enterFont, spaceFont, scoreFont;
    BufferedImage bgImage;
    int currentState = MENU;
    Rocketship myRocketship;
    ConcurrentMap<String, Rocketship> rocketships;
    Map<String, Integer> scores;
    List<Projectile> projectiles;
    List<Alien> aliens;
    ObjectManager objectManager;
    Timer frameDraw;
    Timer alienSpawn;

    ServerSocket serverSocket;
    ArrayList<Server> servers;
    Client client;
    int port;
    boolean isServer;
    String initials;

    public GamePanel( boolean isServer, int port ) {
        titleFont = new Font("Arial", Font.PLAIN, 48);
        enterFont = new Font("Arial", Font.PLAIN, 30);
        spaceFont = new Font("Arial", Font.PLAIN, 30);
        scoreFont = new Font("Arial", Font.BOLD, 24);
        
        this.initials = JOptionPane.showInputDialog("Enter your initials").toUpperCase();
        if( this.initials.length() > 3 ) {
            this.initials = this.initials.substring(0, 3);
        }

        this.port = port;
        this.isServer = isServer;
        aliens = new ArrayList<Alien>();
        projectiles = new ArrayList<Projectile>();
        rocketships = new ConcurrentHashMap<String, Rocketship>();
        myRocketship = new Rocketship(this.initials, 50 + new Random().nextInt(400), 700);
        rocketships.put( myRocketship.name, myRocketship );
        objectManager = new ObjectManager(rocketships, projectiles, aliens);
        scores = objectManager.getScores();
        servers = new ArrayList<Server>();
        serverSocket = null;

        // Calling the image to be in the background
        try {
            bgImage = null;
            bgImage = ImageIO.read(this.getClass().getResourceAsStream("space.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: load image unsuccessful");
        }
    }

    public void start() {
        if( this.isServer ) {
            serverSetup();
        } else {
            this.client = new Client( this, Server.getIPAddress(), this.port );
        }

        // Running the programs at 60 frames per second (1000 milliseconds = 1 second)
        // Keyword "this" points to the panel itself to be redrawn again
        frameDraw = new Timer(1000 / 60, this);

        // We start drawing the frame repeatedly
        frameDraw.start();

        // Signal there's an update to be painted
        revalidate();

        /*
         * MUST be the last method call
         */
        if( this.isServer ) {
            acceptClients();
        } else {
            startClient();
        }
    }

    private void serverSetup() {
        try {
            this.serverSocket = new ServerSocket( this.port, 100 );

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String date = formatter.format( LocalDateTime.now() );
            System.out.println( date + " - Server started at: " + "Port " + this.port );
        } catch( IOException e1 ) {
            System.out.println("ERROR: Creating server socket at port: " + this.port);
            e1.printStackTrace();
        }
    }

    private void acceptClients() {

        // Stay in loop and handle any incoming connections from clients
        while( this.serverSocket != null ) {
            try {
                // This instruction blocks/listens until a connection from the client
                Socket socket = this.serverSocket.accept();

                // Connection is made, start a new server thread
                Server server = new Server( this, socket );
                this.servers.add( server );
                server.start();

            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    private void startClient() {
        client.start();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String date = formatter.format( LocalDateTime.now() );
        System.out.println( date + " - Client started at: " + Server.getIPAddress() + ", Port: " + this.port );
    }

    // Depending on the current state, either the program call drawMenuState,
    // or drawGameState, or drawEndState.
    @Override
    protected void paintComponent(Graphics g) {
        if (currentState == MENU) {
            drawMenuState(g);
        } else if (currentState == GAME) {
            drawGameState(g);
        } else if (currentState == END) {
            drawEndState(g);
        }
    }

    void updateMenuState() {

    }

    void updateGameState() {
        if( !rocketships.isEmpty() ) {
            objectManager.update();
        } else {
            currentState = END;
        }
    }

    void updateEndState() {

    }

    // The color of the window becomes blue when the game begins
    protected void drawMenuState(Graphics g) {

        // Setting the color of the window to blue
        g.setColor(Color.BLUE);

        // Inflating the window again with current width and height
        g.fillRect(0, 0, LeagueInvaders.WIDTH, LeagueInvaders.HEIGHT);

        // Sets the color of the title and the position
        g.setFont(titleFont);
        g.setColor(Color.YELLOW);
        g.drawString("LEAGUE INVADERS", 10, 200);

        g.setFont(enterFont);
        g.setColor(Color.YELLOW);
        g.drawString("Number of players: " + rocketships.size(), 100, 400);
        
        int i = 0;
        for( String name : rocketships.keySet() ) {
            g.drawString("player: " + name, 100, 450 + (50 * i++));
        }

        // Sets the color of ENTER to start and its position
        g.setFont(enterFont);
        g.setColor(Color.YELLOW);
        g.drawString("Press ENTER to start", 100, 300);

        // Sets the color of SPACE for instructions and its position
        g.setFont(spaceFont);
        g.setColor(Color.YELLOW);
        g.drawString("Press SPACE for instructions", 50, 700);
    }

    // The color of the window becomes black when the game is in session
    protected void drawGameState(Graphics g) {

        if (bgImage != null) {

            // Drawing the space image in the background
            g.drawImage(bgImage, 0, 0, LeagueInvaders.WIDTH, LeagueInvaders.HEIGHT, null);
        } else {

            // If no space image, then draw a black rectangle in the background
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, LeagueInvaders.WIDTH, LeagueInvaders.HEIGHT);
        }

        // Sets the font and string of current score
        g.setFont(scoreFont);
        g.setColor(Color.RED);

        int i = 0;
        for(String name : this.scores.keySet()) {
            g.drawString(name + " score: " + this.scores.get(name), 30, 50 + (50 * i++));
        }

        // Drawing a rocket ship in the game panel
        objectManager.draw(g);
    }

    // The color of the window becomes red when the game ends
    protected void drawEndState(Graphics g) {

        // Setting the color of the window to red
        g.setColor(Color.RED);

        // Inflating the window again with current width and height
        g.fillRect(0, 0, LeagueInvaders.WIDTH, LeagueInvaders.HEIGHT);

        // Sets the color of the title and the position
        g.setFont(titleFont);
        g.setColor(Color.YELLOW);
        g.drawString("GAME OVER", 100, 200);

        // Set the color of killed enemies and its position
        g.setFont(enterFont);
        g.setColor(Color.YELLOW);
        
        int i = 0;
        for(String name : this.scores.keySet()) {
            g.drawString(name + " killed " + this.scores.get(name) + " enemies", 100, 300 + (50 * i++));
        }
        
        // Sets the color of Enter to restart and its position
        g.setFont(spaceFont);
        g.setColor(Color.YELLOW);
        g.drawString("Press ENTER to restart", 100, 700);
    }

    // Checks the state of the game 60 frames a second a
    // and calls the appropriate method
    @Override
    public void actionPerformed(ActionEvent e) {
        if( isServer ) {
            /*
             * Send game data to server or all clients
             */
            if (currentState == MENU) {
                updateMenuState();
            } else if (currentState == GAME) {
                updateGameState();
            } else if (currentState == END) {
                updateEndState();
            }

            for( Server server : this.servers ) {
                server.sendGameData();
            }
        } else {
            /*
             * Client only moves its own rocket ship, generates projectiles,
             * and removes inactive objects.
             * All other updates and collisions handled by the server.
             */
            myRocketship.update();
            objectManager.purgeObjects();
            client.sendMessage();
        }

        // Calls repaint method and the frame becomes redrawn
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        /*
         * Data structures reset for clients done when reading state
         * transition from server
         */
        if( isServer && key == KeyEvent.VK_ENTER ) {
            if (currentState == END) {
                currentState = MENU;
                alienSpawn.stop();
                aliens.clear();
                projectiles.clear();
                rocketships.clear();
                myRocketship = new Rocketship(this.initials, 50 + new Random().nextInt(400), 700);
                rocketships.put( myRocketship.name, myRocketship );
                objectManager = new ObjectManager(rocketships, projectiles, aliens);
                scores = objectManager.getScores();
            } else {
                // Changes the current state to GAME
                currentState++;
    
                // game starts and aliens begin to spawn
                startGame();
            }
        }

        // Checks keys only if the current state of the game is GAME
        if (currentState == GAME && this.rocketships.containsKey(this.myRocketship.name) ) {
            
            if (key == KeyEvent.VK_UP) {
                myRocketship.up();
            }

            if (key == KeyEvent.VK_DOWN) {
                myRocketship.down();
            }

            if (key == KeyEvent.VK_LEFT) {
                myRocketship.left();
            }

            if (key == KeyEvent.VK_RIGHT) {
                myRocketship.right();
            }

            // A projectile is created when the space bar is pressed
            if( key == KeyEvent.VK_SPACE) {
                if( this.isServer ) {
                    projectiles.add(myRocketship.createNewProjectile());
                } else {
                    myRocketship.newProjectiles.add(myRocketship.createNewProjectile());
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if( key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT ) {
            myRocketship.resetSpeedX();
        }

        if( key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN ) {
            myRocketship.resetSpeedY();
        }
    }

    // Create aliens in the game when the game is started
    protected void startGame() {
        for( String rocketshipName : this.rocketships.keySet() ) {
            scores.put(rocketshipName, 0);
        }
        
        // spawns a new alien every second and the reference is the objectManager,
        // where the code of the alien will is implemented
        alienSpawn = new Timer(500 / this.rocketships.size(), objectManager);
        alienSpawn.start();
    }
}
