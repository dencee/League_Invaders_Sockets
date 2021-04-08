import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.google.gson.annotations.Expose;

public class GameObject {
    @Expose
    int x;

    @Expose
    int y;

    @Expose
    boolean isActive;

    int width;
    int height;
    int speed;
    Rectangle collisionBox;
    BufferedImage image = null;

    // Initializing variables in the game object
    protected GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 0;
        this.isActive = true;

        // Initializes collision box
        collisionBox = new Rectangle(x, y, width, height);
    }

    protected void update() {
        collisionBox.setBounds(x, y, width, height);
    }

    protected void loadImage(String imageFile) {
        try {
            this.image = null;
            this.image = ImageIO.read(this.getClass().getResourceAsStream(imageFile));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR: load image unsuccessful: " + imageFile);
        }
    }
}
