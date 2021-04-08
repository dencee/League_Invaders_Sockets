import java.awt.Color;
import java.awt.Graphics;

import com.google.gson.annotations.Expose;

public class Projectile extends GameObject {
    public static int PROJECTILE_WIDTH = 10;
    public static int PROJECTILE_HEIGHT = 10;
    
    @Expose
    String name;

    // Calling a Projectile object and passing values to super
    protected Projectile(String name, int x, int y) {
        super(x, y, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        super.speed = 20;
        this.name = name;
        loadImage("bullet.png");
    }

    @Override
    protected void update() {
        super.y -= speed;

        // Updates collisionBox in GameObject
        super.update();
    }

    // Draws alien into the panel
    protected void draw(Graphics g) {
        // Draw the background image. Otherwise, draw a blue rectangle if there is no image present
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }
    }
}
