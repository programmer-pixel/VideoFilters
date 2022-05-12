import processing.core.PApplet;

import javax.swing.*;

public class LightenBlackAndWhiteFilter implements PixelFilter, Clickable{
    private int percent;

    public LightenBlackAndWhiteFilter() {
        percent = Integer.parseInt(JOptionPane.showInputDialog("Please enter a percentage from 0 to 100 that you want to lighten: "));
    }

    @Override
    public DImage processImage(DImage img) {
        short[][] grid = img.getBWPixelGrid();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                grid[i][j] = (short) (grid[i][j] + (255 - grid[i][j])*(percent/100.0));
            }
        }

        img.setPixels(grid);
        return img;
    }

    @Override
    public void drawOverlay(PApplet window, DImage original, DImage filtered) {

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, DImage img) {

    }

    @Override
    public void keyPressed(char key) {
        if(key == '+' && percent < 100){
            percent++;
        }
        else if(key == '-' && percent > 0){
            percent--;
        }
    }
}
