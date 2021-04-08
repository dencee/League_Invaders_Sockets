import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class Rocketship extends GameObject {
    public static int ROCKETSHIP_WIDTH = 50;
    public static int ROCKETSHIP_HEIGHT = 50;

    @Expose
    String name;

    @Expose
    int score;

    @Expose
    List<Projectile> newProjectiles;
    
    int speedX;
    int speedY;

    // Crating constructor and passing values to super()
    protected Rocketship(String name, int x, int y) {
        super(x, y, ROCKETSHIP_WIDTH, ROCKETSHIP_HEIGHT);
        this.name = name;
        this.speed = 10;
        this.speedX = 0;
        this.speedY = 0;
        this.score = 0;
        this.newProjectiles = new ArrayList<>();
        loadImage("rocket.png");
    }

    // Creating a blue rectangle at a specific position
    protected void draw(Graphics g) {
        // Draw the background image. Otherwise, draw a blue rectangle if there is no image present
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, width, height);
        }
    }

    @Override
    protected void update() {
        if( x + speedX >= 0 && x + width + speedX < LeagueInvaders.WIDTH ) {
            x += speedX;
        }

        if( y + speedY >= 0 && y + width + 30 + speedY < LeagueInvaders.HEIGHT ) {
            y += speedY;
        }

        super.update();
    }

    public void right() {
        this.speedX = this.speed;
    }

    public void left() {
        this.speedX = -this.speed;
    }

    public void up() {
        this.speedY = -this.speed;
    }

    public void down() {
        this.speedY = this.speed;
    }

    public void resetSpeedX() {
        this.speedX = 0;
    }

    public void resetSpeedY() {
        this.speedY = 0;
    }

    public int getScore() {
        return score;
    }
    
    public Projectile createNewProjectile() {
        return new Projectile(name, x + (width / 2), y);
    }
}
