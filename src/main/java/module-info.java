module athensclub.speedtype {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires org.fxmisc.richtext;
    requires reactfx;
    requires com.jfoenix;

    exports athensclub.speedtype.components;

    opens athensclub.speedtype to javafx.graphics;
    opens athensclub.speedtype.controllers to javafx.fxml;
    opens athensclub.speedtype.components to javafx.fxml;
}