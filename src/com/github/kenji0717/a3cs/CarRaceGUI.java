package com.github.kenji0717.a3cs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector3d;

import jp.sourceforge.acerola3d.a3.*;

class CarRaceGUI extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    CarRaceImpl impl;
    SimpleIDE ide;
    A3Canvas mainCanvas;
    A3SubCanvas carCanvas;
    JTextField carClassTF;
    JButton changeCPB;
    JButton confB;
    JButton ideB;
    JLabel generalInfoL;
    JButton startB;
    JButton pauseB;
    JButton stopB;
    JLabel carEnergyL;
    JTextArea stdOutTA;
    JTextAreaOutputStream out;
    
    CarRaceGUI(CarRaceImpl i,String carClass) {
        super("CarRace");
        impl = i;
        ide = new SimpleIDE(this);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        VBox baseBox = new VBox();
        this.add(baseBox,BorderLayout.CENTER);

        HBox controlBox = new HBox();
        baseBox.myAdd(controlBox,0);
        HBox classNameBox = new HBox();
        controlBox.myAdd(classNameBox,1);
        classNameBox.myAdd(new JLabel("CAR:"),0);
        carClassTF = new JTextField(carClass);
        classNameBox.myAdd(carClassTF,1);
        changeCPB = new JButton("changeCP");
        changeCPB.addActionListener(this);
        classNameBox.myAdd(changeCPB,0);
        VBox controlBox2 = new VBox();
        controlBox.myAdd(controlBox2,0);
        HBox generalInfoBox = new HBox();
        controlBox2.myAdd(generalInfoBox,0);
        confB = new JButton("Conf");
        confB.addActionListener(this);
        generalInfoBox.myAdd(confB,0);
        ideB = new JButton("IDE");
        ideB.addActionListener(this);
        generalInfoBox.myAdd(ideB,0);
        generalInfoL = new JLabel("Time:");
        generalInfoBox.myAdd(generalInfoL,1);
        HBox mainButtonsBox = new HBox();
        controlBox2.myAdd(mainButtonsBox,0);
        startB = new JButton("START");
        startB.addActionListener(this);
        mainButtonsBox.myAdd(startB,1);
        pauseB = new JButton("PAUSE");
        pauseB.addActionListener(this);
        mainButtonsBox.myAdd(pauseB,1);
        stopB = new JButton("STOP");
        stopB.addActionListener(this);
        mainButtonsBox.myAdd(stopB,1);

        HBox displayBox = new HBox();
        baseBox.myAdd(displayBox,1);
        mainCanvas = A3Canvas.createA3Canvas(400,400);
        mainCanvas.setCameraLocImmediately(0.0,150.0,0.0);
        mainCanvas.setCameraLookAtPointImmediately(-50.0,0.0,1.0);
        A3CSController c = new A3CSController(150.0);
        mainCanvas.setA3Controller(c);
        //mainCanvas.setNavigationMode(A3CanvasInterface.NaviMode.SIMPLE,150.0);
        displayBox.myAdd(mainCanvas,1);
        VBox subBox = new VBox();
        displayBox.myAdd(subBox,1);
        HBox carBox = new HBox();
        subBox.myAdd(carBox,1);
        carCanvas = A3SubCanvas.createA3SubCanvas(200,200);
        carBox.myAdd(carCanvas,1);
        VBox carInfoBox = new VBox();
        carBox.myAdd(carInfoBox,0);
        carInfoBox.myAdd(new JLabel("CAR:"),0);
        carEnergyL = new JLabel("Energy: 000");
        carInfoBox.myAdd(carEnergyL,0);
        
        stdOutTA = new JTextArea(10,80);
        stdOutTA.setEditable(false);
        JScrollPane sp = new JScrollPane(stdOutTA);
        sp.setMinimumSize(new Dimension(100,100));
        baseBox.myAdd(sp,0);
        out = new JTextAreaOutputStream(stdOutTA,System.out);
        PrintStream ps = new PrintStream(out,true);
        System.setOut(ps);
        System.setErr(ps);
        //this.pack();
        //this.setVisible(true);
    }

    void setCar(CarBase c) {
        carCanvas.setAvatar(c.car.a3);
        Vector3d lookAt = new Vector3d(0.0,0.0,6.0);
        Vector3d camera = new Vector3d(0.0,3.0,-6.0);
        Vector3d up = new Vector3d(0.0,1.0,0.0);
        carCanvas.setNavigationMode(A3CanvasInterface.NaviMode.CHASE,lookAt,camera,up,10.0);
    }

    void updateCarInfo(CarBase c) {
        carEnergyL.setText("Energy: "+c.energy);
    }
    void updateTime(double t) {
        generalInfoL.setText("Time:"+String.format("%4.2f",t));
    }
    @Override
    public void actionPerformed(ActionEvent ae) {
        Object s = ae.getSource();
        if (s==startB) {
            impl.startBattle();
        } else if (s==pauseB) {
            impl.pauseBattle();
        } else if (s==stopB) {
            impl.stopBattle();
        } else if (s==changeCPB) {
            changeCP();
        } else if (s==confB) {
            conf();
        } else if (s==ideB) {
            ide();
        }
    }
    String getPath(int i) {
        Object[] possibleValues = { "System", "IDE", "JAR" };
        String selectedValue = (String)JOptionPane.showInputDialog(this,
                "Choose one", "Input",
                JOptionPane.INFORMATION_MESSAGE, null,
                possibleValues, possibleValues[i]);
        if (selectedValue==null)
            return null;
        if (selectedValue.equals("System"))
            return "System";
        else if (selectedValue.equals("IDE"))
            return "IDE";
        else {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "JAR File", "jar");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                String url = f.toURI().toString();
                url = "jar:"+url+"!/";
                return url;
            }
            return null;
        }
    }
    void changeCP() {
        int i = 0;
        if (impl.carClasspath.equals("System"))
            i = 0;
        else if (impl.carClasspath.equals("IDE"))
            i = 1;
        else
            i = 2;
        String cp = getPath(i);
        if (cp!=null)
            impl.changeCP(cp);
    }
    void setParamEditable(boolean b) {
        carClassTF.setEditable(b);
        changeCPB.setEnabled(b);
        confB.setEnabled(b);
        ideB.setEnabled(b);
    }
    void conf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            impl.setWorkDir(f.getAbsolutePath());
            impl.setWorkDirURL(f.toURI().toString());
        } else {
            impl.setWorkDirURL(null);
        }
    }
    void ide() {
        ide.popup(impl.workDir);
    }
    void clearTA() {
        stdOutTA.setText("");
    }
    void clearCamera() {
        mainCanvas.setCameraLocImmediately(0.0,150.0,0.0);
        mainCanvas.setCameraLookAtPointImmediately(-50.0,0.0,1.0);
        carCanvas.setCameraLocImmediately(-10,10,0);
        carCanvas.setCameraLookAtPointImmediately(0,0,0);
    }
}
