import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Client extends Thread {
    private String ip;
    private int port;
    private Socket connection;
    private GamePanel gamePanel;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Gson gson;

    public Client(GamePanel panel, String ip, int port) {
        this.gamePanel = panel;
        this.ip = ip;
        this.port = port;

        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void run() {
        try {
            connection = new Socket( ip, port );

            os = new ObjectOutputStream( connection.getOutputStream() );
            is = new ObjectInputStream( connection.getInputStream() );
            os.flush();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        while( connection.isConnected() ) {
            try {
                /*
                 * This method blocks until there is data to read
                 */
                String serverData = (String)is.readObject();

                parseJsonData(serverData);

            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    private void parseJsonData(String jsonGameData) {
        GameData data = this.gson.fromJson(jsonGameData, GameData.class);

        if( this.gamePanel.currentState == this.gamePanel.END && data.currentState == this.gamePanel.MENU ) {
            this.gamePanel.resetGameData();
            this.gamePanel.repaint();
        }
        
        this.gamePanel.currentState = data.currentState;
        
        for( String name : data.scores.keySet() ) {
            this.gamePanel.scores.put(name, data.scores.get(name));
        }

        synchronized(ObjectManager.alienLock) {
            this.gamePanel.aliens.clear();
            
            for( Alien alien : data.aliens ) {
                Alien newAlien = new Alien(alien.x, alien.y);
                this.gamePanel.aliens.add(newAlien);
            }
        }

        synchronized(ObjectManager.projectileLock) {
            this.gamePanel.projectiles.clear();
            
            for( Projectile projectile : data.projectiles ) {
                Projectile newProjectile = new Projectile(projectile.name, projectile.x, projectile.y);
                this.gamePanel.projectiles.add(newProjectile);
            }
        }

        synchronized(ObjectManager.rocketshipLock) {
            if( this.gamePanel.currentState == this.gamePanel.MENU ) {
                /*
                 * Only allow adding new rocket ships in menu,
                 * before game starts
                 */
                for( String rocketshipName : data.rocketships.keySet() ) {
                    if( !this.gamePanel.rocketships.containsKey(rocketshipName) ) {
                        Rocketship rocketshipData = data.rocketships.get(rocketshipName);
                        Rocketship newRocketship = new Rocketship(rocketshipData.name, rocketshipData.x, rocketshipData.y);
                        this.gamePanel.rocketships.put(newRocketship.name, newRocketship);
                    }
                }
            } else if( this.gamePanel.currentState == this.gamePanel.GAME ) {
                /*
                 * During the game, update positions of existing rocket ships
                 * or remove them if they are destroyed
                 */
                for( String rocketshipName : this.gamePanel.rocketships.keySet() ) {
                    
                    if( data.rocketships.containsKey(rocketshipName) ) {
                        Rocketship existingRocketship = this.gamePanel.rocketships.get(rocketshipName);
                        Rocketship newRocketshipData = data.rocketships.get(rocketshipName);
                        
                        existingRocketship.isActive = newRocketshipData.isActive;
                        
                        /*
                         * Don't update this client's rocket ship position from
                         * the server data. The client's data is most up to date.
                         */
                        if( !existingRocketship.name.equals(this.gamePanel.myRocketship.name) ) {
                            existingRocketship.x = newRocketshipData.x;
                            existingRocketship.y = newRocketshipData.y;
                        }
                    } else {
                        this.gamePanel.rocketships.remove(rocketshipName);
                    }
                }
            }
        }
    }

    private String toJsonData() {
        /*
         * Only pass data (position and new projectiles) on this
         * client rocket ship (no others). Server updates all other positions
         * and handles all collisions
         */
        Map<String, Rocketship> myRocketshipMap = new HashMap<>();
        myRocketshipMap.put(this.gamePanel.myRocketship.name, this.gamePanel.myRocketship);

        GameData data = new GameData(-1, null, null, myRocketshipMap, null);
        String json = gson.toJson(data);

        //System.out.println(json);
        return json;
    }

    /*
     * Call this method in the game panel to send updates to the server
     */
    public void sendMessage() {
        String jsonData = toJsonData();

        try {
            if( os != null ) {
                os.writeObject( jsonData );
                os.flush();
                
                /*
                 *  After sending the server should have info on the
                 *  new projectiles so clear
                 */
                this.gamePanel.myRocketship.newProjectiles.clear();
            }
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }
}
