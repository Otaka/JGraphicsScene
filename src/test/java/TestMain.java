import com.jgraphicsscene.DragMode;
import com.jgraphicsscene.JGraphicsScene;
import com.jgraphicsscene.JGraphicsView;
import com.jgraphicsscene.events.ItemChangedType;
import com.jgraphicsscene.node.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;

public class TestMain extends JFrame {

    JPanel viewsPanel;
    JGraphicsView view1;
    JGraphicsView view2;
    JGraphicsScene scene;
    private int scaleTimeline = 0;

    public static void main(String[] args) {
        System.out.println("Start JGraphicsScene test");
        SwingUtilities.invokeLater(() -> {
            try {
                new TestMain().main();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void main() throws IOException {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Test JGraphicsScene");
        setLayout(new BorderLayout());
        viewsPanel = new JPanel(new GridLayout(2, 1));
        viewsPanel.setPreferredSize(new Dimension(800, 800));
        add(viewsPanel, BorderLayout.CENTER);
        setupScene();

        JPanel manipPanel = new JPanel();
        manipPanel.setPreferredSize(new Dimension(300, 300));
        add(manipPanel, BorderLayout.EAST);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupScene() throws IOException {
        scene = new JGraphicsScene();
        JGraphicsItem item1 = scene.addItem(new JGraphicsRectItem(20, 20, -15, -15, 30, 30));
        item1.setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable);
        item1.dirtyTransform();
        item1.setPosition(200, 200, true);
        item1.dirtyTransform();
        JGraphicsRectItem item2 = new JGraphicsRectItem(100, 100, -15, -15, 30, 30).setFillColor(Color.YELLOW);
        item2.setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemSendsMoved);
        item2.setRotation((float) Math.toRadians(10));
        item2.addItem(new JGraphicsRectItem(-30, -30, -5, -5, 10, 10).setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable));
        item2.addItem(new JGraphicsEllipseItem(15, -15, -5, -5, 10, 10).setFlag(JGraphicsItem.ItemIsSelectable));
        item2.addEvent((type, i) -> {
            if (type == ItemChangedType.Position) {
                System.out.println("Event moved " + i.getGlobalPosition());
            }
            return true;
        });
        scene.addItem(item2);
        scene.addItem(new JGraphicsImageItem(200, 200, ImageIO.read(TestMain.class.getResourceAsStream("/icons/boy.png")))
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable));
        scene.addItem(new JGraphicsImageItem(300, 40, ImageIO.read(TestMain.class.getResourceAsStream("/icons/girl.png")))
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable));
        scene.addItem(new JGraphicsTextItem(450, 40, -0, 0,150,100,"Hello world. My name is Dima. Every day I go to rabota. There are a lot of trees and flowers around my rabota."))
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable);

        view1 = new JGraphicsView().setWheelZoomEnabled(true).setMiddleMousePanEnabled(true).setWheelZoomToPointer(true).setDragMode(DragMode.RubberBandDrag);
        view1.setPreferredSize(new Dimension(800, 500));
        view1.setScene(scene);
        view1.setAntialias(true);
        view1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        viewsPanel.add(view1);

        view2 = new JGraphicsView().setWheelZoomEnabled(true).setMiddleMousePanEnabled(true).setWheelZoomToPointer(true).setDragMode(DragMode.RubberBandDrag);
        view2.setPreferredSize(new Dimension(800, 500));
        view2.setScene(scene);
        view2.setAntialias(false);
        view2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        viewsPanel.add(view2);

        Timer timer = new Timer(20, (e) -> {
            item2.setRotation((float) (item2.getRotation() + Math.toRadians(0.3)));
            item2.dirtyTransform();
            scaleTimeline += 5;
            item1.setScale((float) Math.sin(Math.toRadians(scaleTimeline)));
            item1.dirtyTransform();
            view1.repaint();
            view2.repaint();
        });

        timer.setRepeats(true);
        timer.start();
    }

    /**
     * TODO:
     * * Focus
     * * Keyboard
     * * methods for translation coordinates
     * * cursor for item
     */
}