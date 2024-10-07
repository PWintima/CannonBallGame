import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CannonBallGameController {

    int hits = 0;
    double blockerSpeed = 3; // Speed of the blocker (can be adjusted)
    boolean movingDown = true; // Direction flag for the blocker

    @FXML
    private Rectangle LowerBound;

    @FXML
    private Rectangle blocker;

    @FXML
    private Rectangle blueTarget1;

    @FXML
    private Rectangle blueTarget2;

    @FXML
    private Rectangle blueTarget3;

    @FXML
    private Rectangle blueTarget4;

    @FXML
    private Circle cannonBall;

    @FXML
    private Rectangle cannonBarrel;

    @FXML
    private Label scoreLabel;

    @FXML
    private Pane shootCannonArea;

    @FXML
    private Label timeLabel;

    @FXML
    private Rectangle upperBound;

    @FXML
    private Rectangle yellowTarget1;

    @FXML
    private Rectangle yellowTarget2;

    @FXML
    private Rectangle yellowTarget3;

    @FXML
    private Rectangle yellowTarget4;

    private Timeline blockerTimeline;
    private Timeline targetTimeline;

    private Random random = new Random();

    // Store movement data (speed and direction) for each target
    private Map<Rectangle, TargetMovement> targetMovementMap = new HashMap<>();

    // Inner class to store speed and direction for each target
    private static class TargetMovement {
        double speed;
        boolean movingDown;

        TargetMovement(double speed, boolean movingDown) {
            this.speed = speed;
            this.movingDown = movingDown;
        }
    }

    @FXML
    void initialize() {
        // Initialize game elements
        scoreLabel.setText("0");
        timeLabel.setText("0");

        // Set the pivot point for the cannon barrel to its left side (x = 0)
        cannonBarrel.setTranslateX(0);  // Ensure the left side is aligned with the base of the cannon
        cannonBarrel.setRotate(0);      // Reset any previous rotation

        // Add a mouse move listener to the shooting area to track mouse movements
        shootCannonArea.setOnMouseMoved(event -> {
            aimCannon(event);  // Call the aimCannon method whenever the mouse moves
        });

        // Set up the blocker's movement using a Timeline
        startBlockerMovement();

        // Initialize target movement
        initializeTargetMovement();

        // Set up the targets' random movement
        startTargetMovement();
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

        if (movingDown) {
            target.setLayoutY(target.getLayoutY() + targetSpeed); // Move down
            // Check if it hits the lower bound
            if (target.getBoundsInParent().intersects(LowerBound.getBoundsInParent())) {
                movement.movingDown = false; // Change direction to upwards
            }
        } else {
            target.setLayoutY(target.getLayoutY() - targetSpeed); // Move up
            // Check if it hits the upper bound
            if (target.getBoundsInParent().intersects(upperBound.getBoundsInParent())) {
                movement.movingDown = true; // Change direction to downwards
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

    }
}
