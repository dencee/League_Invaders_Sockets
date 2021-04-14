import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Alien extends GameObject {
    public static int ALIEN_WIDTH = 50;
    public static int ALIEN_HEIGHT = 50;
    int speedX;
    boolean variant;

    protected Alien(int x, int y) {
        super(x, y, ALIEN_WIDTH, ALIEN_HEIGHT);
        setup();
    }

    protected Alien(int x, int y, boolean isActive) {
        super(x, y, ALIEN_WIDTH, ALIEN_HEIGHT);
        super.isActive = isActive;
        setup();
    }
    
    private void setup() {
        super.speed = 4;
        this.speedX = ( new Random().nextBoolean() ) ? 3 * super.speed : 3 * -super.speed;
        this.variant = ( new Random().nextDouble() > 0.8 ) ? true : false;
        loadImage ("alien.png");
    }

    // Alien moves closer downward in the game panel
    @Override
    protected void update() {
        super.y += speed;
        
        if( this.variant ) {
            if( super.x + this.speedX < 0 || super.x + super.width > LeagueInvaders.WIDTH ) {
                this.speedX = -this.speedX;
            }
            
            super.x += this.speedX;
        }

        // Updates collisionBox in GameObject
        super.update();
    }

    // Draws alien into the panel
    protected void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillRect(x, y, width, height);
        }
    }
}
