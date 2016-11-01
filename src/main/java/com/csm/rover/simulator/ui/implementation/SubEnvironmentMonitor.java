package com.csm.rover.simulator.ui.implementation;

import com.csm.rover.simulator.environments.EnvironmentIO;
import com.csm.rover.simulator.environments.EnvironmentRegistry;
import com.csm.rover.simulator.environments.PlatformEnvironment;
import com.csm.rover.simulator.environments.rover.TerrainEnvironment;
import com.csm.rover.simulator.environments.sub.AquaticEnvironment;
import com.csm.rover.simulator.objects.io.EnvrioFileFilter;
import com.csm.rover.simulator.objects.util.DecimalPoint;
import com.csm.rover.simulator.ui.visual.PopulatorDisplayFunction;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

@FrameMarker(name = "Sub Environment Monitor", platform = "Sub")
class SubEnvironmentMonitor extends EnvironmentDisplay {
    private static final Logger LOG = LogManager.getLogger(SubEnvironmentMonitor.class);

    private JPanel content;
    private TerrainMapDisplay display;

    private DecimalPoint focus;

    protected SubEnvironmentMonitor() {
        super("Sub");
        focus = new DecimalPoint(0, 0);
        initialize();
    }

    private void initialize() {
        content = new JPanel();
        setBackground(Color.BLACK);
        setLayout(null);
        setContentPane(content);
        setVisible(false);
        setTitle("Sub Environment Monitor");
        setSize(500, 500);
    }

    private void showMenu(Point p){
        JPopupMenu menu = new JPopupMenu();
        menu.setVisible(false);
        menu.setLocation(p);
        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (e.getX() <= menu.getX() ||
                        e.getX() >= menu.getX()+menu.getWidth() ||
                        e.getY() <= menu.getY() ||
                        e.getY() >= menu.getY()+menu.getHeight()) {
                    menu.setVisible(false);
                }
            }
        });

        JMenuItem mntmSaveMapFile = new JMenuItem("Save Map File");
        mntmSaveMapFile.addActionListener((e) -> {
            JFileChooser finder = new JFileChooser();
            finder.setFileFilter(new EnvrioFileFilter(platform_type));
            finder.setApproveButtonText("Save");
            finder.setCurrentDirectory(UiConfiguration.getFolder(platform_type));
            int option = finder.showSaveDialog(getParent());
            menu.setVisible(false);
            SwingUtilities.invokeLater(() -> {
                if (option == JFileChooser.APPROVE_OPTION) {
                    UiConfiguration.changeFolder(platform_type, finder.getCurrentDirectory());
                    EnvironmentIO.saveEnvironment(display.getEnvironment(), EnvironmentIO.appendSuffix(platform_type, finder.getSelectedFile()));
                }
            });
        });
        menu.add(mntmSaveMapFile);

        for (String pop : display.getPopulators()){
            JRadioButtonMenuItem item = new JRadioButtonMenuItem("Show " + pop);
            item.setSelected(display.isPopulatorVisible(pop));
            item.addActionListener((a) -> {
                display.setPopulatorVisible(pop, item.isSelected());
                menu.setVisible(false);
            });
            menu.add(item);
        }

        menu.setVisible(true);
    }

    private void setFocusPoint(DecimalPoint point){
        focus = point;
        redraw();
    }

    void redraw(){
        display.setLocation((int)Math.round(this.getWidth()/2.0-focus.getX()* display.getResolution()- display.getWidth()/2.0),
                (int)Math.round(this.getHeight()/2.0+focus.getY()* display.getResolution() - display.getHeight()/2.0));
    }

    private Optional<Point> last = Optional.empty();
    private void dragMap(Point p){
        if (p == null){
            last = Optional.empty();
        }
        else if (last.isPresent()){
            int dx = last.get().x - p.x;
            int dy = p.y - last.get().y;
            double cx = dx / (double)display.getResolution();
            double cy = dy / (double)display.getResolution();
            setFocusPoint(new DecimalPoint(this.focus.getX()+cx, this.focus.getY()+cy));
        }
        else {
            last = Optional.of(p);
        }
    }

    private void zoomMap(int zoom){
        display.setResolution(display.getResolution()+zoom);
        redraw();
    }

    @Override
    protected void doSetEnvironment(PlatformEnvironment environment) {
        display = new SubMapDisplay((AquaticEnvironment)environment);
        display.setResolution(20);
        content.add(display);

        display.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1){
                    dragMap(e.getPoint());
                }
                if (e.getButton() == 3){
                    showMenu(new Point(e.getXOnScreen()-2, e.getYOnScreen()-2));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragMap(null);
            }
        });
        display.addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseDragged(MouseEvent e) {
                dragMap(e.getPoint());
            }
        });
        display.addMouseWheelListener((e) -> zoomMap(5 * -e.getWheelRotation()));

        getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "pan left");
        getActionMap().put("pan left", ActionBuilder.newAction(() -> setFocusPoint(focus.offset(-1, 0))));
        getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "pan right");
        getActionMap().put("pan right", ActionBuilder.newAction(() -> setFocusPoint(focus.offset(1, 0))));
        getInputMap().put(KeyStroke.getKeyStroke("UP"), "pan up");
        getActionMap().put("pan up", ActionBuilder.newAction(() -> setFocusPoint(focus.offset(0, 1))));
        getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "pan down");
        getActionMap().put("pan down", ActionBuilder.newAction(() -> setFocusPoint(focus.offset(0, -1))));
        redraw();
    }

    @Override
    protected void update() {
        redraw();
    }

}

