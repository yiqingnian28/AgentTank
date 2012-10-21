package com.agent.utility;




import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.awt.Color;


import java.awt.FlowLayout;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import com.agent.utility.Tank.Direction;


/**
 * This map is where all elements, including tank and bullet, live. And it 
 * is a grid map with 48*36 grid.
 * @author Li,Qian,Liu
 * 
 *
 */
public class Map extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 1L;
	
	/**Determines how wide the grid is in pixels**/
	public final static Integer GRID_WIDTH=16;	
	
	/**The number of grids in X-axis**/
	public final static Integer GRID_XNUMBER=48;
	
	/**The number of grids in Y-axis**/
	public final static Integer GRID_YNUMBER=36;
	
	/**The map size in pixels**/
	public final static Integer GAME_HEIGHT=GRID_YNUMBER*GRID_WIDTH;
	public final static Integer GAME_WIDTH=GRID_XNUMBER*GRID_WIDTH;	
	
	/****/
	public final static Integer GRID_AVAILABLE=-1;
	public final static Integer GRID_COLLISION=-2;
	public final static Integer NO_TANK_EXISTS=-1;
	private Color backgroundColor=Color.GREEN;
	private Integer globalTankID=0;
	
	/**List of all tanks**/
	private static List<Tank> tankList=new ArrayList<Tank>();
	
	/**List of all bullets**/
	private static List<Bullet> bulletList=new LinkedList<Bullet>();
		
	private Integer[][] mapGrid=new Integer[GRID_XNUMBER][GRID_YNUMBER];
	private Image bufImg = null;
	private BoxLayout boxLayout=null;
	private JPanel mainJPanel=null;
	private JButton startButton=null;
	private JButton stopButton=null;
	private ButtonListener buttonListener=null;
	private JTextField enemyJTextField=new JTextField(2);
	private JTextField friendJTextField=new JTextField(2);
	
	/**
	 * Constructor. Initialize UI.
	 */
	public Map(){
		
		resetMap();
		mainJPanel=new JPanel();
		add(mainJPanel);	
		boxLayout=new BoxLayout(mainJPanel, BoxLayout.Y_AXIS);
		mainJPanel.setLayout(boxLayout);
	
		JPanel southPanel = new JPanel();	
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setBorder(new LineBorder(Color.blue));
		
		//first row of south panel
		JPanel rowJPanel1=new JPanel();
		southPanel.add(rowJPanel1);				
		rowJPanel1.setLayout(new FlowLayout());
		rowJPanel1.add(new JLabel("Friend Tank Number"));
		rowJPanel1.add(friendJTextField);
		
		//second row of south panel
		JPanel rowJPanel2=new JPanel();
		southPanel.add(rowJPanel2);
		rowJPanel2.setLayout(new FlowLayout());
		rowJPanel2.add(new JLabel("Enemy Tank Number"));
		rowJPanel2.add(enemyJTextField);
				
		//third row of south panel
		JPanel rowJPanel3=new JPanel();
		southPanel.add(rowJPanel3);
		rowJPanel3.setLayout(new FlowLayout());
		buttonListener=new ButtonListener();
		startButton=new JButton("START");
		startButton.addActionListener(buttonListener);
		stopButton=new JButton("STOP");
		stopButton.setEnabled(false);
		stopButton.addActionListener(buttonListener);		
		rowJPanel3.add(startButton);
		rowJPanel3.add(stopButton);
		JPanel northPanel=new JPanel();
		northPanel.setBackground(Color.red);
		northPanel.add(new DrawTank());	
		mainJPanel.add(northPanel);		
		mainJPanel.add(southPanel);
		
		friendJTextField.setText("3");
		enemyJTextField.setText("3");
		this.setTitle("Agent Tank");
		this.setLocation(30,30);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setBackground(backgroundColor);
		
		
	}
	
	/**
	 * Set each grid of this map to GRID_AVAILABLE
	 */
	public void resetMap(){
		for (int i=0;i<GRID_XNUMBER;i++){
			for(int j=0;j<GRID_YNUMBER;j++){				
				mapGrid[i][j]=GRID_AVAILABLE;
			}			
		}
	}
	
	/**
	 * draw the game scene
	 */
	public void run() {
		pack();
		setVisible(true);
		while(true){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			repaint();
		}
	}
	
	/**
	 *  Add a new tank agent.
	 */
	public void addTank(Tank t){
		tankList.add(t);
	}
	
	/**
	 * add a bullet to the bullet list
	 * @param d direction
	 * @param pos initial position
	 * @param id id of the firing tank 
	 */
	public void addBullet(Direction d,Point pos,Integer id){
		bulletList.add(new Bullet(d, pos,id,this));
	}
	
	/**
	 * Check if the move of a tank is appropriate
	 * @param tank the tank that make this move
	 * @return true if the move is ok
	 */
	public synchronized Boolean checkMove(Tank tank) {
		Boolean moveIsOk=null;
		Point destGrid=tank.getGridDestination();
		
		//target grid is not available
		if(mapGrid[destGrid.x][destGrid.y]!=GRID_AVAILABLE){
			System.out.println("collision in grid"+tank.getGridDestination());
			
			//find id of the tank in this grid and set its target gird back
			Tank collisionTank=findTankByTankID(mapGrid[destGrid.x][destGrid.y]);			
			Point cPosition=collisionTank.getGridPosition();
			collisionTank.setGridDestination(cPosition);
			
			//set the original grid of the collision tank to the collisiont tank's id
			mapGrid[cPosition.x][cPosition.y]=(Integer)(collisionTank.getArguments()[2]);			
			mapGrid[destGrid.x][destGrid.y]=GRID_AVAILABLE;
			
			//set this tank back
			Point curPoint=tank.getGridPosition();
			tank.setGridDestination(tank.getGridPosition());			
			mapGrid[curPoint.x][curPoint.y]=(Integer)(collisionTank.getArguments()[2]);
			
			moveIsOk=false;
		}
		
		//target grid is available
		else {
			Point gridPos=tank.getGridPosition();
			mapGrid[destGrid.x][destGrid.y]=(Integer)(tank.getArguments()[2]);
			mapGrid[gridPos.x][gridPos.y]=GRID_AVAILABLE;
			moveIsOk=true;
		}
		
		return moveIsOk;
	}
	
	
	/**
	 * 
	 * @param i 
	 * @return Return tank with id=i, null if no tank was found.
	 */
	public Tank findTankByTankID(Integer i){
		for (Tank tank : tankList) {
			if ((Integer)(tank.getArguments()[2])==i) {
				return tank;
			}
		}
		return null;		
	}
	
	/**
	 * Find a list of tank in the campaign specified by the parameter.
	 * @param campaign Tank.ENEMY or Tank.FRIEND
	 * @return Return a list with size>=0
	 */
	public ArrayList<Tank> findTanksByCampaign(Integer campaign){
		ArrayList<Tank> tList=new ArrayList<Tank>();
		for (Tank tank : tankList) {
			if ((Integer)(tank.getArguments()[1])==campaign) {
				tList.add(tank);
			}
		}
		return tList;
	}
	
	/**
	 * Move out this tank from map. Note that this tank is not cleared.
	 */
	public void deleteTank(Tank t) {
		Point p=t.getGridDestination();
		mapGrid[p.x][p.y]=GRID_AVAILABLE;
	}
	
	/**
	 * 
	 */
	public void update(Graphics g){
		
			
		if (bufImg == null)  
            // 创建一个虚拟屏幕（图片）  
            bufImg = this.createImage(GAME_WIDTH, GAME_HEIGHT);  
        // 得到虚拟屏幕的图像类  
        Graphics gBufImg = bufImg.getGraphics();  
        // 保存虚拟屏幕的画笔颜色  
        Color c = gBufImg.getColor();  
        // 重绘背景  
        gBufImg.setColor(backgroundColor);  
        gBufImg.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);  
        // 恢复虚拟屏幕的画笔颜色  
        gBufImg.setColor(c);  
        // 调用paint方法在虚拟屏幕上绘制图形  
        paint(gBufImg);  
        // 将虚拟屏幕贴到屏幕  
        g.drawImage(bufImg, 0, 0, null);  
        
        
	}
	
	/**
	 * Check if there is a tank where the Point p specifies in the map.
	 * @param p
	 * @return the id of tank. Return NO_TANK_EXISTS if no tank is 
	 * at the grid refers to the given grid
	 */
	public Integer tankExist(Point p){
		if(p.x>=Map.GRID_XNUMBER||p.x<0)
			return NO_TANK_EXISTS;
		if(p.y>=Map.GRID_YNUMBER||p.y<0)
			return NO_TANK_EXISTS;
		if (mapGrid[p.x][p.y]>=0) {
			return mapGrid[p.x][p.y];
		}
		return NO_TANK_EXISTS;
	}
	
	
	/**
	 * 
	 */
	public void putTankInGrid(Tank tank){
		Point dest=tank.getGridDestination();
		mapGrid[dest.x][dest.y]=(Integer) (tank.getArguments()[2]);
	}
	
	/**
	 * Used to start the game.
	 * @author lenovo
	 *
	 */
	class ButtonListener implements ActionListener{
		/**Runtime used to create agent container dynamically**/
		private Runtime runtime=null;
		
		/**ContainerController can create agents**/
		private ContainerController cc=null;
		@Override
		public void actionPerformed(ActionEvent e) { 		
			if(((JButton)e.getSource()).getText()=="START"){
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				runtime = Runtime.instance();
				runtime.setCloseVM(true);
		        cc =  runtime.createMainContainer(new ProfileImpl(false));      
		        
		        try {
		            // create enemy tank agent and start it
		        	for(int i=0;i<Integer.parseInt(enemyJTextField.getText());i++){
		        		int id=globalTankID++;
		        		Object[] enemyObjects={Map.this,Tank.ENEMY,id}; 
		        		System.out.println(globalTankID);
		        		(cc.createNewAgent("enemy"+i, "com.agent.utility.Tank", enemyObjects)).start();
		        	}	
		        	for(int i=0;i<Integer.parseInt(friendJTextField.getText());i++){
		        		int id=globalTankID++;
		        		Object[] enemyObjects={Map.this,Tank.FRIEND,id}; 
		        		System.out.println(globalTankID);
		        		(cc.createNewAgent("firend"+i, "com.agent.utility.Tank", enemyObjects)).start();
		        	}
		        } catch (Exception exception){}				
			}
			else{
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
				try {
					cc.kill();
				} catch (StaleProxyException e1) {
					e1.printStackTrace();
				}
			}
		
		}
	}
	
	/**
	 * component used to draw game scenes
	 * @author lenovo
	 *
	 */
	@SuppressWarnings("serial")
	class DrawTank extends JPanel{
		public DrawTank(){
			Dimension size = new Dimension(GAME_WIDTH,GAME_HEIGHT);
			setMaximumSize(size);
	        setPreferredSize(size);
	        setMinimumSize(size);					
			setSize(Map.GAME_WIDTH,Map.GAME_HEIGHT);
			
			setBackground(Color.GREEN);
		}
		/**
		 * override function that is used to draw the game scenes
		 */
		public void paintComponent(Graphics g) {			
			super.paintComponent(g);
			/////////////
			for (int i=0;i<GRID_XNUMBER;i++){
				for(int j=0;j<GRID_YNUMBER;j++){
					if(mapGrid[i][j]>=0){
						g.setColor(Color.yellow);
						
					}else {
						g.setColor(Color.green);
					}
					g.fillRect(GRID_WIDTH*i, GRID_WIDTH*j, GRID_WIDTH, GRID_WIDTH);
				}
			}
			/////////////
			for(int i=0;i<tankList.size();i++){
				Tank t =tankList.get(i);
				t.draw(g);
			}			
			for (int i=0 ;i<bulletList.size();i++) {
				bulletList.get(i).draw(g);
				Bullet bullet=bulletList.get(i);
				bullet.tryToHitTarget(Map.this);
				if(!bullet.update()){
					bulletList.remove(i);
				}
			}				
		}
	}
	
	
}
