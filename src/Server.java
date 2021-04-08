import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Server extends Thread {
    private Socket connection;
    private GamePanel gamePanel;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Gson gson;

    public Server(GamePanel panel, Socket socket) {
        this.gamePanel = panel;
        this.connection = socket;

        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void run() {
        try {
            os = new ObjectOutputStream( this.connection.getOutputStream() );
            is = new ObjectInputStream( this.connection.getInputStream() );

            os.flush();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        while( connection.isConnected() ) {
            try {
                /*
                 *  This method blocks until there is data to read
                 */
                String clientData = (String)is.readObject();

                parseJsonData(clientData);

            } catch( ClassNotFoundException | IOException e ) {
                JOptionPane.showMessageDialog( null, "Connection Lost" );
                e.printStackTrace();
            }
        }
    }
    
    private void parseJsonData(String jsonGameData) {
        GameData gameData = this.gson.fromJson(jsonGameData, GameData.class);

        /*
         * Client should only be passing its own rocketship's data
         */
        Rocketship rocketship = gameData.rocketships.values().iterator().next();
        
        synchronized( ObjectManager.rocketshipLock ) {
            if( this.gamePanel.currentState == this.gamePanel.MENU ) {
                if( !this.gamePanel.rocketships.containsKey(rocketship.name) ) {
                    Rocketship newRocketship = new Rocketship(rocketship.name, rocketship.x, rocketship.y);
                    this.gamePanel.rocketships.put(newRocketship.name, newRocketship);
                }
            } else if( this.gamePanel.currentState == this.gamePanel.GAME ) {
                if( this.gamePanel.rocketships.containsKey(rocketship.name) ) {
                    Rocketship existingRocketship = this.gamePanel.rocketships.get(rocketship.name);
                    existingRocketship.x = rocketship.x;
                    existingRocketship.y = rocketship.y;
                    existingRocketship.isActive = rocketship.isActive;
                }
            }
        }

        /*
         * Game data projectiles List should only contain new projectiles
         * from that particular rocket ship
         */
        synchronized( ObjectManager.projectileLock ) {
            if( this.gamePanel.currentState == this.gamePanel.GAME ) {
                for( Projectile projectile : rocketship.newProjectiles ) {
                    Projectile newProjectile = new Projectile(projectile.name, projectile.x, projectile.y);
                    this.gamePanel.projectiles.add( newProjectile );
                }
            }
        }
    }

    private String toJsonData() {
        GameData data = new GameData(this.gamePanel.currentState, this.gamePanel.aliens,
                                     this.gamePanel.projectiles, this.gamePanel.rocketships, this.gamePanel.scores);
        String json = gson.toJson(data);

        //System.out.println(json);
        return json;
    }

    /*
     * Call this method in the game panel to send updates to the associated client
     */
    public void sendGameData() {
        String jsonData = toJsonData();

        try {
            if( os != null ) {
                os.writeObject( jsonData );
                os.flush();
            }
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }

    public static String getIPAddress() {
        return "76.167.223.125";
//        try {
//            return InetAddress.getLocalHost().getHostAddress();
//        } catch( UnknownHostException e ) {
//            return "ERROR!!!!!";
//        }
    }
}
