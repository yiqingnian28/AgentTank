/**
 * 
 */
package com.agent.utility;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.agent.utility.Tank.Direction;

/** 
 *Class Bullet has several attributes that user cares about, such as 
 *direction, speed and current position in the grid map.
 *@author Qian,Li,Liu
 */
public class Bullet {
	
	public final static Integer IS_NOT_HIT=-1;	
	
	private final Integer BULLET_SPEED=Map.GRID_WIDTH/4;
	
	/**Only effective bullet can cause damage.**/
	private Boolean isEffective=true;
	
	/**The source of this bullet.**/
	private Integer fireTankID=null;
	
	/**Current pixel position of this bullet.**/
	private Point position=null;
	
	/**Current grid position(position in the grid map) of this bullet.**/
	private Point gridPosition=null;
	
	private Integer speed=BULLET_SPEED;	
	private Direction direction=null;
	
	/**True when this bullet hits a tank.**/
	private Boolean isHit=false;
	private final Integer MAX_DRAW_TIME=5;
	
	/**When a bullet hits a tank, it changes to a flame(a circle 
	 * filled by a certain color). And this flameRadius is the 
	 * current radius of the flame**/
	private Integer flameRadius=1;
	
	/****/
	private Integer flameDrawCount=0;
	
	/**The position of this bullet's target in the grid map.**/
	private Point targetPosition=null;
	
	/**
	 * Constructor function
	 * @param d Direction of this bullet
	 * @param gridPos Place where this bullet is first created.
	 * @param tankID Tank that creates this bullet.
	 */
	public Bullet(Direction d,Point gridPos,Integer tankID, Map map) {
		// TODO Auto-generated constructor stub
		fireTankID=tankID;
		direction=d;
		gridPosition=new Point(gridPos);
		position=new Point(
				gridPos.x*Map.GRID_WIDTH+Map.GRID_WIDTH/2,
				gridPos.y*Map.GRID_WIDTH+Map.GRID_WIDTH/2);
		tryToHitTarget(map);
		
	}
	
	/**
	 * Paiting function of bullet. When a bullet is still flying, it is a
	 * bullet.When it hits a tank, however, it turns into a flame.
	 * @param g 
	 */
	public void draw(Graphics g){
		
		//When this bullet doesn't hit any tank
		if(!isHit){
			g.setColor(Color.black);
			switch (direction) {
				//if direction is horizontal, draw a horizontal bullet 
				case LEFT:
				case RIGHT:{
					g.fillRect(position.x-1, position.y-2, 4, 2);
					break;
				}
				//else draw a vertical bullet
				case UP:
				case DOWN:{
					g.fillRect(position.x-2, position.y-1, 2, 4);
					break;
				}
				default:
			}	
		}else{	
			//the bullet hits a tank
			Point p =new Point(targetPosition);
			g.setColor(Color.white);
			
			//draw flame at different part of the tank, according to the
			//direction in which the bullet flies.
			switch (direction) {
			case LEFT:
			{
				p.x+=Map.GRID_WIDTH/2;
				p.y+=Map.GRID_WIDTH/4;
				break;
			}
			case RIGHT:
			{
				p.y+=Map.GRID_WIDTH/4;
				break;
			}
			case UP:
			{
				p.x+=Map.GRID_WIDTH/16*6;
				p.y+=Map.GRID_WIDTH/16*12;
				break;
			}
			case DOWN:
			{
				p.x+=Map.GRID_WIDTH/16*6;
				break;
			}
			default:
		}	
			g.fillArc(p.x, p.y, flameRadius, flameRadius,0,360);
			
		}
	}
	
	/**
	 * Update the data of a bullet
	 * @return false if a bullet should be deleted;true if the bullet is
	 * still alive。
	 */
	public Boolean update() {
		
		if(!isHit){
			//bullet is still flying
			switch (direction) {
			case LEFT:position.x-=speed;break;
			case RIGHT:position.x+=speed;break;
			case UP:position.y-=speed;break;
			case DOWN:position.y+=speed;break;
			default:
			}		
			
			//update the grid position of this bullet
			gridPosition.x=position.x/Map.GRID_WIDTH;
			gridPosition.y=position.y/Map.GRID_WIDTH;
		}else {			
			//bullet hits a tank and changes to a flame.
			if(flameDrawCount++<MAX_DRAW_TIME){
				flameRadius++;
			}
			else{
				flameRadius--;
				if(flameRadius==0)
					return false;
			}
		}
		
		//check if the bullet is out of boundary
		if (position.x>Map.GAME_WIDTH||
				position.x<0||
				position.y>Map.GAME_HEIGHT||
				position.y<0) {
			 return false;			
		}else {
			return true;
		}
	}
	
	
	/**
	 * Check if this bullet hits a tank.
	 * @param map the map in which this bullet lives.
	 * @return id if this bullet hits a tank, id is the id of the 
	 * tank hit by this bullet. return IS_NOT_HIT if no tank is hit.
	 */
	public Integer tryToHitTarget(Map map){				
		//check if the place specified by the grid position of this bullet
		//holds a tank. If it does, get the id of the tank.
		Integer id=map.tankExist(gridPosition);			
		if(id==Map.NO_TANK_EXISTS||id==fireTankID){
			//no effective tank exists. So no tank is hit.
			return IS_NOT_HIT;
		}
		else {	
			//a tank is hit.
			isHit=true;
			Tank t =map.findTankByTankID(id);
			if(isEffective){
				isEffective=false;
				t.hitByBullet();
			}
			
			//get the target tank's grid position so that the flame can be
			//attached to the target
			targetPosition=t.getPosition();
			return id;
		}
	}
	
	public Point getGridPostion(){
		return gridPosition;
	}
}
