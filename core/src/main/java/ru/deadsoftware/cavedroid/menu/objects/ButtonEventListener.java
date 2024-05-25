package ru.deadsoftware.cavedroid.menu.objects;

/**
 * A {@link Button} event listener. Should be sent as lambda to Button's constructor.
 */
public interface ButtonEventListener {

    /**
     * Will be called by {@link Button} when clicked
     */
    void buttonClicked();

}
