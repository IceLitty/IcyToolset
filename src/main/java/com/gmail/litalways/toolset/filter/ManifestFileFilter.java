package com.gmail.litalways.toolset.filter;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author IceRain
 * @since 2024/07/22
 */
public class ManifestFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f != null && f.isFile() && "MANIFEST.MF".equals(f.getName());
    }

    @Override
    public String getDescription() {
        return "MANIFEST.MF";
    }

}
