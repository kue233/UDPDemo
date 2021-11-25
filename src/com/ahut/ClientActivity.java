package com.ahut;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Target;
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
	
	// audio msg
	private AudioFormat format;
	private TargetDataLine targetDataLine; // file
	private boolean isStartAudio = true; // is starting recoding?
	
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
		
		// recording button
		JButton soundRecordBtn = new JButton("click record");
		soundRecordBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isStartAudio) {
					// capture audio file
					captureAudio();
					soundRecordBtn.setText("click end");
				}else {
					targetDataLine.stop(); // stop record
					targetDataLine.close(); // delete record file
					soundRecordBtn.setText("click record");
					sendMessage("send you a voice message, click to play", 1);
					playBtn.setVisible(true);
					
				}
				isStartAudio=!isStartAudio;
			}
		});
		soundRecordBtn.setBounds(10, 334, 133, 23);
		getContentPane().add(soundRecordBtn);
		
		
		// play button
		playBtn = new JButton("click play");
		playBtn.setVisible(false);
		playBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playAudio();
				playBtn.setText("playing record...");
			}
		});
		playBtn.setBounds(379, 333, 133, 23);
		getContentPane().add(playBtn);
		
		textArea = new JTextArea();
		textArea.setBounds(10, 11, 502, 305);
		getContentPane().add(textArea);
		
	}
	
	// receive a message bean, show in txt panel
	public void pushMessage(MessageBean bean) {
		textArea.append(bean.getName()+ ":" + bean.getContent()+"\n");
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
			e.printStackTrace();
		}
	
	}
	
	
	private void captureAudio() {
		format = getAudioFormat();
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			new CaptureThread().start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		
	}
	
	// identity audio file parameters
	private AudioFormat getAudioFormat() {
		float sampleRate = 11025.0f; // sample num per sec, 8000 10000 16000 22050 44100
		int sampleSizeInBits = 16;
		int channels = 1; // 
		boolean signed = true; // is the audio file with sign (bit)
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		
	}
	
	class CaptureThread extends Thread{
		@Override
		public void run() {
			AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE; // audio file save format
			File audioFile = new File("chat.wav");
			try {
				targetDataLine.open(format);
				targetDataLine.start();
				AudioSystem.write(new AudioInputStream(targetDataLine), fileType, audioFile);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void playAudio() {
		AudioInputStream as;
		try {
			as = AudioSystem.getAudioInputStream(new File("chat.wav"));
			AudioFormat format = as.getFormat();
			SourceDataLine sdl = null;
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			sdl = (SourceDataLine) AudioSystem.getLine(info);
			sdl.open(format);
			sdl.start();
			int nByteRead = 0;
			byte[] abData = new byte[512];
			while (nByteRead != -1) {
				// start to play
				nByteRead = as.read(abData,0,abData.length);
				if(nByteRead>=0) {
					sdl.write(abData, 0, nByteRead);
				}	
			}
			sdl.drain(); // empty data line queue
			sdl.close();
			//playBtn.setVisible(false);
			playBtn = new JButton("click play");
			
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}
}
