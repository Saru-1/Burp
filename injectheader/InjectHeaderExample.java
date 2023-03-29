/*
 * Copyright (c) 2022-2023. PortSwigger Ltd. All rights reserved.
 *
 * This code may be used to extend the functionality of Burp Suite Community Edition
 * and Burp Suite Professional, provided that this usage does not violate the
 * license terms for those products.
 */

package example.injectheader;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.TableModel;

import java.awt.*;
import java.awt.event.*;

import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;


//Burp will auto-detect and load any class that extends BurpExtension.
public class InjectHeaderExample implements BurpExtension {
    
    private MontoyaApi api;
    public static String header_name= new String();
    public static String header_value= new String();

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("HTTP header injector Example");
        
        //GUI
        MyTableModel tableModel = new MyTableModel();
        api.userInterface().registerSuiteTab("Header injector", constructLoggerTab(tableModel));
        
        //Register our http handler with Burp.
        api.http().registerHttpHandler(new MyHttpHandler(api,tableModel));
    }
    private Component constructLoggerTab(MyTableModel tableModel)
    {
        // main split pane
        double percent = 0.50;

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // tabs with request/response viewers
        JTabbedPane tabs = new JTabbedPane();

        UserInterface userInterface = api.userInterface();

        HttpRequestEditor requestViewer = userInterface.createHttpRequestEditor(READ_ONLY);
        HttpResponseEditor responseViewer = userInterface.createHttpResponseEditor(READ_ONLY);

        tabs.addTab("Request", requestViewer.uiComponent());
        tabs.addTab("Response", responseViewer.uiComponent());

        splitPane.setRightComponent(tabs);

        // table of log entries
        JTable table = new JTable(tableModel)
        {
            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend)
            {
                // show the log entry for the selected row
                HttpResponseReceived responseReceived = tableModel.get(rowIndex);
                requestViewer.setRequest(responseReceived.initiatingRequest());
                responseViewer.setResponse(responseReceived);

                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };

        JScrollPane scrollPane = new JScrollPane(table);
        
        
        JPanel jp= new JPanel();
        JButton addbutton = new JButton("Add Header");
        
        JTextField hfield = new JTextField(20);
        JTextField vfield = new JTextField(70);
        JLabel jlbl = new JLabel("This is a sample : It is a test");
        
        jp.add(addbutton);
        jp.add(hfield);
        jp.add(vfield);
        jp.add(jlbl);
        jp.add(scrollPane);
        splitPane.setLeftComponent(jp);
        splitPane.setDividerLocation(800);

        addbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                header_name=hfield.getText();
                header_value=vfield.getText();
                jlbl.setText(header_name+": "+header_value);
            }
        });

        return splitPane;
    }
}
