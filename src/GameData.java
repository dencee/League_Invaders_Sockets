import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class GameData {
    @Expose
    int currentState;

    @Expose
    List<Alien> aliens;

    @Expose
    List<Projectile> projectiles;
    
    @Expose
    Map<String, Rocketship> rocketships;
    
    @Expose
    Map<String, Integer> scores;

    GameData(int state, List<Alien> aliens, List<Projectile> projectiles, Map<String, Rocketship> ships, Map<String, Integer> scores){
        this.currentState = state;
        this.aliens = aliens;
        this.projectiles = projectiles;
        this.rocketships = ships;
        this.scores = scores;
    }
}
