public interface Clickable {
    /***
     * Action you wish to perform when the mouse is clicked.  For example, store the color of the pixel where
     * the mouse was clicked to set a threshold or a target color.
     * @param mouseX the x coordiante where the mouse was clicked ( with (0, 0) the upper left corner of your image )
     * @param mouseY
     * @param img
     */
    public void mouseClicked(int mouseX, int mouseY, DImage img);
    public void keyPressed(char key);
}
