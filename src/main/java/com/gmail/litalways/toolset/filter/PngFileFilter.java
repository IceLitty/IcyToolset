package com.gmail.litalways.toolset.filter;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class PngFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f != null && !f.isDirectory() && f.getName().toLowerCase().endsWith(".png");
    }

    @Override
    public String getDescription() {
        return "*.png";
    }

}
