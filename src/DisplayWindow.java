import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;
import processing.video.Movie;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

/**
 * Display class for working with image filters
 * by David Dobervich
 */
public class DisplayWindow extends PApplet {
    private static final int WEBCAM_WIDTH = 640;
    private static final int WEBCAM_HEIGHT = 480;
    private static final int WEBCAM = 1;
    private static final int IMAGE = 2;
    private static final int VIDEO = 3;

    private Capture webcam;
    private Movie movie;
    private DImage inputImage;

    private boolean currentlyViewingFilteredImage = false;
    private int source;
    private DImage frame, filteredFrame, oldFilteredFrame, currentDisplayFrame;
    private boolean loading = false;

    private int centerX, centerY;

    private PixelFilter filter;
    private int count = 0;
    private String colorString = "";
    private boolean paused = false;

    public void settings() {
        initializeImageSource(args);

        size(800, 650);
        centerX = width/2;
        centerY = height/2;
    }

    private void initializeImageSource(String[] args) {
        if (args == null || args.length == 0) {         // if no input provided, do it interactively with user
            displayVideoSourceChoiceDialog();
            return;
        }

        String sourcePath = args[0];
        this.inputImage = tryToLoadStillImage(sourcePath);
        source = IMAGE;

        if (inputImage == null) {
            this.movie = new Movie(this, sourcePath);
            this.source = VIDEO;
        }
    }

    private void displayVideoSourceChoiceDialog() {
        Object[] options = {"Load mp4 or image from disk",
                "Use a webcam"};
        this.source = JOptionPane.showOptionDialog(null,
                "What video source would you like to use?",
                "Video source",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);

        if (source == WEBCAM) {
            displayHeight = WEBCAM_HEIGHT;
            displayWidth = WEBCAM_WIDTH;
        } else {
            String sourcePath = fileChooser();
            this.inputImage = tryToLoadStillImage(sourcePath);
            source = IMAGE;

            if (inputImage == null) {
                this.movie = new Movie(this, sourcePath);
                this.source = VIDEO;
            }
        }
    }

    private String fileChooser() {
        String userDirLocation = System.getProperty("user.dir");
        File userDir = new File(userDirLocation);
        JFileChooser fc = new JFileChooser(userDir);
        int returnVal = fc.showOpenDialog(null);
        File file = fc.getSelectedFile();
        return file.getAbsolutePath();
    }

    private DImage tryToLoadStillImage(String moviePath) {
        try {
            PImage input = this.loadImage(moviePath);
            return new DImage(input);
        } catch (Exception e) {
            return null;
        }
    }

    public void setup() {
        if (source == VIDEO && movie == null) {
            System.err.println("No mp4 file loaded, switching to webcam as video source");
            source = WEBCAM;
        } else if (source != WEBCAM && movie != null) {
            movie.play();
        }

        if (source == IMAGE && inputImage == null) {
            System.err.println("No mp4 file loaded, switching to webcam as video source");
            source = WEBCAM;
        }

        if (source == WEBCAM && webcam == null) {
            webcam = new Capture(this, WEBCAM_WIDTH, WEBCAM_HEIGHT);
            webcam.start();
        }
    }

    public void draw() {
        background(200);
        if (source == IMAGE) {
            applyFilterToImage(inputImage.getPImage());
        }
        if (frame == null) {
            return;
        }
        if (oldFilteredFrame == null) oldFilteredFrame = frame;

        DImage currentFiltered = (!loading && filteredFrame != null) ? filteredFrame : oldFilteredFrame;
        currentDisplayFrame = (!currentlyViewingFilteredImage) ? frame : filteredFrame;

        if (!currentlyViewingFilteredImage) {
            drawFrame(frame, frame, currentFiltered, centerX - frame.getWidth()/2, centerY - frame.getHeight()/2);
        } else {        // viewing filtered
            drawFrame(currentFiltered, frame, currentFiltered, centerX - currentFiltered.getWidth()/2, centerY - currentFiltered.getHeight()/2);
        }

        fill(200);
        rect(0, height-20*2, width, 20*2);
        fill(0);

        count++;
        if (count == 11) {
            colorString = colorStringAt(mouseX, mouseY);
            count = 0;
        }
        fill(0);
        textSize(16);
        textAlign(LEFT, CENTER);
        text(mousePositionString(currentDisplayFrame) + " " + colorString, 10, height - 22);

        if (filter == null) {
            text("Press 'f' to load a filter", 350, height - 22);
        } else if (!currentlyViewingFilteredImage) {
            text("Press 's' to show filtered image", 350, height - 22);
        }

        if (paused) {
            text("Press 'p' to unpause", 620, height-22);
        }

        stroke(200);
        strokeWeight(1);
        line(0, mouseY, width, mouseY);
        line(mouseX, 0, mouseX, height);
    }

