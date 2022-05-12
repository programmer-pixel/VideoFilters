import processing.core.PApplet;

import javax.swing.*;

public class DarkenBlackAndWhiteFilter implements PixelFilter, Clickable{
    private int splitPoint;

    public DarkenBlackAndWhiteFilter() {
        splitPoint = Integer.parseInt(JOptionPane.showInputDialog("Please enter a percentage from 0 to 100 that you want to lighten: "));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, DImage img) {

    }

    @Override
    public void keyPressed(char key) {
        if(key == '+' && splitPoint < 100){
            splitPoint++;
        }
        else if(key == '-' && splitPoint > 0){
            splitPoint--;
        }
    }

    @Override
    public DImage processImage(DImage img) {
        short[][] grid = img.getBWPixelGrid();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if(grid[i][j] > splitPoint){
                    grid[i][j] = 255;
                }
                else if(grid[i][j] < splitPoint){
                    grid[i][j] = 0;
                }
                else{
                    grid[i][j] = 255;
                }
            }
        }

        img.setPixels(grid);
        return img;
    }

    @Override
    public void drawOverlay(PApplet window, DImage original, DImage filtered) {

    }
}
