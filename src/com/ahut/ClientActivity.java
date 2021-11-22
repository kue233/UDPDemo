package com.ahut;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import com.google.gson.Gson;

import javax.swing.JTextArea;

public class ClientActivity extends JFrame{
	
	private String name;
	private JTextField textField;
	private DatagramSocket socket;
	private InetAddress ip;
	private JTextArea textArea; // big text display area
	private JButton playBtn;
	
	public ClientActivity(String name) {
		super("Chat Room: " + name); // set title
		this.name = name;
		setSize(538,440); // set size
		getContentPane().setLayout(null); // window can be drag to resize
		
		
		initLayout();
		initUdp();
		show();
	}
	
	private void initUdp() {
		MyService.loginGroups(this); // login this client
		try {
			socket = new DatagramSocket();
			ip = InetAddress.getByName("127.0.0.1");
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void initLayout() {
		JButton sendBtn = new JButton("send");
		sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = textField.getText();
				sendMessage(msg, 0);
			}
		});
		sendBtn.setBounds(423, 367, 89, 23);
		getContentPane().add(sendBtn);
		
		textField = new JTextField();
		textField.setBounds(10, 368, 403, 20);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		
		JButton soundRecordBtn = new JButton("click record");
		soundRecordBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		soundRecordBtn.setBounds(10, 334, 89, 23);
		getContentPane().add(soundRecordBtn);
		
		playBtn = new JButton("click play");
		playBtn.setBounds(423, 333, 89, 23);
		getContentPane().add(playBtn);
		
		textArea = new JTextArea();
		textArea.setBounds(10, 11, 502, 305);
		getContentPane().add(textArea);
		
	}
	
	// receive a message bean, show in txt panel
	public void pushMessage(MessageBean bean) {
		textArea.append(bean.getName()+ ":" + bean.getContent());
		if(bean.getType() == 0) { // string msg
			// if it is a string msg, disappear play btn
			playBtn.setVisible(false);
		}else {
			playBtn.setVisible(true);
		}
	}
	
	private void sendMessage(String msg, int type) {
		// init a new bean and contents
		MessageBean bean = new MessageBean();
		bean.setName(name);
		bean.setContent(msg);
		bean.setType(type);
		// turn MessageBean to a String Json
		Gson gson = new Gson(); 
		
		String json = gson.toJson(bean);
		byte[] bytes = json.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length,ip, MyService.PORT);
		try {
			socket.send(datagramPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
