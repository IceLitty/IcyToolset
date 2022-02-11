package com.gmail.litalways.toolset.listener;

import javax.swing.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * @author IceRain
 * @since 2022/02/09
 */
public class ScrollbarSyncListener implements AdjustmentListener {

    private final JScrollBar v1, h1, v2, h2;

    public ScrollbarSyncListener(JScrollPane sp1, JScrollPane sp2) {
        v1 = sp1.getVerticalScrollBar();
        h1 = sp1.getHorizontalScrollBar();
        v2 = sp2.getVerticalScrollBar();
        h2 = sp2.getHorizontalScrollBar();
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        JScrollBar scrollBar = (JScrollBar) e.getSource();
        int value = scrollBar.getValue();
        JScrollBar target = null;
        if (scrollBar == v1)
            target = v2;
        if (scrollBar == h1)
            target = h2;
        if (scrollBar == v2)
            target = v1;
        if (scrollBar == h2)
            target = h1;
        if (target != null)
            target.setValue(value);
    }

}
