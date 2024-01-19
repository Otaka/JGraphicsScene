# JGraphicsScene

This is a small Java Swing graphics framework that is somewhat similar to Qt QGraphicsScene. 
It allows users to place items on the scene and configure their behavior, such as movement with the mouse or with a
timer.

<p>
  <img src="https://raw.githubusercontent.com/Otaka/JGraphicsScene/master/readme_res/showcase.png" alt="Small demo" width="692" height="373" />
</p>

### Usage
First you should add the following dependency to your maven project:

```xml
<dependency>
    <groupId>io.github.otaka</groupId>
    <artifactId>jgraphicsscene</artifactId>
    <version>0.1</version>
</dependency>
```

Then in java code you can create a scene and add items to it:

```java
JGraphicsScene scene = new JGraphicsScene();
JGraphicsView view = new JGraphicsView().setWheelZoomEnabled(true).setMiddleMousePanEnabled(true).setZoomToPointer(true).setDragMode(DragMode.RubberBandDrag);
view.setOffSceneColor(Color.GREEN);
view.setPreferredSize(new Dimension(800, 500));
view.setScene(scene);
view.setAntialias(true);
view.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//Add view to your JFrame or JPanel

//Add item to the scene
JGraphicsItem rectangleItem = new JGraphicsRectItem(300, 100, 30, 30)
        .setFillColor(Color.MAGENTA)
        .setFlag(JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemIsSelectable | JGraphicsItem.ItemShowManipulationHandlers);
scene.addItem(rectangleItem);
```


### Items

* **JGraphicsRectItem** - rectangle. You can configure fill color and border color.
* **JGraphicsEllipseItem** - ellipse. You can configure fill color and border color.
* **JGraphicsTextItem** - multiline text with wrapping. You can configure text color, font and alignment.
* **JGraphicsImageItem** - BufferedImage item
* **JGraphicsLineItem** - line. You can configure line color and width. Also you can configure the arrows on line ends.
* **JGraphicsSplineItem** - curved line. You can provide a list of points and the spline will be drawn through them. You can configure line color and width. Also you can configure the arrows on line ends.


Also you can implement your own type of JGraphicsItem by extending existing item class, or abstract JGraphicsItem class.

### Item flags
In any item you can configure flags that will change its behavior.
* **ItemIsMovable** - item can be moved with mouse
* **ItemIsSelectable** - item can be selected with mouse(if item is selected it will be drawn with a border)
* **ItemShowManipulationHandlers** - item will show manipulation handlers when selected, so it can be resized with mouse
* **ItemIgnoresParentScale** - the item will not be scaled when it's parent is scaled

### Item events
You can subscribe to item events:
```java
 item.addEvent((eventType, item, arg) -> {
    if (eventType == ItemChangedType.Position) {
        ...
    }
    return true;
});
```
There are no much events yet:
* **Position** - when the item position is changed
* **DoubleClick** - when the item is double clicked
* **MousePress** - when the mouse is pressed on the item
* **MouseRelease** - when the mouse is released on the item
* **ManipulatorPressed** - when the manipulation handler is pressed
* **ManipulatorMoved** - when the manipulation handler is moved
* **ManipulatorReleased** - when the manipulation handler is released

### Connections
It is often needed to allow user to connect items with lines or arrows. For this purpose there is a special class - **ConnectionManager**

It allows you to configure connection targets on the item. Connection lines can connect the items only through the connection targets.
The ConnectionManager requires **ConnectionManagerConfiguration** object serves as a factory for connection lines, and provide callback to check if the connection is allowed.
It is better to check the TestConnections.java example(located in tests) to see how it works.

ConnectionManager stores some data about the connections inside, so if you use it, you should be carefull when removing items from scene, you should not forget to remove it from ConnectionManager also.


### Serialization and deserialization
It is not very easy to serialize and deserialize the scene especially if you have custom items and connections(ConnectionManager internal state)

For this purpose there is a special method connectionManager.createSerializer() that returns a serializer object that can serialize and deserialize the scene.

Most important thing in serializer - method setExportCustomizer(ConnectionsManagerExportImport.ExportCustomizer exportCustomizer). It allows you to write a code that will serialize items to Java HashMap, because it may not know anything about your custom items and their data.

The serializer can also deserialize the scene back. It will create items and connections, but it will not be able to restore their data, so you should provide a customizer that will restore the data from HashMap to your items.

To see how it actually works you can check the TestConnections.java example(located in tests).