class SubMapDisplay extends JPanel {
    private static final Logger LOG = LogManager.getLogger(TerrainMapDisplay.class);

    private final AquaticEnvironment subMap;

    private int squareResolution = 50;

    private Map<String, Boolean> viewPopulators;
    private Map<String, PopulatorDisplayFunction> popDisplays;

    SubMapDisplay(AquaticEnvironment environment){
        this.subMap = environment;
        viewPopulators = new HashMap<>();
        popDisplays = new HashMap<>();
        for (String pop : subMap.getPopulators()){
            viewPopulators.put(pop, true);
            popDisplays.put(pop, EnvironmentRegistry.getPopulatorDisplayFunction(subMap.getType(), pop));
        }
        this.setBounds(0, 0, 100, 100);
        this.setBackground(Color.BLACK);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                setSize(subMap. * squareResolution + 1, subMap.getSize() * squareResolution + 1);
            }
        });
    }

    //paint the map
    //kind long and ugly, I know, but necessary for performance
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            int xstart = (-this.getLocation().x / squareResolution - 1);
            int xend = xstart + (this.getParent().getWidth() / squareResolution + 3);
            if (xstart < 0){
                xstart = 0;
            }
            if (xend > subMap.getSize()){
                xend = subMap.getSize();
            }
            int ystart = (-this.getLocation().y / squareResolution - 1);
            int yend = ystart + (this.getParent().getHeight() / squareResolution + 4);
            if (ystart < 0){
                ystart = 0;
            }
            if (yend > subMap.getSize()){
                yend = subMap.getSize();
            }
            int fails = 0;
            for (int y = ystart; y < yend; y++){
                for (int x = xstart; x < xend; x++){
                    try {
                        DecimalPoint loc = new DecimalPoint(x + 0.5, y + 0.5);
                        Color color = null;
                        for (String pop : viewPopulators.keySet()) {
                            if (viewPopulators.get(pop) && color == null) {
                                double value = subMap.getPopulatorValue(pop, loc);
                                color = value == 0 ? null : popDisplays.get(pop).displayFunction(value);
                            }
                        }
                        if (color == null) {
                            double scaled = subMap.getDensityAt(loc) / subMap.getMaxDensity() * 100;
                            int red, green = 0, blue = 0;
                            if (scaled < 25) {
                                red = (int) ((scaled) / 25 * 255);
                            }
                            else if (scaled < 50) {
                                red = 255;
                                green = (int) ((scaled - 25) / 25 * 255);
                            }
                            else if (scaled < 75) {
                                red = (int) ((25 - (scaled - 50)) / 25 * 255);
                                green = 255;
                            }
                            else if (scaled < 100) {
                                red = (int) ((scaled - 75) / 25 * 255);
                                green = 255;
                                blue = red;
                            }
                            else {
                                red = 255;
                                green = 255;
                                blue = 255;
                            }
                            color = new Color(red, green, blue);
                        }

                        g.setColor(color);
                        g.fillRect(x * squareResolution, y * squareResolution, squareResolution, squareResolution);

                        g.setColor(Color.DARK_GRAY);
                        g.drawRect(x * squareResolution, y * squareResolution, squareResolution, squareResolution);
                    }
                    catch (Exception e){
                        if (fails > 10){
                            throw e;
                        }
                        else {
                            LOG.log(Level.WARN, "Failed to draw square [" + x + ", " + y + "]: " + e.getMessage());
                            fails++;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            LOG.log(Level.ERROR, "Failed to draw map", e);
        }
    }

    PlatformEnvironment getEnvironment(){
        return subMap;
    }

    Set<String> getPopulators(){
        return viewPopulators.keySet();
    }

    void setPopulatorVisible(String pop, boolean b){
        if (viewPopulators.containsKey(pop)){
            viewPopulators.put(pop, b);
        }
        repaint();
    }

    boolean isPopulatorVisible(String pop){
        return viewPopulators.get(pop);
    }

    void setResolution(int res){
        if (res > 0){
            squareResolution = res;
            setSize(subMap.getSize() * squareResolution, subMap.getSize() * squareResolution);
            this.repaint();
        }
    }

    int getResolution(){
        return squareResolution;
    }

}
