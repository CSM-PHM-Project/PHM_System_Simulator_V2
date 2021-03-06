/*
 * SPEARS: Simulated Physics and Environment for Autonomous Risk Studies
 * Copyright (C) 2017  Colorado School of Mines
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.spears.ui.implementation;

import com.spears.environments.PlatformEnvironment;
import com.spears.platforms.Platform;
import com.spears.ui.sound.SoundPlayer;
import com.spears.ui.sound.SpearsSound;
import com.spears.ui.sound.VolumeChangeListener;
import com.spears.ui.visual.MainMenu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class EmbeddedMenuBar extends JMenuBar implements MainMenu {

	private static final long serialVersionUID = 1996909612935260422L;

	private JMenu fileMenu, viewMenu, optionsMenu;
	
	private NewFrameMenu newMenu;
	private JMenu showMenu;

    private Optional<Runnable> exitOp = Optional.empty();
    private Optional<VolumeChangeListener> volumeListener = Optional.empty();
	
	EmbeddedMenuBar() {
		fileMenu = new JMenu("File");
		this.add(fileMenu);
		
		viewMenu = new JMenu("View");
		this.add(viewMenu);
		
		optionsMenu = new JMenu("Options");
		this.add(optionsMenu);
		
		initialize();
	}

	private void initialize(){
		
		JMenuItem mntmOpen = new JMenuItem("Open Simulation");
		mntmOpen.setIcon(ImageFunctions.getMenuIcon("/gui/open_folder.png"));
		mntmOpen.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.OPEN).setOrigin(e).build()));
		fileMenu.add(mntmOpen);
		
		JMenuItem mntmNewSimulation = new JMenuItem("New Simulation");
		mntmNewSimulation.setIcon(ImageFunctions.getMenuIcon("/gui/add.png"));
		mntmNewSimulation.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.NEW_SIM).setOrigin(e).build()));
		fileMenu.add(mntmNewSimulation);
		
		fileMenu.addSeparator();
		
		JMenuItem mntmViewLogFile = new JMenuItem("View Log File");
		mntmViewLogFile.setIcon(ImageFunctions.getMenuIcon("/gui/file.png"));
		mntmViewLogFile.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.VIEW_LOG).setValue("SPEARS").setOrigin(e).build()));
		fileMenu.add(mntmViewLogFile);
		
		JMenu mnViewPlatformLog = new JMenu("View Platform Log");
		fileMenu.add(mnViewPlatformLog);
		
		fileMenu.addSeparator();
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener((ActionEvent e) -> SoundPlayer.playSound(SpearsSound.GOODBYE));
		mntmExit.setIcon(ImageFunctions.getMenuIcon("/gui/power.png"));
		mntmExit.addActionListener((ActionEvent e) -> {if (exitOp.isPresent()) exitOp.get().run(); });
		fileMenu.add(mntmExit);
		
		newMenu = new NewFrameMenu();
		newMenu.setText("New Window");
		newMenu.setIcon(ImageFunctions.getMenuIcon("/gui/user_id.png"));
		viewMenu.add(newMenu);
		
		viewMenu.addSeparator();
		
		JMenu mnArrangeGrid = new JMenu("Arrange Grid");
		mnArrangeGrid.setIcon(ImageFunctions.getMenuIcon("/gui/grid.png"));
		viewMenu.add(mnArrangeGrid);
		
		JMenu mnDivisions = new JMenu("Divisions");
		mnArrangeGrid.add(mnDivisions);
		
		JMenu mnLeftSide = new JMenu("Left Side");
		mnDivisions.add(mnLeftSide);
		
		ButtonGroup leftDivisionSelection = new ButtonGroup();
		
		JRadioButtonMenuItem radioButtonMenuItem_5 = new JRadioButtonMenuItem("0");
		mnLeftSide.add(radioButtonMenuItem_5);
		radioButtonMenuItem_5.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.GRID_CHANGE).setValue("L0").setOrigin(e).build()));
		leftDivisionSelection.add(radioButtonMenuItem_5);
		
		JRadioButtonMenuItem radioButtonMenuItem_4 = new JRadioButtonMenuItem("1");
		mnLeftSide.add(radioButtonMenuItem_4);
		radioButtonMenuItem_4.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.GRID_CHANGE).setValue("L1").setOrigin(e).build()));
		leftDivisionSelection.add(radioButtonMenuItem_4);
		
		JRadioButtonMenuItem radioButtonMenuItem_6 = new JRadioButtonMenuItem("2");
		mnLeftSide.add(radioButtonMenuItem_6);
		radioButtonMenuItem_6.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.GRID_CHANGE).setValue("L2").setOrigin(e).build()));
		leftDivisionSelection.add(radioButtonMenuItem_6);
		
		JRadioButtonMenuItem radioButtonMenuItem_7 = new JRadioButtonMenuItem("3");
		mnLeftSide.add(radioButtonMenuItem_7);
		radioButtonMenuItem_7.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.GRID_CHANGE).setValue("L3").setOrigin(e).build()));
		leftDivisionSelection.add(radioButtonMenuItem_7);

        switch (UiConfiguration.getDesktopDivsLeft()){
            case 0:
                radioButtonMenuItem_5.setSelected(true);
                break;
            case 1:
                radioButtonMenuItem_4.setSelected(true);
                break;
            case 2:
                radioButtonMenuItem_6.setSelected(true);
                break;
            case 3:
                radioButtonMenuItem_7.setSelected(true);
                break;
        }
		
		JMenu mnRightSide = new JMenu("Right Side");
		mnDivisions.add(mnRightSide);
		
		ButtonGroup rightDivisionSelection = new ButtonGroup();
		
		JRadioButtonMenuItem radioButtonMenuItem = new JRadioButtonMenuItem("0");
		radioButtonMenuItem.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.GRID_CHANGE).setValue("R0").setOrigin(e).build()));
        mnRightSide.add(radioButtonMenuItem);
        rightDivisionSelection.add(radioButtonMenuItem);
		
		JRadioButtonMenuItem radioButtonMenuItem_1 = new JRadioButtonMenuItem("1");
		mnRightSide.add(radioButtonMenuItem_1);
		radioButtonMenuItem_1.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.GRID_CHANGE).setValue("R1").setOrigin(e).build()));
		rightDivisionSelection.add(radioButtonMenuItem_1);
		
		JRadioButtonMenuItem radioButtonMenuItem_2 = new JRadioButtonMenuItem("2");
		radioButtonMenuItem_2.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.GRID_CHANGE).setValue("R2").setOrigin(e).build()));
		mnRightSide.add(radioButtonMenuItem_2);
		rightDivisionSelection.add(radioButtonMenuItem_2);
		
		JRadioButtonMenuItem radioButtonMenuItem_3 = new JRadioButtonMenuItem("3");
		radioButtonMenuItem_3.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.GRID_CHANGE).setValue("R3").setOrigin(e).build()));
        mnRightSide.add(radioButtonMenuItem_3);
        rightDivisionSelection.add(radioButtonMenuItem_3);

        switch (UiConfiguration.getDesktopDivsRight()){
            case 0:
                radioButtonMenuItem.setSelected(true);
                break;
            case 1:
                radioButtonMenuItem_1.setSelected(true);
                break;
            case 2:
                radioButtonMenuItem_2.setSelected(true);
                break;
            case 3:
                radioButtonMenuItem_3.setSelected(true);
                break;
        }
		
		JMenuItem mntmCetnerLine = new JMenuItem("Center Line");
		mntmCetnerLine.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.DIVIDER_CHANGE).setOrigin(e).build()));
		mnArrangeGrid.add(mntmCetnerLine);
		
		showMenu = new JMenu("Show");
		showMenu.setIcon(ImageFunctions.getMenuIcon("/gui/present.png"));
		viewMenu.add(showMenu);
		
		InternalEventHandler.registerInternalListener((EmbeddedFrameEvent e) -> {
            if (e.getAction().equals(EmbeddedFrameEvent.Action.ADDED)){
                showMenu.add(new FrameShowMenu(e.getComponent()));
            }
            else if (e.getAction().equals(EmbeddedFrameEvent.Action.CLOSED)) {
                for (java.awt.Component menuitem : showMenu.getMenuComponents()) {
                    try {
                        FrameShowMenu menu = (FrameShowMenu) menuitem;
                        if (menu.getFrame() == e.getComponent()) {
                            showMenu.remove(menuitem);
                        }
                    }
                    catch (ClassCastException ex) {}
                }
            }
		});
		
		JMenu mnTools = new JMenu("Tools");
		mnTools.setIcon(ImageFunctions.getMenuIcon("/gui/tools.png"));
		optionsMenu.add(mnTools);
		
		JMenu mnVolume = new JMenu("Volume");
		mnTools.add(mnVolume);
		
		ButtonGroup volumeControls = new ButtonGroup();
		
		JRadioButtonMenuItem mntmHigh = new JRadioButtonMenuItem("High");
		mntmHigh.setIcon(ImageFunctions.getMenuIcon("/gui/speaker_loud.png"));
		mntmHigh.addActionListener((ActionEvent e) -> fireVolumeChange(SoundPlayer.Volume.HIGH));
		mnVolume.add(mntmHigh);
		volumeControls.add(mntmHigh);
		
		JRadioButtonMenuItem mntmLow = new JRadioButtonMenuItem("Low");
		mntmLow.setIcon(ImageFunctions.getMenuIcon("/gui/speaker_quiet.png"));
		mntmLow.addActionListener((ActionEvent e) -> fireVolumeChange(SoundPlayer.Volume.LOW));
		mnVolume.add(mntmLow);
		volumeControls.add(mntmLow);
		
		JRadioButtonMenuItem mntmMute = new JRadioButtonMenuItem("Mute");
		mntmMute.setIcon(ImageFunctions.getMenuIcon("/gui/speaker_mute.png"));
		mntmMute.addActionListener((ActionEvent e) -> fireVolumeChange(SoundPlayer.Volume.MUTE));
		mnVolume.add(mntmMute);
		volumeControls.add(mntmMute);

        switch (UiConfiguration.getVolume()){
            case HIGH:
                mntmHigh.setSelected(true);
                break;
            case LOW:
                mntmLow.setSelected(true);
                break;
            case MUTE:
                mntmMute.setSelected(true);
                break;
        }
		
		optionsMenu.addSeparator();
		
		JMenuItem mntmSettings = new JMenuItem("Settings");
		mntmSettings.setIcon(ImageFunctions.getMenuIcon("/gui/gear_thick.png"));
		mntmSettings.addActionListener((ActionEvent e) -> InternalEventHandler.fireInternalEvent(MenuCommandEvent.builder().setAction(MenuCommandEvent.Action.SETTINGS).setOrigin(e).build()));
		optionsMenu.add(mntmSettings);

        this.add(Box.createHorizontalGlue());
        this.add(Box.createHorizontalGlue());

        JMenu minimizeBtn = new JMenu("__");
        minimizeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SoundPlayer.playSound(SpearsSound.DROPPING);
                ((JFrame)UiFactory.getApplication()).setState(JFrame.ICONIFIED);
            }
        });
        this.add(minimizeBtn);

        JMenu closeBtn = new JMenu("\u2715");
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (exitOp.isPresent()){
                    SoundPlayer.playSound(SpearsSound.GOODBYE);
                    exitOp.get().run();
                }
            }
        });
        this.add(closeBtn);
	}

	void setPlatfroms(Map<String, PlatformEnvironment> enviros, Map<String, List<Platform>> platforms){
		newMenu.initPlatformViewControls(enviros, platforms);
	}

    @Override
    public void setCloseOperation(Runnable exit){
        exitOp = Optional.of(exit);
    }

    @Override
    public void setVolumeListener(VolumeChangeListener listen){
        volumeListener = Optional.of(listen);
        listen.changeVolume(UiConfiguration.getVolume());
    }

    private void fireVolumeChange(SoundPlayer.Volume level){
        UiConfiguration.setVolume(level);
        if (volumeListener.isPresent()){
            volumeListener.get().changeVolume(level);
        }
    }

}
