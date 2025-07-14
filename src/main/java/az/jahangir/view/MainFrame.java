package az.jahangir.view;

import az.jahangir.model.GraphModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private final GraphPanel graphPanel;
    private final JTextField functionField;
    private final JButton plotButton;
    private final JButton resetButton;
    private final JLabel statusLabel;
    private final JCheckBox derivativeCheckBox;

    public MainFrame(GraphModel model) {
        this.graphPanel = new GraphPanel(model);

        setTitle("Graph Plotter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        add(graphPanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        derivativeCheckBox = new JCheckBox("Show f'(x)", true);
        functionField = new JTextField("x^2", 25);
        plotButton = new JButton("Plot");
        resetButton = new JButton("Reset");
        controlPanel.add(new JLabel("f(x) = "));
        controlPanel.add(functionField);
        controlPanel.add(plotButton);
        controlPanel.add(resetButton);
        controlPanel.add(derivativeCheckBox);

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    public JCheckBox getDerivativeCheckBox() {
        return derivativeCheckBox;
    }

    public JTextField getFunctionField() {
        return functionField;
    }

    public JButton getPlotButton() {
        return plotButton;
    }

    public JButton getResetButton() {
        return resetButton;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public GraphPanel getGraphPanel() {
        return graphPanel;
    }
}