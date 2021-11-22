package com.ahut;

public class Main {

	public static void main(String[] args) {
		new ClientActivity("Doomfist");
		new ClientActivity("ana");
		
		MyService myService =new MyService();
		myService.start(); // start thread
		
	}

}
