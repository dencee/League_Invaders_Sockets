import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ObjectManager implements ActionListener {
    protected Map<String, Rocketship> rocketships;
    protected Map<String, Integer> scores;
    protected List<Projectile> projectiles;
    protected List<Alien> aliens;
    protected Random random = new Random();

    public static Object rocketshipLock = new Object();
    public static Object projectileLock = new Object();
    public static Object alienLock = new Object();

    // Initializing rocket ship
    protected ObjectManager(Map<String, Rocketship> rocketShips, List<Projectile> projectiles, List<Alien> aliens) {
        this.rocketships = rocketShips;
        this.projectiles = projectiles;
        this.aliens = aliens;
        scores = new HashMap<String, Integer>();
    }

    // Adding aliens into the game panel at a random x-position
    protected void addAlien() {
        Alien newAlien = new Alien(random.nextInt(LeagueInvaders.WIDTH - 50), 0);
        double randDouble = random.nextDouble();
        
        if( randDouble > 0.98 ) {
            newAlien.speed = 20;
        } else if( randDouble > 0.9 ) {
            newAlien.speed = 10;
        } else if( randDouble > 0.5 ) {
            newAlien.speed = 5;
        }
        
        aliens.add(newAlien);
    }

    // Object manager updates aliens and projectile independently
    protected void update() {

        synchronized ( ObjectManager.rocketshipLock ) {
            for(String shipName : this.rocketships.keySet()) {
                this.rocketships.get(shipName).update();
            }
        }

        synchronized( ObjectManager.projectileLock ) {
            for( Projectile projectile : this.projectiles ) {
                projectile.update();
    
                // if projectile is below the screen, then remove the projectile
                if (projectile.y <= 0) {
                    projectile.isActive = false;
                }
            }
        }

        synchronized( ObjectManager.alienLock ) {
            for (Alien alien : aliens) {
                alien.update();
    
                // if alien is above the screen, then remove
                if (alien.y >= LeagueInvaders.HEIGHT) {
                    alien.isActive = false;
                }
            }
        }

        // Game is over is false if the rocket ship is inactive. Otherwise, checks for
        // collisions and purges objects from the frame.
        checkCollision();
        purgeObjects();
    }

    protected void draw(Graphics g) {

        synchronized( ObjectManager.rocketshipLock ) {
            for( String shipName : rocketships.keySet() ) {
                Rocketship ship = rocketships.get(shipName);
                ship.draw(g);
                g.setColor(Color.darkGray);
                g.drawString(ship.name, ship.x, ship.y + ship.width + 20);
            }
        }

        synchronized( ObjectManager.projectileLock ) {
            for( Projectile projectile : this.projectiles ) {
                projectile.draw(g);
            }
        }

        synchronized( ObjectManager.alienLock ) {
            for (Alien alien : this.aliens) {
                alien.draw(g);
            }
        }
    }

    // Iterates through ArrayList and removes any alien or projectile marked as not
    // active.
    protected void purgeObjects() {
        
        synchronized( ObjectManager.rocketshipLock ){
            Iterator<String> rocketshipIterator = this.rocketships.keySet().iterator();
            
            while( rocketshipIterator.hasNext() ) {
                String name = rocketshipIterator.next();
                Rocketship rocketship = this.rocketships.get(name);
                
                if( !rocketship.isActive ) {
                    this.rocketships.remove(name);
                }
            }
        }
        
        // DO NOT use for loops to remove items in a loop
        synchronized( ObjectManager.alienLock ) {
            Iterator<Alien> alienIterator = this.aliens.iterator();

            while( alienIterator.hasNext() ) {
                Alien alien = alienIterator.next();

                if ( !alien.isActive ) {
                    alienIterator.remove();
                }
            }
        }

        // DO NOT use for loops to remove items in a loop
        synchronized( ObjectManager.projectileLock ) {
            Iterator<Projectile> projectileIterator = this.projectiles.iterator();

            while( projectileIterator.hasNext() ) {
                Projectile projectile = projectileIterator.next();

                if( !projectile.isActive ) {
                    projectileIterator.remove();
                }
            }
        }
    }

    // New alien appears in the game after one second
    @Override
    public void actionPerformed(ActionEvent e) {
        addAlien();
    }

    // Checks collisions between alien and rocket or alien and projectile
    protected void checkCollision() {

        for( Alien alien : this.aliens ) {

            /*
             * Checks if aliens collide with the rocket ship
             */
            for( String rocketName: this.rocketships.keySet() ) {
                Rocketship ship = this.rocketships.get(rocketName);
                
                boolean isCollision = ship.collisionBox.intersects(alien.collisionBox);

                if( isCollision ) {
                    alien.isActive = false;
                    ship.isActive = false;
                }
            }
            /*
             * Checks if projectiles collide with the aliens
             */
            for( Projectile projectile : this.projectiles ) {
                boolean isCollision = projectile.collisionBox.intersects(alien.collisionBox);
                
                if( isCollision ) {
                    alien.isActive = false;
                    projectile.isActive = false;
                    
                    int initialScore = scores.getOrDefault(projectile.name, 0);
                    scores.put(projectile.name, initialScore + 1);
                }
            }
        }
    }
    
    public Map<String, Integer> getScores() {
        return this.scores;
    }
}
