package com.ahut;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import com.google.gson.Gson;


/***
 * this class is to distribute data
 * 
 * 
 * @author kue84
 *
 */
public class MyService extends Thread{
	
	public static int PORT = 10004;
	private static DatagramSocket socket; // UDP
	//data storage object
	private static ArrayList<ClientActivity> mList = new ArrayList<ClientActivity>();
	public MyService() {
		try {
			socket = new DatagramSocket(PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public static void loginGroups(ClientActivity clientActivity) {
		if(clientActivity == null)
			return;
		mList.add(clientActivity);
	}
	
	private void receiveMessage() {
		byte[] buf = new byte[1024];
		// UDP data packages
		DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
		
		while (true) {
			try {
				// receive data packages	
				socket.receive(datagramPacket);
				String msg = new String(datagramPacket.getData(),0,datagramPacket.getLength());
				
				// use gson to decode msg
				Gson gson = new Gson();
				// use class reflection to decode msg to Messagebean class
				MessageBean bean = gson.fromJson(msg, MessageBean.class);
				
				for(ClientActivity clientActivity:mList) {
					// send data to each client
					clientActivity.pushMessage(bean);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void run() {
		while (true) {
			receiveMessage();
		}
		
	}
	
}
