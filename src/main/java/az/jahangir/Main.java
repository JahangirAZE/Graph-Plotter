package az.jahangir;

import az.jahangir.controller.GraphController;
import az.jahangir.model.GraphModel;
import az.jahangir.view.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            GraphModel model = new GraphModel();

            MainFrame view = new MainFrame(model);

            GraphController controller = new GraphController(model, view);

            controller.initController();

            view.setVisible(true);
        });
    }
}