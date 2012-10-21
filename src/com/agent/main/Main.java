package com.agent.main;



import java.util.List;

import com.agent.utility.Map;
import com.agent.utility.Tank;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
public class Main {
	private static List<Tank> tankList=null;
	private static Map map=new Map();
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		new Thread(map).start();
		
		

	}
	public static void startGame(){
		
	

	
	}
}
