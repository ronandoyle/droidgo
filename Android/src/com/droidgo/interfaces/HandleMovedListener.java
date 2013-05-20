package com.droidgo.interfaces;

/**
 * The interface which listens for the joystick handle to be moved.
 * @author Ronan Doyle
 *
 */
public interface HandleMovedListener {
    public void OnMoved(int userX, int userY);
    public void OnReleased();
    public void OnCentered();
}
