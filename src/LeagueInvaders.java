import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class LeagueInvaders {

    protected static final int WIDTH = 500;
    protected static final int HEIGHT = 800;
    protected JFrame window;
    public GamePanel gamePanel;

    public static void main(String[] args) {
        new LeagueInvaders();
    }

    // League invader constructor and initializing window
    protected LeagueInvaders() {
        window = new JFrame();
        window.setSize(WIDTH, HEIGHT);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int result = JOptionPane.showConfirmDialog(null ,"Are you hosting a game as a server?",
                "Hosting Game", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        boolean isServer = false;

        if(result == JOptionPane.YES_OPTION) {
            isServer = true;
            window.setTitle("League Invaders: Server");
        } else {
            window.setTitle("League Invaders: Client");
        }

        // Creating a game panel object
        gamePanel = new GamePanel(isServer);

        // Adding gamePanel to the window
        window.add(gamePanel);

        // Adding a key listener to the window
        window.addKeyListener(gamePanel);

        /*
         * Must put at the end
         */
        gamePanel.start();
    }
}
