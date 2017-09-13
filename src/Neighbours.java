import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.System.*;
import static oracle.jrockit.jfr.events.Bits.intValue;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then method start() far below.
 * - To test uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    // Enumeration type for the Actors
    enum Actor {
        BLUE, NONE, RED   // Type only has 3 values, NONEs are white
    }

    // Enumeration type for the state of an Actor
    enum State {
        UNSATISFIED,
        NA,        // Not applicable (NA), used for NONEs
        SATISFIED
    }

    final Random rand = new Random();

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable is accessible from any method
    Actor[][] world;              // The world is a square matrix of Actors
    int counter = 0;

    // This is the method called by the timer to update the world approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        final double threshold = 0.7;
        teleportAll(actorSatisfaction(threshold));
        // TODO add methods
    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime
    // That's why we must have "@Override" and "public" (just accept for now)
    @Override
    public void init(){
        //test();    // <---------------- Uncomment to TEST!

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.25, 0.25, 0.50};
        // Number of locations (places) in world (square)
        int nLocations = 900000;

        // TODO find methods that does the job
        Actor[] actors = generateDistribution(nLocations, dist[0], dist[1]);
        actors = shuffle(actors);
        world = toMatrix(actors);
        changeBlobSize();
        // ...
        // world =           // Finally set world variable
    }

    // Check if inside world
//    boolean isValidLocation(int size, int row, int col) {
//        // TODO
//        return false;
//    }

    // ----------- Utility methods -----------------

    // TODO need any utilities add here (= methods possible reusable for other programs)


    private void changeBlobSize() {
        while (blobSize*world.length+100 > 1000 && blobSize > 1) {
            blobSize--;
        }
    }

    private Actor[] generateDistribution(int amount, double probRed, double probBlue) {
        amount = (int)(sqrt(amount))*(int)(sqrt(amount));
        Actor[] distActors = new Actor[amount];
        double amountRed = StrictMath.round(amount * probRed);
        double amountBlue = StrictMath.round(amount * probBlue);
        for (int i = 0; i < amount; i++) {
            if (i < amountRed) {
                distActors[i] = Actor.RED;
            } else if ( i < amountRed + amountBlue){
                distActors[i] = Actor.BLUE;
            } else {
                distActors[i] = Actor.NONE;
            }
        }
        return distActors;
    }

    private Actor[] shuffle(Actor[] arr) {
        int index;
        Actor a;
        for (int i = 0; i < arr.length;i++) {
            index = rand.nextInt(arr.length);
            a = arr[i];
            arr[i] = arr[index];
            arr[index] = a;
        }
        return arr;
    }

    private Actor[][] toMatrix(Actor[] arr) {
        int length = intValue(Math.sqrt(arr.length));
        Actor[][] matrix = new Actor[arr.length/length][length];
        int count = 0;
        for (int a = 0; a < arr.length/length; a++) {
            for (int b = 0; b < length; b++){
                matrix[a][b] = arr[count];
                count++;
            }
        }
        return matrix;
    }

    private Actor neighbourColour(int offsetY, int offsetX, int row, int col){
        if (offsetX == 0 && offsetY == 0) {
            return Actor.NONE;
        }
        if (row + offsetY >= 0 && row + offsetY < world.length){
            if (col + offsetX >= 0 && col + offsetX < world[0].length){
                return (world[row+offsetY][col+offsetX]);
            }
        }
        return Actor.NONE;
    }

    private double percentageSameNeighbours(int row, int col, Actor colour){
        // Returns a value out of regular possible values
        if (colour == Actor.NONE) {
            return 2.0;
        }
        int amount = 0;
        int total = 0;
        for(int offsetY = -1; offsetY < 2;offsetY++){
            for(int offsetX = -1; offsetX < 2;offsetX++){
                if (neighbourColour(offsetY, offsetX, row, col) == colour) {
                    amount += 1;
                    total += 1;
                } else if (neighbourColour(offsetY, offsetX, row, col) != Actor.NONE) {
                    total += 1;
                }
            }
        }
        if (total == 0) {
            return 1.0;
        }
        return (double)amount/total;
    }

    private State isActorSatisfied(int row, int col, Actor colour, double threshold) {
        if (percentageSameNeighbours(row,col,colour) > 1.0) {
            return State.NA;
        } else if (percentageSameNeighbours(row,col,colour) >= threshold) {
            return State.SATISFIED;
        } else {
            return State.UNSATISFIED;
        }
    }

    private State[][] actorSatisfaction(double threshold) {
        State[][] worldSatisfaction = new State[world.length][world[0].length];
        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world[0].length; col++) {
                worldSatisfaction[row][col] = isActorSatisfied(row, col, world[row][col], threshold);
            }
        }
        return worldSatisfaction;
    }

    private void switcheroo(State[][] worldSatisfaction, int row, int col) {
        int x;
        int y;
        Actor temp = world[row][col];
        if (worldSatisfaction[row][col] == State.UNSATISFIED) {
            do {
                x = rand.nextInt(world[0].length);
                y = rand.nextInt(world.length);
            } while (worldSatisfaction[y][x] != State.NA || (row == y && col == x));
            world[row][col] = world[y][x];
            world[y][x] = temp;

        }
    }

    private void teleportAll(State[][] worldSatisfaction) {
        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world[0].length; col++) {
                switcheroo(worldSatisfaction, row, col);
            }
        }
    }



    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
//    void test(){
//        // A small hard coded world for testing
//        world = new Actor[][]{
//                {Actor.RED, Actor.RED, Actor.NONE},
//                {Actor.NONE, Actor.BLUE, Actor.NONE},
//                {Actor.RED, Actor.NONE, Actor.BLUE}
//        };
//        double th = 0.5;   // Simple threshold used for testing
//
//        // A first test!
//        int s = world.length;
//        out.println(isValidLocation(s, 0, 0));
//
//
//        /* Move of unsatisfied hard to test because of random */
//
//        exit(0);
//    }

    // ---- NOTHING to do below this row, it's JavaFX stuff  ----
    int blobSize = 10;
    long previousTime = nanoTime();
    final long INTERVAL = 45000000;

    @Override

    public void start(Stage primaryStage) throws Exception {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(world.length*blobSize+100, world.length*blobSize+100);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long currentNanoTime) {
                long elapsedNanos = currentNanoTime - previousTime;
                if (elapsedNanos > INTERVAL) {
                    updateWorld();
                    renderWorld(gc);
                    previousTime = currentNanoTime;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g) {
        g.clearRect(0,0, world.length*blobSize+100, world.length*blobSize+100);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int x = blobSize * col + 50;
                int y = blobSize * row + 50;

                if (world[row][col] == Actor.RED) {
                    g.setFill(Color.RED);
                } else if (world[row][col] == Actor.BLUE) {
                    g.setFill(Color.BLUE);
                } else {
                    g.setFill(Color.WHITE);
                }
                g.fillOval(x, y, blobSize, blobSize);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
