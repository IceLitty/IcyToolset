package com.gmail.litalways.toolset.gui;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class MainFormAboutFunction {

    private final MainForm mainForm;

    public MainFormAboutFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.buttonAboutEncoding.addActionListener(e -> this.mainForm.buttonAboutEncoding.setText(System.getProperty("file.encoding")));
    }

}
