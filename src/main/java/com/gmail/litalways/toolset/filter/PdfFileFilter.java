package com.gmail.litalways.toolset.filter;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author IceRain
 * @since 2024/08/25
 */
public class PdfFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f != null && f.isFile() && f.getName().toLowerCase().endsWith(".pdf");
    }

    @Override
    public String getDescription() {
        return "*.pdf";
    }

}
