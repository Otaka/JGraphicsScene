import com.jgraphicsscene.DragMode;
import com.jgraphicsscene.JGraphicsScene;
import com.jgraphicsscene.JGraphicsView;
import com.jgraphicsscene.effects.JGraphicsBlinkEffect;
import com.jgraphicsscene.effects.JGraphicsXorColorizeEffect;
import com.jgraphicsscene.events.ItemChangedType;
import com.jgraphicsscene.node.JGraphicsEllipseItem;
import com.jgraphicsscene.node.JGraphicsImageItem;
import com.jgraphicsscene.node.JGraphicsItem;
import com.jgraphicsscene.node.JGraphicsLineItem;
import com.jgraphicsscene.node.JGraphicsRectItem;
import com.jgraphicsscene.node.JGraphicsSplineItem;
import com.jgraphicsscene.node.JGraphicsTextItem;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.IOException;

public class TestMain extends JFrame {

    JPanel viewsPanel;
    JGraphicsView view1;
    JGraphicsScene scene;
    private int scaleTimeline = 0;
    private JGraphicsItem imageItem;

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
        viewsPanel = new JPanel(new BorderLayout());
        viewsPanel.setPreferredSize(new Dimension(800, 800));
        add(viewsPanel, BorderLayout.CENTER);

        JPanel manipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSlider moveSlider = new JSlider(0, 300, 0);
        moveSlider.addChangeListener(e -> {
            imageItem.setPosition(moveSlider.getValue() + 200, 200, true);
            imageItem.dirtyTransform();
        });
        manipPanel.add(moveSlider);
        add(manipPanel, BorderLayout.SOUTH);
        setupScene();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupScene() throws IOException {
        scene = new JGraphicsScene();
        JGraphicsItem item1 = scene.addItem(new JGraphicsRectItem(20, 20, 30, 30))
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable)
                .setPosition(200, 200, true)
                .dirtyTransform();

        JGraphicsItem item2 = new JGraphicsRectItem(100, 100, 30, 30)
                .setFillColor(Color.YELLOW)
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable)
                .addEffect(new JGraphicsXorColorizeEffect(Color.RED))
                .addItem(new JGraphicsRectItem(-30, -30, 10, 10)
                        .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemIgnoresParentScale
                        )
                );
        item2.addItem(new JGraphicsEllipseItem(15, -15, 10, 10)
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemIgnoresParentScale));
        item2.addEvent((type, i, arg) -> {
            if (type == ItemChangedType.Position) {
                System.out.println("Event moved global:" + i.getGlobalPosition() + " view:" + i.getPositionInView(view1));
            }
            return true;
        });
        scene.addItem(item2);

        JGraphicsItem item3 = new JGraphicsRectItem(200, 100, 30, 30)
                .setFillColor(Color.CYAN)
                .setFlag(JGraphicsItem.ItemIsMovable);
        item3.addItem(new JGraphicsTextItem(-20, -40, 100, 100, "Only move"));
        scene.addItem(item3);

        JGraphicsItem item4 = new JGraphicsRectItem(300, 100, 30, 30)
                .setFillColor(Color.MAGENTA)
                .setFlag(JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemShowManipulationHandlers);
        scene.addItem(item4);

        JGraphicsItem item5 = new JGraphicsEllipseItem(350, 100, 30, 30)
                .setFillColor(Color.RED)
                .setFlag(JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemShowManipulationHandlers);
        scene.addItem(item5);

        scene.addItem(new JGraphicsLineItem(50, 50, 100, 100)
                .setArrowOnStart(true)
                .setArrowOnEnd(true)
                .setArrowStartColor(Color.RED)
                .setArrowEndColor(Color.GREEN)
                .setZOrder(2)
                .addEffect(new JGraphicsBlinkEffect())
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemShowManipulationHandlers));
        imageItem = scene.addItem(new JGraphicsImageItem(200, 200, ImageIO.read(TestMain.class.getResourceAsStream("/icons/boy.png")))
                .setWidth(500).setZOrder(2)
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemShowManipulationHandlers));
        JGraphicsItem girlItem = scene.addItem(new JGraphicsImageItem(300, 40, ImageIO.read(TestMain.class.getResourceAsStream("/icons/girl.png")))
                .setFlag(JGraphicsItem.ItemAppliesOwnEffectToChildren | JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemShowManipulationHandlers));
        girlItem.addEffect(new JGraphicsBlinkEffect());
        JGraphicsItem girl2Item = girlItem.addItem(new JGraphicsImageItem(50, 50, ImageIO.read(TestMain.class.getResourceAsStream("/icons/girl.png")))
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemIgnoresParentScale).setRotation((float) Math.toRadians(10)));
        scene.addItem(new JGraphicsTextItem(450, 40, 150, 100, "The cat (Felis catus) is a domestic species of small carnivorous mammal.")
                        .setHAlign(JGraphicsTextItem.HAlign.CENTER).setVAlign(JGraphicsTextItem.VAlign.CENTER))
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemShowManipulationHandlers);
        scene.addItem(new JGraphicsTextItem(450, 150, 150, 100, "Some big text")
                        .setFont(JGraphicsTextItem.getDefaultFont().deriveFont(Font.ITALIC, 35f)).setVAlign(JGraphicsTextItem.VAlign.CENTER)
                        .setTextColor(Color.GREEN))
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemShowManipulationHandlers);

        scene.addItem(new JGraphicsSplineItem(new Point2D.Float(100, 200), new Point2D.Float(300, 200))
                .setArrowOnEnd(true).setArrowOnStart(true).setArrowEndColor(Color.RED).setArrowStartColor(Color.GREEN).setShowManipulatorsOnEnds(true)
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemShowManipulationHandlers));
        scene.addItem(new JGraphicsSplineItem(new Point2D.Float(100, 250), new Point2D.Float(300, 250))
                .setArrowOnEnd(true).setArrowOnStart(true).setArrowEndColor(Color.RED).setArrowStartColor(Color.GREEN).setShowManipulatorsOnEnds(false)
                .setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemShowManipulationHandlers));
        view1 = new JGraphicsView().setWheelZoomEnabled(true).setMiddleMousePanEnabled(true).setZoomToPointer(true).setDragMode(DragMode.RubberBandDrag);
        view1.setOffSceneColor(Color.GREEN);
        view1.setPreferredSize(new Dimension(800, 500));
        view1.setScene(scene);
        view1.setAntialias(true);
        view1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        viewsPanel.add(view1);

        Timer timer = new Timer(20, (e) -> {
            item2.setRotation((float) (item2.getRotation() + Math.toRadians(0.3)));
            item2.dirtyTransform();
            scaleTimeline += 5;
            item1.setScale((float) Math.sin(Math.toRadians(scaleTimeline)));
            item1.dirtyTransform();
            view1.repaint();
        });

        timer.setRepeats(true);
        timer.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                view1.dispose();
            }
        });
    }
}