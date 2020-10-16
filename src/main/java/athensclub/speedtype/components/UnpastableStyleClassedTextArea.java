package athensclub.speedtype.components;

import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * A {@link StyleClassedTextArea} class with {@link #paste()} as no-op.
 */
public class UnpastableStyleClassedTextArea extends StyleClassedTextArea {

    @Override
    public void paste() {
    }
}
