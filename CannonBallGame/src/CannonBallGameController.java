import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CannonBallGameController {

    int hits = 0; // handle the number of hits
    private int shotsFired=0; //handle the number of shots fired
    double blockerSpeed = 3; // Setting blocker speed
    boolean movingDown = true; // Direction flag for the blocker
    private Timeline cannonballTimeline; // Declare the cannonball timeline
    private int startingTime = 20; 
    private int remainingTime;
    private Timeline timer;
    
    //sound
    private AudioClip cannonFireSound;
    private AudioClip blockerHitSound;
    private AudioClip targetHitSound;     

    @FXML
    private Circle cannonBall;

    @FXML
    private Rectangle cannonBarrel;

    @FXML
    private Label score;

    @FXML
    private Pane shootCannonArea;

    
    @FXML
    private Label time;

    @FXML
    private Rectangle upperBound,LowerBound;

 // Targets
    @FXML private Rectangle blueTarget1, blueTarget2, blueTarget3, blueTarget4;
    @FXML private Rectangle yellowTarget1, yellowTarget2, yellowTarget3, yellowTarget4;
    @FXML private Rectangle blocker;
   

    private Timeline blockerTimeline;
    private Timeline targetTimeline;

    private Random random = new Random();

    // Store movement data (speed and direction) for each target
    private Map<Rectangle, TargetMovement> targetMovementMap = new HashMap<>();

    
    private static class TargetMovement {
        double speed;
        boolean movingDown;

        TargetMovement(double speed, boolean movingDown) {
            this.speed = speed;
            this.movingDown = movingDown;
        }
    } // Class to track target movement data

    @FXML
    void initialize() {
        // Initialize game elements
        score.setText("0");
        time.setText("0");

        // Set the pivot point for the cannon barrel to its left side (x = 0)
        cannonBarrel.setTranslateX(0);  // Ensure the left side is aligned with the base of the cannon
        cannonBarrel.setRotate(0);      // Reset any previous rotation

        // Add a mouse move listener to the shooting area to track mouse movements
        shootCannonArea.setOnMouseMoved(event -> {
            aimCannon(event);  // Call the aimCannon method whenever the mouse moves
        });

        
        startBlockerMovement();// Set up the blocker's movement using a Timeline     
        initializeTargetMovement();// Initialize target movement        
        startTargetMovement();// Set up the targets' random movement    
        
        
        
        remainingTime = startingTime;
        time.setText(String.valueOf(remainingTime));
        startTimer();
        
        //load SOund Effects
        cannonFireSound= new AudioClip(getClass().getResource("/cannon_fire.wav").toString());
        blockerHitSound= new AudioClip(getClass().getResource("/blocker_hit.wav").toString());
        targetHitSound= new AudioClip(getClass().getResource("/target_hit.wav").toString());
    }

    void startBlockerMovement() {
        blockerTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            moveBlocker();
        }));
        blockerTimeline.setCycleCount(Timeline.INDEFINITE); // Make the timeline run indefinitely
        blockerTimeline.play(); // Start the animation
    }

    void moveBlocker() {
        // Move the blocker up or down based on the direction
        if (movingDown) {
            blocker.setLayoutY(blocker.getLayoutY() + blockerSpeed); // Move down
            // Check if it hits the lower bound
            if (blocker.getBoundsInParent().intersects(LowerBound.getBoundsInParent())) {
                movingDown = false; // Change direction to upwards
            }
        } else {
            blocker.setLayoutY(blocker.getLayoutY() - blockerSpeed); // Move up
            // Check if it hits the upper bound
            if (blocker.getBoundsInParent().intersects(upperBound.getBoundsInParent())) {
                movingDown = true; // Change direction to downwards
            }
        }
    }

    void initializeTargetMovement() {
        // Initialize movement data (speed and direction) for each target
        targetMovementMap.put(blueTarget1, new TargetMovement(1 + random.nextDouble() * 2, random.nextBoolean()));
        targetMovementMap.put(blueTarget2, new TargetMovement(1 + random.nextDouble() * 2, random.nextBoolean()));
        targetMovementMap.put(blueTarget3, new TargetMovement(1 + random.nextDouble() * 2, random.nextBoolean()));
        targetMovementMap.put(blueTarget4, new TargetMovement(1 + random.nextDouble() * 2, random.nextBoolean()));

        targetMovementMap.put(yellowTarget1, new TargetMovement(1 + random.nextDouble() * 2, random.nextBoolean()));
        targetMovementMap.put(yellowTarget2, new TargetMovement(1 + random.nextDouble() * 2, random.nextBoolean()));
        targetMovementMap.put(yellowTarget3, new TargetMovement(1 + random.nextDouble() * 2, random.nextBoolean()));
        targetMovementMap.put(yellowTarget4, new TargetMovement(1 + random.nextDouble() * 2, random.nextBoolean()));
    }

    void startTargetMovement() {
        targetTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            moveTargets(blueTarget1);
            moveTargets(blueTarget2);
            moveTargets(blueTarget3);
            moveTargets(blueTarget4);
            moveTargets(yellowTarget1);
            moveTargets(yellowTarget2);
            moveTargets(yellowTarget3);
            moveTargets(yellowTarget4);
        }));
        targetTimeline.setCycleCount(Timeline.INDEFINITE); // Make the timeline run indefinitely
        targetTimeline.play(); // Start the animation
    }

    void moveTargets(Rectangle target) {
        TargetMovement movement = targetMovementMap.get(target);
        double targetSpeed = movement.speed;
        boolean movingDown = movement.movingDown;

     // Skip targets that are not visible
        if (!target.isVisible()) {
            return;
        }
        
        
        if (movingDown) {
            target.setLayoutY(target.getLayoutY() + targetSpeed);
            if (target.getBoundsInParent().intersects(LowerBound.getBoundsInParent())) {
                movement.movingDown = false;
            }
        } else {
            target.setLayoutY(target.getLayoutY() - targetSpeed);
            if (target.getBoundsInParent().intersects(upperBound.getBoundsInParent())) {
                movement.movingDown = true;
            }
        }
    }

    @FXML
    void aimCannon(MouseEvent event) {
        // Get the current mouse X and Y positions relative to the Pane
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Get the X and Y positions of the cannon's base (left side of the cannon barrel)
        double cannonBaseX = cannonBarrel.getLayoutX();
        double cannonBaseY = cannonBarrel.getLayoutY() + cannonBarrel.getHeight() / 2; // Middle of the barrel's left side

        // Calculate the differences in X and Y between the mouse and the cannon's base
        double deltaX = mouseX - cannonBaseX;
        double deltaY = mouseY - cannonBaseY;

        // Calculate the angle using atan2 (this will give us the angle relative to the cannon's base)
        double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));

        // Limit the angle to -30 degrees (downward) to 30 degrees (upward)
        double maxAngle = 30;
        double minAngle = -30;

        // Clamp the angle between the desired range
        if (angle > maxAngle) {
            angle = maxAngle;
        } else if (angle < minAngle) {
            angle = minAngle;
        }

        // Apply the rotation to the cannon barrel
        cannonBarrel.setRotate(angle);
    }

    @FXML
    void clickTarget(MouseEvent event) {

    }

    @FXML
    void fireCannonBall(MouseEvent event) {
        shotsFired++; // Increment shots fired
        cannonFireSound.play();

        if (cannonballTimeline != null) {
            cannonballTimeline.stop();
        }

        double angle = Math.toRadians(cannonBarrel.getRotate());
        double startX = cannonBarrel.getLayoutX() + cannonBarrel.getWidth();
        double startY = cannonBarrel.getLayoutY() + cannonBarrel.getHeight() / 2;

        cannonBall.setLayoutX(startX);
        cannonBall.setLayoutY(startY);
        cannonBall.setVisible(true);

        double speed = 5;
        double dx = speed * Math.cos(angle);
        double dy = speed * Math.sin(angle);

        cannonballTimeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            cannonBall.setLayoutX(cannonBall.getLayoutX() + dx);
            cannonBall.setLayoutY(cannonBall.getLayoutY() + dy);

            // Check for collision with the blocker
            if (cannonBall.getBoundsInParent().intersects(blocker.getBoundsInParent())) {
                handleBlockerCollision();
                cannonballTimeline.stop();
                cannonBall.setVisible(false);
                return;
            }

            // Check for collisions with targets
            Rectangle hitTarget = null;
            for (Rectangle target : targetMovementMap.keySet()) {
                if (target.isVisible() && cannonBall.getBoundsInParent().intersects(target.getBoundsInParent())) {
                    hitTarget = target;
                    break;
                }
            }

            if (hitTarget != null) {
                handleTargetHit(hitTarget);
                cannonballTimeline.stop();
                cannonBall.setVisible(false);
            }

            // Check for collisions with boundaries
            if (cannonBall.getLayoutX() > shootCannonArea.getWidth() ||
                cannonBall.getLayoutY() < 0 ||
                cannonBall.getLayoutY() > shootCannonArea.getHeight()) {
                cannonballTimeline.stop();
                cannonBall.setVisible(false);
            }
        }));

        cannonballTimeline.setCycleCount(Timeline.INDEFINITE);
        cannonballTimeline.play();
    }

    void handleTargetHit(Rectangle target) {
        if (remainingTime > 0) { // Prevent updates after game ends
            targetHitSound.play();
            target.setVisible(false);
            hits++;
            score.setText(String.valueOf(hits));

            remainingTime += 3; // Add 3 seconds bonus
            time.setText(String.valueOf(remainingTime));
            
         // Check if all targets have been hit
            if (allTargetsHit()) {
                timer.stop();
                showEndDialog("You Win!", "Shots Fired: " + shotsFired + "\nElapsed Time: " + (startingTime - remainingTime));
            }
        }
    }

    
    void handleBlockerCollision() {
        if (remainingTime > 0) { // Prevent updates after game ends
            blockerHitSound.play();
            remainingTime = Math.max(0, remainingTime - 3); // Apply penalty
            time.setText(String.valueOf(remainingTime));
        }
    }

    
    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (remainingTime > 0) { // Decrement time only if it's above 0
                remainingTime--;
                time.setText(String.valueOf(remainingTime));
            }

            // Check win condition
            if (hits == 9 && remainingTime > 0) {
                timer.stop();
                showEndDialog("You Win!", "Shots Fired: " + shotsFired + "\nElapsed Time: " + (startingTime - remainingTime));
            }

            // Check lose condition
            if (remainingTime == 0) {
                timer.stop();
                showEndDialog("You Lose!", "Shots Fired: " + shotsFired + "\nElapsed Time: " + startingTime);
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }
    
    
    private boolean allTargetsHit() {
        for (Rectangle target : targetMovementMap.keySet()) {
            if (target.isVisible()) {
                return false; // At least one target is still visible
            }
        }
        return true; // All targets have been hit
    }





    private void showEndDialog(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();

            // Reset the game after dialog closes
            resetGame();
        });
    }



    private void resetGame() {
        // Reset score
        hits = 0;
        score.setText("0");

        // Reset timer
        remainingTime = startingTime;
        time.setText(String.valueOf(remainingTime));

        // Reset shots fired
        shotsFired = 0;

        // Reset cannonball visibility
        cannonBall.setVisible(false);

        // Reset targets
        for (Rectangle target : targetMovementMap.keySet()) {
            target.setVisible(true);
        }

        // Reset blocker position
        blocker.setLayoutY(200);

        // Restart target and blocker movements
        if (targetTimeline != null) {
            targetTimeline.stop();
            startTargetMovement();
        }
        if (blockerTimeline != null) {
            blockerTimeline.stop();
            startBlockerMovement();
        }

        // Start timer again
        startTimer();
    }

}