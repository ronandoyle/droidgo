package com.droidgo;

/**
 * The interface which listens for the joystick knob to be moved.
 * @author Ronan
 *
 */

public interface HandleMovedListener {
    public void OnMoved(int userX, int userY);
    public void OnReleased();
    public void OnCentered();
}
