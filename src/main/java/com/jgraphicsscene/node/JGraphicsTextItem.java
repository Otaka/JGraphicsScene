package com.jgraphicsscene.node;

import com.jgraphicsscene.PaintContext;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class JGraphicsTextItem extends JGraphicsAbstractRectItem {
    private static final Font DEFAULT_FONT = new JLabel().getFont();
    private Color textColor = Color.BLACK;
    private String text;
    private Font font;
    private List<String> cachedSplittedStrings;
    private List<Integer> cachedSplittedStringsWidth;
    private int totalHeight;
    private int fontAscent;
    private int cachedStringsDistance;
    private HAlign hAlign = HAlign.LEFT;
    private VAlign vAlign = VAlign.TOP;

    public JGraphicsTextItem(float x, float y, float width, float height, String text) {
        setPosition(x, y, false);
        setWidth(width);
        setHeight(height);
        this.text = text;
    }

    public static Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    public Font getFont() {
        if (font == null) {
            font = DEFAULT_FONT;
        }
        return font;
    }

    public JGraphicsTextItem setFont(Font font) {
        this.font = font;
        cachedSplittedStrings = null;
        return this;
    }

    public String getText() {
        return text;
    }

    public JGraphicsTextItem setText(String text) {
        this.text = text;
        cachedSplittedStrings = null;
        return this;
    }

    public HAlign getHAlign() {
        return hAlign;
    }

    public JGraphicsTextItem setHAlign(HAlign hAlign) {
        this.hAlign = hAlign;
        return this;
    }

    public VAlign getVAlign() {
        return vAlign;
    }

    public JGraphicsTextItem setVAlign(VAlign vAlign) {
        this.vAlign = vAlign;
        return this;
    }

    @Override
    public JGraphicsAbstractRectItem setWidth(float width) {
        cachedSplittedStrings = null;
        return super.setWidth(width);
    }

    public Color getTextColor() {
        return textColor;
    }

    public JGraphicsTextItem setTextColor(Color color) {
        this.textColor = color;
        return this;
    }

    @Override
    protected void paintItem(PaintContext p) {
        Graphics2D g = p.getGraphics();
        g.setFont(getFont());
        g.setColor(getTextColor());
        if (cachedSplittedStrings == null) {
            preprocessText(g);
        }
        int yOffset = 0;
        if (vAlign == VAlign.CENTER) {
            yOffset = (int) (getHeight() - totalHeight) / 2;
            yOffset -= fontAscent;
            if (yOffset < 0) yOffset = 0;
        } else if (vAlign == VAlign.BOTTOM) {
            yOffset = (int) (getHeight() - totalHeight);
        }
        int lineY = cachedStringsDistance + yOffset;
        for (int i = 0; i < cachedSplittedStrings.size(); i++) {
            String str = cachedSplittedStrings.get(i);
            int lineWidth = cachedSplittedStringsWidth.get(i);
            int x = 0;
            if (hAlign == HAlign.CENTER) {
                x = (int) (getWidth() - lineWidth) / 2;
            } else if (hAlign == HAlign.RIGHT) {
                x = (int) (getWidth() - lineWidth);
            }
            g.drawString(str, x, lineY);
            lineY += cachedStringsDistance;
        }
    }

    private void preprocessText(Graphics2D g) {
        cachedSplittedStringsWidth = new ArrayList<>();
        cachedSplittedStrings = new ArrayList<>();
        FontMetrics fontMetrics = g.getFontMetrics();
        cachedStringsDistance = fontMetrics.getHeight();
        String[] lines = text.split("\\n");
        for (String line : lines) {
            preprocessLine(line, fontMetrics, cachedSplittedStrings, getWidth());
        }
        fontAscent = fontMetrics.getHeight() - fontMetrics.getAscent();
        //calculate width of each line and total height of the text block
        int totalHeight = 0;
        for (String line : cachedSplittedStrings) {
            int width = fontMetrics.stringWidth(line);
            cachedSplittedStringsWidth.add(width);
            totalHeight += cachedStringsDistance;
        }
        this.totalHeight = totalHeight;
    }

    private static void preprocessLine(String lineToPreprocess, FontMetrics fontMetrics, List<String> outList, float requiredWidth) {
        String[] arr = lineToPreprocess.split(" ");
        int nIndex = 0;
        while (nIndex < arr.length) {
            StringBuilder line = new StringBuilder(arr[nIndex++]);
            while ((nIndex < arr.length) && (fontMetrics.stringWidth(line + " " + arr[nIndex]) < requiredWidth)) {
                line.append(" ").append(arr[nIndex]);
                nIndex++;
            }
            outList.add(line.toString());
        }
    }

    public enum HAlign {
        LEFT, CENTER, RIGHT
    }

    public enum VAlign {
        TOP, CENTER, BOTTOM
    }
}