    private String colorStringAt(int mouseX, int mouseY) {
        loadPixels();
        int c = pixels[mouseY*width + mouseX];
        float red = red(c);
        float green = green(c);
        float blue = blue(c);
        return "r: " + red +  " g: " + green + " b: " + blue;
    }

    private int getImageMouseX(DImage displayImage) {
        return mouseX - centerX + displayImage.getWidth()/2;
    }

    private int getImageMouseY(DImage displayImage) {
        return mouseY - centerY + displayImage.getHeight()/2;
    }

    private String mousePositionString(DImage displayImage) {
        return "(" + getImageMouseX(displayImage) + ", " + getImageMouseY(displayImage) + ")";
    }

    public void drawFrame(DImage toDisplay, DImage original, DImage filtered, int x, int y) {
        image(toDisplay.getPImage(), x, y);

        if (filter != null) {
            pushMatrix();
            translate(x, y);

            filter.drawOverlay(this, original, filtered);

            popMatrix();
        }
    }

    public void captureEvent(Capture c) {
        c.read();
        applyFilterToImage(c.get());
    }

    public void applyFilterToImage(PImage img) {
        if (paused) return;

        oldFilteredFrame = filteredFrame;
        loading = true;

        frame = new DImage(img);
        filteredFrame = new DImage(frame);
        filteredFrame = runFilters(filteredFrame);

        loading = false;
    }

    public void movieEvent(Movie m) {
        if (paused) return;

        oldFilteredFrame = filteredFrame;
        loading = true;
        m.read();
        frame = new DImage(m.get());
        filteredFrame = new DImage(frame);

        filteredFrame = runFilters(filteredFrame);

        loading = false;
    }

    private DImage runFilters(DImage frameToFilter) {
        if (filter != null) return filter.processImage(frameToFilter);
        return frameToFilter;
    }

    public void keyReleased() {
        if (key == 'f' || key == 'F') {
            this.filter = loadNewFilter();
        }

        if (key == 's' || key == 'S') {
            currentlyViewingFilteredImage = !currentlyViewingFilteredImage;
        }

        if (key == 'p' || key == 'P') {
            paused = !paused;

            if (source != WEBCAM && movie != null) {
                if (paused) {
                    movie.pause();
                } else {
                    movie.play();
                }
            }
        }

        if (frame != null && (filter instanceof Clickable)) {
            ((Clickable)filter).keyPressed(key);
        }
    }

    public void mouseReleased() {
        if (this.filter != null && this.filter instanceof Clickable) {
            ((Clickable)filter).mouseClicked(getImageMouseX(currentDisplayFrame), getImageMouseY(currentDisplayFrame), currentDisplayFrame);
        }
    }

    private PixelFilter loadNewFilter() {
        String name = JOptionPane.showInputDialog("Type the name of your processImage class");
        PixelFilter f = null;
        try {
            Class c = Class.forName(name);
            f = (PixelFilter) c.newInstance();
        } catch (Exception e) {
            System.err.println("Something went wrong when instantiating your class!  (running its constructor). " +
                    "The error is: " + e.getMessage());
            System.err.println(e.getMessage());
        }

        return f;
    }

    public static void showFor(String filePath) {
        PApplet.main("DisplayWindow", new String[]{filePath});
    }

    public static void getInputInteractively() {
        PApplet.main("DisplayWindow", new String[]{});
    }
}

