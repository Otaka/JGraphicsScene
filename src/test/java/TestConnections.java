import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jgraphicsscene.DragMode;
import com.jgraphicsscene.JGraphicsScene;
import com.jgraphicsscene.JGraphicsView;
import com.jgraphicsscene.connection.ConnectionConfig;
import com.jgraphicsscene.connection.ConnectionManager;
import com.jgraphicsscene.connection.ConnectionManagerConfiguration;
import com.jgraphicsscene.connection.ConnectionsManagerExportImport;
import com.jgraphicsscene.connection.ExportData;
import com.jgraphicsscene.connection.NodeConfig;
import com.jgraphicsscene.connection.TargetConfig;
import com.jgraphicsscene.node.JGraphicsEllipseItem;
import com.jgraphicsscene.node.JGraphicsItem;
import com.jgraphicsscene.node.JGraphicsRectItem;
import com.jgraphicsscene.node.JGraphicsSplineItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestConnections extends JFrame {

    JPanel viewsPanel;
    JGraphicsView view;
    JGraphicsScene scene;
    ConnectionManager connectionManager;

    public static void main(String[] args) {
        System.out.println("Start Connections test");
        SwingUtilities.invokeLater(() -> new TestConnections().start());
    }

    private void start() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Test Graph Editor");
        setLayout(new BorderLayout());
        viewsPanel = new JPanel();
        viewsPanel.setPreferredSize(new Dimension(800, 800));
        add(viewsPanel, BorderLayout.CENTER);
        setupScene();

        JPanel manipPanel = createManipulationPanel();
        add(manipPanel, BorderLayout.EAST);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createManipulationPanel() {
        JPanel manipPanel = new JPanel();
        manipPanel.setPreferredSize(new Dimension(300, 300));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener((e) -> save());
        manipPanel.add(saveButton);
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener((e) -> load());
        manipPanel.add(loadButton);
        return manipPanel;
    }


    private void setupScene() {
        scene = new JGraphicsScene();
        view = new JGraphicsView().setWheelZoomEnabled(true).setMiddleMousePanEnabled(true).setZoomToPointer(true).setDragMode(DragMode.RubberBandDrag);

        view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                    List<JGraphicsItem> selectedItems = view.getSelectedItems();
                    for (JGraphicsItem selectedItem : selectedItems) {
                        connectionManager.remove(selectedItem);
                    }
                    view.hideManipulators();
                    scene.fireRedraw();
                }
            }
        });
        view.setPreferredSize(new Dimension(800, 800));
        view.setScene(scene);
        view.setAntialias(true);
        view.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        viewsPanel.add(view);
        ConnectionManagerConfiguration configuration = new ConnectionManagerConfiguration() {

            @Override
            public boolean checkAllowToConnect(ConnectionConfig connection, boolean draggingEndManipulator, TargetConfig nodeTargetConfig) {
                if (draggingEndManipulator) {
                    return nodeTargetConfig.hasUserTag("in");
                } else {
                    return nodeTargetConfig.hasUserTag("out");
                }
            }

            @Override
            public JGraphicsSplineItem createConnection(TargetConfig targetConfig) {
                if (targetConfig.hasUserTag("in")) return null;
                JGraphicsSplineItem connection = new JGraphicsSplineItem(new Point2D.Float(0, 0), new Point2D.Float(100, 100));
                connection.setArrowOnEnd(true).setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemShowManipulationHandlers);
                return connection;
            }
        };
        connectionManager = new ConnectionManager(view, scene, configuration);
        JGraphicsItem n1 = createNode(60, 60).setPosition(100, 100, false);
        JGraphicsItem n2 = createNode(60, 60).setPosition(200, 100, false);
        JGraphicsItem n3 = createNode(60, 60).setPosition(100, 200, false);
        connectionManager.createConnection(n1, 1, n2, 0);
    }

    private JGraphicsItem createNode(int width, int height) {
        JGraphicsRectItem node = new JGraphicsRectItem(0, 0, width, height);
        node.setXOffset(-width / 2f);
        node.setYOffset(-height / 2f);
        node.setFlag(JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemIsSelectable);
        scene.addItem(node);

        JGraphicsEllipseItem targetIn = new JGraphicsEllipseItem(-width / 2f, 0, 10, 10);
        targetIn.setFlag(JGraphicsItem.ItemIsSelectable);
        targetIn.setFillColor(Color.RED);
        targetIn.setXOffset(-5);
        targetIn.setYOffset(-5);
        node.addItem(targetIn);

        JGraphicsEllipseItem targetOut = new JGraphicsEllipseItem(width / 2f, 0, 10, 10);
        targetOut.setFlag(JGraphicsItem.ItemIsSelectable);
        targetOut.setFillColor(Color.GREEN);
        targetOut.setXOffset(-5);
        targetOut.setYOffset(-5);
        node.addItem(targetOut);

        connectionManager.initConnectionTarget(0, targetIn, node).setUserTags("in");
        connectionManager.enableCreatingConnectionByDraggingConnectionTarget(targetIn, MouseEvent.BUTTON1);
        connectionManager.initConnectionTarget(1, targetOut, node).setUserTags("out");
        connectionManager.enableCreatingConnectionByDraggingConnectionTarget(targetOut, MouseEvent.BUTTON1);
        return node;
    }

    private void save() {
        try {
            PrintStream printStream = new PrintStream(new FileOutputStream("./testData.json"));
            ExportData exportData = connectionManager.createSerializer().setExportCustomizer(new ConnectionsManagerExportImport.ExportCustomizer() {
                @Override
                public void fillNodeData(NodeConfig node, Object extraNodeData) {
                    ((Map<String, String>) extraNodeData).put("name", "Object1");
                }

                @Override
                public void fillConnectionData(ConnectionConfig connection, Object extraConnectionData) {
                    ((Map<String, String>) extraConnectionData).put("name", "Connection1");
                }

                @Override
                public HashMap<String, String> createExtraNodeData(NodeConfig node) {
                    return new HashMap<>();
                }

                @Override
                public HashMap<String, String> createExtraConnectionData(ConnectionConfig connection) {
                    return new HashMap<>();
                }
            }).exportData();
            new GsonBuilder().setPrettyPrinting().create().toJson(exportData, printStream);
            printStream.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void load() {
        try {
            connectionManager.clear();
            ExportData data = new Gson().fromJson(new FileReader("./testData.json"), ExportData.class);
            connectionManager.createSerializer()
                    .setImportCustomizer(new ConnectionsManagerExportImport.ImportCustomizer() {
                        @Override
                        public JGraphicsItem createNode(Object extraNodeData) {
                            return TestConnections.this.createNode(60, 60);
                        }

                        @Override
                        public JGraphicsSplineItem createConnection(Object extraConnectionData) {
                            return (JGraphicsSplineItem) new JGraphicsSplineItem(new Point2D.Float(0, 0), new Point2D.Float(100, 100))
                                    .setArrowOnEnd(true).setFlag(JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemShowManipulationHandlers);
                        }
                    })
                    .importData(data);
            scene.fireRedraw();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
