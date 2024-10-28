package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SimpleGraphicEditor extends JFrame {

    private String currentTool = "Pencil";
    private int currentThickness = 5;
    private Point lastPoint = null;
    private Point startPoint = null;
    private BufferedImage canvas;
    private Graphics2D g2d;
    private Color currentColor = Color.BLACK;
    private Color currentFillColor = Color.BLUE;
    private boolean isFilled = true;

    private final DrawingPanel drawingPanel;

    public SimpleGraphicEditor() {
        setResizable(true);
        setMinimumSize(new Dimension(800, 600));

        initCanvas();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}

            @Override
            public void windowClosed(WindowEvent e) {}

            @Override
            public void windowIconified(WindowEvent e) {}

            @Override
            public void windowDeiconified(WindowEvent e) {}

            @Override
            public void windowActivated(WindowEvent e) {}

            @Override
            public void windowDeactivated(WindowEvent e) {}

            @Override
            public void windowClosing(WindowEvent e) {
                Object[] options = {"Сохранить", "Не сохранять", "Отмена"};
                int rc = JOptionPane.showOptionDialog(e.getWindow(), "Если вы не сохраните изображение, то вы его не сохраните", "Сохранить изображение перед выходом?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (rc == 0) {
                    e.getWindow().setVisible(false);
                    saveBMPImage();
                    System.exit(0);
                }
                else if (rc == 1) {
                    e.getWindow().setVisible(false);
                    System.exit(0);
                }
            }
        });

        JMenuBar jMenuBar = new JMenuBar();
        JMenu menu0 = new JMenu("Файл");
        JMenu menu1 = new JMenu("Инструменты");
        JMenu menu2 = new JMenu("Параметры");
        JMenu menu3 = new JMenu("Фигуры");

        menu1.add("Карандаш").addActionListener(e -> currentTool = "Pencil");
        menu1.add("Кисть").addActionListener(e -> currentTool = "Brush");
        menu1.add("Ластик").addActionListener(e -> currentTool = "Eraser");
        menu2.add("Толщина").addActionListener(e -> showThicknessDialog());
        menu2.add("Цвет").addActionListener(e -> currentColor = JColorChooser.showDialog(this, "Выберите цвет", currentColor));
        menu3.add("Прямоугольник").addActionListener(e -> {
            currentTool = "Rectangle";
            isFilled = false;
        });
        menu3.add("Окружность").addActionListener(e -> {
            currentTool = "Circle";
            isFilled = false;
        });
        menu3.add("Прямоугольник (залить цветом)").addActionListener(e -> {
            currentTool = "Rectangle";
            isFilled = true;
            currentFillColor = JColorChooser.showDialog(this, "Выберите цвет заливки", currentFillColor);
        });
        menu3.add("Окружность (залить цветом)").addActionListener(e -> {
            currentTool = "Circle";
            isFilled = true;
            currentFillColor = JColorChooser.showDialog(this, "Выберите цвет заливки", currentFillColor);
        });
        menu3.addSeparator();
        menu3.add("Комбинированная область").addActionListener(e -> Function13());
        menu0.add("Открыть изображение").addActionListener(e -> loadBMPImage());
        menu0.add("Сохранить изображение").addActionListener(e -> saveBMPImage());
        menu0.add("Выход").addActionListener(e -> System.exit(0));

        jMenuBar.add(menu0);
        jMenuBar.add(menu1);
        jMenuBar.add(menu2);
        jMenuBar.add(menu3);
        setJMenuBar(jMenuBar);

        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
                startPoint = e.getPoint();
            }

            public void mouseReleased(MouseEvent e) {
                if (currentTool.equals("Rectangle") || currentTool.equals("Circle")) {
                    g2d.setStroke(new BasicStroke(currentThickness));
                    if (currentTool.equals("Rectangle")) {
                        Rectangle rect = new Rectangle(Math.min(startPoint.x, lastPoint.x), Math.min(startPoint.y, lastPoint.y), Math.abs(lastPoint.x - startPoint.x), Math.abs(lastPoint.y - startPoint.y));
                        if (isFilled) {
                            g2d.setColor(currentFillColor);
                            g2d.fill(rect);
                        }
                        g2d.setColor(currentColor);
                        g2d.draw(rect);
                    } else {
                        int diameter = Math.max(Math.abs(lastPoint.x - startPoint.x), Math.abs(lastPoint.y - startPoint.y));
                        int x = startPoint.x + (lastPoint.x < startPoint.x ? -diameter : 0);
                        int y = startPoint.y + (lastPoint.y < startPoint.y ? -diameter : 0);
                        Ellipse2D circle = new Ellipse2D.Double(x, y, diameter, diameter);
                        if (isFilled) {
                            g2d.setColor(currentFillColor);
                            g2d.fill(circle);
                        }
                        g2d.setColor(currentColor);
                        g2d.draw(circle);
                    }
                    drawingPanel.repaint();
                    lastPoint = null;
                    startPoint = null;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    g2d.setStroke(new BasicStroke(currentThickness));
                    g2d.setColor(currentColor);
                    switch (currentTool) {
                        case "Pencil" -> {
                            g2d.setStroke(new BasicStroke(currentThickness));
                            g2d.drawLine(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                        }
                        case "Eraser" -> {
                            g2d.setColor(Color.WHITE);
                            g2d.setStroke(new BasicStroke(currentThickness));
                            g2d.drawLine(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                        }
                        case "Brush" -> {
                            g2d.setStroke(new BasicStroke(currentThickness * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2d.drawLine(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                        }
                        case "Rectangle" -> {
                        }
                    }
                    lastPoint = e.getPoint();
                    drawingPanel.repaint();
                }
            }
        });


        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                resizeCanvas(getWidth(), getHeight());
            }
        });
    }

    private void Function13() {
        g2d.setStroke(new BasicStroke(currentThickness));

        Area rectangleArea = new Area(new Rectangle(50, 50, 100, 50));
        Area circleArea = new Area(new Ellipse2D.Double(75, 40, 150, 150));
        Area triangleArea = new Area(new Polygon(new int[]{200, 250, 150}, new int[]{50, 150, 150}, 3));
        Area ellipseArea = new Area(new Ellipse2D.Double(200, 50, 120, 80));

        g2d.setColor(Color.BLUE);
        g2d.fill(rectangleArea);

        g2d.setColor(Color.RED);
        g2d.fill(circleArea);

        g2d.setColor(Color.YELLOW);
        g2d.fill(triangleArea);

        g2d.setColor(Color.GREEN);
        g2d.fill(ellipseArea);


        Area intersection = new Area(rectangleArea);
        intersection.intersect(circleArea);
        g2d.setColor(blendColors(Color.BLUE, Color.RED));
        g2d.fill(intersection);

        intersection = new Area(rectangleArea);
        intersection.intersect(triangleArea);
        g2d.setColor(blendColors(Color.BLUE, Color.YELLOW));
        g2d.fill(intersection);

        intersection = new Area(circleArea);
        intersection.intersect(triangleArea);
        g2d.setColor(blendColors(Color.RED, Color.YELLOW));
        g2d.fill(intersection);

        intersection = new Area(circleArea);
        intersection.intersect(ellipseArea);
        g2d.setColor(blendColors(blendColors(Color.RED, Color.YELLOW), Color.GREEN));
        g2d.fill(intersection);

        drawingPanel.repaint();
    }

    private Color blendColors(Color c1, Color c2) {
        return new Color((c1.getRed() + c2.getRed())/2, (c1.getGreen() + c2.getGreen())/2, (c1.getBlue() + c2.getBlue()/2));
    }

    private void loadBMPImage() {
        try {
            FileDialog fileDialog = new FileDialog(this, "Загрузить BMP изоюражение", FileDialog.LOAD);
            fileDialog.setFile("*.bmp");
            fileDialog.setVisible(true);

            String file = fileDialog.getDirectory() + fileDialog.getFile();
            if (file != null) {
                canvas = ImageIO.read(new File(file));
                g2d = canvas.createGraphics();
                drawingPanel.repaint();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке изображения: " + e.getMessage());
        }
    }

    private void saveBMPImage() {
        try {
            FileDialog fileDialog = new FileDialog(this, "Сохранить изображение как", FileDialog.SAVE);
            fileDialog.setFile("image.bmp");
            fileDialog.setVisible(true);

            String directory = fileDialog.getDirectory();
            String fileName = fileDialog.getFile();

            if (directory != null && fileName != null) {
                if (!fileName.toLowerCase().endsWith(".bmp")) {
                    fileName += ".bmp";
                }

                File file = new File(directory, fileName);

                boolean result = ImageIO.write(canvas, "BMP", file);
                if (result) {
                    JOptionPane.showMessageDialog(this, "Изображение успешно сохранено в: " + file.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(this, "Не удалось сохранить изображение.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Директория или имя файла не выбраны.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при сохранении изображения: " + e.getMessage());
        }
    }

    private void initCanvas() {
        canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        g2d = canvas.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void resizeCanvas(int width, int height) {
        BufferedImage newCanvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2dNew= newCanvas.createGraphics();
        g2dNew.fillRect(0, 0, width, height);
        g2dNew.drawImage(canvas, 0, 0, null);

        double scaleX = (double) width / canvas.getWidth();
        double scaleY = (double) height / canvas.getHeight();

        g2dNew.drawImage(canvas.getScaledInstance((int) (canvas.getWidth() * scaleX), (int) (canvas.getHeight() * scaleY), Image.SCALE_SMOOTH), 0, 0, null);

        canvas = newCanvas;
        g2d.dispose();
        g2d = canvas.createGraphics();
        drawingPanel.repaint();
    }

    private void showThicknessDialog() {
        JSlider jSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, currentThickness);
        jSlider.setMajorTickSpacing(10);
        jSlider.setMinorTickSpacing(1);
        jSlider.setPaintTicks(true);
        jSlider.setPaintLabels(true);

        JOptionPane.showMessageDialog(this, jSlider, "Выберите толщину линии", JOptionPane.PLAIN_MESSAGE);

        currentThickness = jSlider.getValue();
    }


    private class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(canvas, 0, 0, this);

            if (lastPoint != null && startPoint != null && (currentTool.equals("Rectangle") || currentTool.equals("Circle")))  {
                g2d.setStroke(new BasicStroke(currentThickness));
                g2d.setColor(currentColor);
                if (currentTool.equals("Rectangle")) {
                    Rectangle tempRect = new Rectangle(Math.min(startPoint.x, lastPoint.x), Math.min(startPoint.y, lastPoint.y),
                            Math.abs(lastPoint.x - startPoint.x), Math.abs(lastPoint.y - startPoint.y));
                    g.drawRect(tempRect.x, tempRect.y, tempRect.width, tempRect.height);
                } else {
                    int diameter = Math.max(Math.abs(lastPoint.x - startPoint.x), Math.abs(lastPoint.y - startPoint.y));
                    int x = startPoint.x + (lastPoint.x < startPoint.x ? -diameter : 0);
                    int y = startPoint.y + (lastPoint.y < startPoint.y ? -diameter : 0);
                    g.drawOval(x, y, diameter, diameter);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleGraphicEditor simpleGraphicEditor = new SimpleGraphicEditor();
            simpleGraphicEditor.setVisible(true);
        });
    }
}