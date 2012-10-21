package com.agent.utility;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;


import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
/**
 * @author Liu,Qian,Li
 * 
 *
 */
@SuppressWarnings("serial")
public class Tank extends Agent {
	
	public enum Direction{UP,DOWN,LEFT,RIGHT,STOP};
	public static final Integer FRIEND=0;
	public static final Integer ENEMY=1;
	public static final Integer FRIEND_TANK_MAX_HEALTH=10;
	public static final Integer ENEMY_TANK_MAX_HEALTH=4;
	/**current health of the tank**/
	private Integer health=0;
	
	
	/**id of this tank**/
	private Integer tankID=null;
	
	/**map in which tank this lives*/
	private Map map=null;
	
	/**current grid position in pixel of tank*/
	private Point position=null;
	
	/**tank当前在48*32地图中的位置 */
	private Point gridPos=null;
	
	/**tank的在地图中的目标位置 */
	private Point gridDestination=null;
	
	/**tank的速度 */
	private Integer speed=null;
	
	/**tank的移动方向 */
	private Direction direction=Direction.DOWN;
	
	/**the campaign of tank:0=friend,1=enemy**/
	private Integer campain=null;
	
	public Tank(){
		Random random=new Random();
		gridPos=new Point();
		gridPos.x=random.nextInt(48);
		gridPos.y=random.nextInt(36);
		gridDestination=new Point(gridPos);
		
		position=new Point(gridPos.x*Map.GRID_WIDTH,gridPos.y*Map.GRID_WIDTH);
		speed=Map.GRID_WIDTH/8;
	}

	/**
	 * 向d指示的方向移动一格，若移动后会出界则不移动。
	 * @param d 要移动的方向
	 */
	public void moveToDirectionByOneStep(Direction d){
		
		Point currentPos=getGridPosition();
		position.x=gridDestination.x*Map.GRID_WIDTH;
		position.y=gridDestination.y*Map.GRID_WIDTH;
		map.putTankInGrid(this);
		switch (d) {
		case UP:
			gridDestination.y-=1;
			break;
		case DOWN:
			gridDestination.y+=1;
			break;
		case LEFT:
			gridDestination.x-=1;
			break;
		case RIGHT:
			gridDestination.x+=1;
		default:
			break;
		}
		
		if (isOutOfBoundary(gridDestination)) {
			gridDestination=currentPos;
			System.out.println("out of boundary!!!!!");
			return;
		}

		if (!map.checkMove(this)) {
			gridDestination=currentPos;
			return;
		}
		direction=d;
	}
	
	/**
	 * 判断p所指示的位置是否在地图外
	 * @param p
	 * @return
	 */
	public Boolean isOutOfBoundary(Point p){
		if(p.x>=Map.GRID_XNUMBER||p.x<0||p.y>=Map.GRID_YNUMBER||p.y<0){
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * 判断tank目前是否处于p所指示的位置上
	 * @param p
	 * @return
	 */
	public Boolean isAtGridPos(Point p){
		Integer gw=Map.GRID_WIDTH;
		if (position.x==gw*p.x&&position.y==gw*p.y){
			return true;	
		}
		return false;
	}

	/**
	 * 开火
	 * @param d 开火的方向
	 */
	public void fire(Direction d){
		Point point=new Point(getGridPosition());
		switch (d) {
		case UP:
			point.y-=1;
			break;
		case DOWN:
			point.y+=1;
			break;
		case LEFT:
			point.x-=1;
			break;
		case RIGHT:
			point.x+=1;
			break;
		default:
			return;
		}
		map.addBullet(d, point,tankID);
	}
	
	/**
	 * 移动tank
	 */
	public void move(){			
		if (gridDestination.x*Map.GRID_WIDTH-position.x>0) {			
			position.x+=speed;
		}else if (gridDestination.x*Map.GRID_WIDTH-position.x<0) {
			position.x-=speed;
		}else if (gridDestination.y*Map.GRID_WIDTH-position.y>0) {
			position.y+=speed;
		}else if (gridDestination.y*Map.GRID_WIDTH-position.y<0) {
			position.y-=speed;
		}
	}
	
	/**
	 * tank的绘图函数
	 * @param g 绘图设备
	 * 
	 */
	public void draw(Graphics g){
		if(health<=0)
			return;
		if(campain==ENEMY){
			g.setColor(Color.BLACK);
		}else {
			g.setColor(Color.BLUE);
		}
		g.fillArc(position.x, position.y, Map.GRID_WIDTH, Map.GRID_WIDTH,0,360);		
		g.setColor(Color.red);
		g.drawString(health.toString(),position.x, position.y);
		
	}
	
	/**
	 *Hit a tank, which means health point of this tank should decrease by 1. When a 
	 *tank's health point<=0, then this tank is destroyed and proper clean-up will be done 
	 */
	public void hitByBullet() {
		health--;
		if(health<=0){
			map.deleteTank(this);
			doDelete();
			
		}
	}
	
	/**
	 *
	 */
	public void setup(){
	
		map=(Map)getArguments()[0];
		map.addTank(this);
		campain=(Integer)getArguments()[1];
		tankID=(Integer)getArguments()[2];
		
		addBehaviour(new PaintBehavior(this, 125));
		if(campain==ENEMY){
			addBehaviour(new EnemyTankBehavior(this, 1000));
		}else {
			addBehaviour(new FirendTankBehavior(this, 1000));

		}
	}
	
	
	
	public Point getPosition () {
		return position;
	}
	
	public Point getGridDestination () {
		Point point=new Point(gridDestination);	
		if(point.x<0)point.x=0;
		if(point.x>Map.GAME_WIDTH-1)point.x=Map.GAME_WIDTH-1;
		if(point.y<0)point.y=0;
		if(point.y>Map.GAME_HEIGHT-1)point.y=Map.GAME_HEIGHT-1;
		gridDestination=point;
		return gridDestination;
	}
	public void setGridDestination(Point destination) {
		gridDestination=destination;		
	}
	public Point getGridPosition() {
		Point point=new Point(position.x/Map.GRID_WIDTH,position.y/Map.GRID_WIDTH);	
		if(point.x<0)point.x=0;
		if(point.x>Map.GRID_XNUMBER-1)point.x=Map.GRID_XNUMBER-1;
		if(point.y<0)point.y=0;
		if(point.y>Map.GRID_YNUMBER-1)point.y=Map.GRID_YNUMBER-1;
		return point;
	}
	
	/**
	 * this behavior is used to deal with update job for painting 
	 * 
	 *
	 */
	private class PaintBehavior extends TickerBehaviour
	{

		public PaintBehavior(Agent a, long period) {
			super(a, period);
		}
		@Override
		protected void onTick() {			
			move();		
		}		
	}
	
	
	/**
	 * 
	 */
	private abstract class TankBehavior extends TickerBehaviour{
		
		/**target tank at which this tank shoots*/
		protected Tank targetTank=null;
		
		public TankBehavior(Agent a, long period) {
			super(a, period);
		}
		
		/**
		 * 
		 * @return
		 */
		protected Integer getDistanceToTank(Tank tank){
			if(tank==null)
				return Integer.MAX_VALUE;
			Integer distanceX=Math.abs(tank.getGridPosition().x-getGridPosition().x);
			Integer distanceY=Math.abs(tank.getGridPosition().y-getGridPosition().y);			
			Integer distance=(int) Math.sqrt(distanceX*distanceX+distanceY*distanceY);
			return distance;
		}
		
		protected void fireAtTarget(){
			if(targetTank==null)
				return;
			
			Integer x=targetTank.getGridPosition().x-getGridPosition().x;
			Integer y=targetTank.getGridPosition().y-getGridPosition().y;
			Direction dir=null;
			if(Math.abs(x)==0){
				if(y>0){
					dir=Direction.DOWN;
				}else if(y<0) {
					dir=Direction.UP;
				}
			}else if(Math.abs(y)==0){
				if(x>0){
					dir=Direction.RIGHT;
				}else if(x<0){
					dir=Direction.LEFT;
				}
			}
			if(dir!=null)
				fire(dir);
		}
	}
		
	
	/**
	 * This behavior of FirendTank
	 * 
	 *
	 */
	private class FirendTankBehavior extends TankBehavior
	{
		public FirendTankBehavior(Agent a, long period) {
			super(a, period);
			health=FRIEND_TANK_MAX_HEALTH;
		}
		@Override
		protected void onTick() {
			Random random=new Random();		
			int i=random.nextInt(4);
			moveToDirectionByOneStep(Direction.values()[i]);
		//	fire(Direction.DOWN);
					
		}		
	}	
	

	/**
	 * 
	 * 
	 *
	 */
	private class EnemyTankBehavior extends TankBehavior{		
		public EnemyTankBehavior(Agent a, long period) {
			super(a, period);
			health=ENEMY_TANK_MAX_HEALTH;
		}		
		@Override
		protected void onTick() {
			if(targetTank==null||targetTank.health<=0)
				getTarget();		
			pushToTargetAndFire();
		}		
		/**
		 * 
		 * @return
		 */
		private void getTarget() {
			ArrayList<Tank>targetTankList=map.findTanksByCampaign(FRIEND);
			Integer minDistance=Integer.MAX_VALUE;

			for(int i=0;i<targetTankList.size();i++){
				if(targetTankList.get(i).health<=0){
					continue;
				}
				Integer distance=getDistanceToTank(targetTankList.get(i));
				if (distance<minDistance) {
					minDistance=distance;
					targetTank=targetTankList.get(i);
				}
			}	
			System.out.println(getLocalName()+"'s target is : "+targetTank.getLocalName());
		}
		
		/**
		 * 
		 */
		protected void pushToTargetAndFire() {
			if(targetTank==null)
				return;
			Direction d=getDirectionToTarget();
			Integer distance=getDistanceToTank(targetTank);
			if(distance>1)
				moveToDirectionByOneStep(d);			
			else {
				map.putTankInGrid(Tank.this);
			}
			fireAtTarget();
		}
		
		/**
		 * Get the direction in which the tank can move closer to its target.
		 * @return
		 */
		protected Direction getDirectionToTarget(){
			if( targetTank==null)
				return Direction.STOP;
			Direction dir=Direction.STOP;
			Point targetPosition=targetTank.getGridDestination();
			Point myPosition=getGridPosition();
			Integer x=targetPosition.x-myPosition.x;
			Integer y=targetPosition.y-myPosition.y;			
			if(Math.abs(x)-Math.abs(y)>0){
				if(y>0){
					dir=Direction.DOWN;
				}else if(y<0) {
					dir=Direction.UP;
				}else if(x>0){
					dir=Direction.RIGHT;					
				}else{
					dir=Direction.LEFT;
				}
			}else {
				if(x>0){
					dir=Direction.RIGHT;
				}else if(x<0){
					dir=Direction.LEFT;
				}else if(y>0){
					dir=Direction.DOWN;
				}else if(y<0) {
					dir=Direction.UP;
				}
			}
			return dir;
		}
	}
		
}
