package summative;

import becker.robots.City;
import becker.robots.Direction;
import becker.robots.icons.Icon;

import java.awt.Color;
import java.math.*;
/**
 * Program to create a fighter robot
 * that will compete again other robots
 * in an arena
 * @author talha
 * @version June 16 2023
 */
public class TalhaT400Atif extends FighterRobot {


	private int health = 0;
	private int myId;
	private AtifData [] sData; //Class that extends OppData for more info
	private OppData [] dataCopy; //Copy of OppData from the last turn
	private boolean newGame= true; //Is true on first turn
	private boolean attacked = false; //Is true when I am attacked
	private int attacker = -1; //ID of robot that attacked me

	/**
	 * Creates fighter robot
	 * @param City for robot
	 * @param Starting avenue of robot
	 * @param Starting street of robot
	 * @param Starting direction of robot
	 * @param id of robot
	 * @param MAX_HEALTH of robot
	 */
	public TalhaT400Atif(City c, int a, int s, Direction d, int id, int MAX_HEALTH) {
		super(c, a, s, d, id, 5,4,1);
		this.health = MAX_HEALTH;
		myId = this.getID();
		this.setColor(getColor().RED);
		this.setLabel(); //Sets label of robot
	}

	private void lookEast() {
		//Turns robot if it is not facing east
		while (this.getDirection() != Direction.EAST) {
			//If turning right is more efficient, robot will turn right
			if (this.getDirection() == Direction.NORTH) {
				this.turnRight();
			}
			else {
				this.turnLeft();
			}
		}
	}

	/**
	 * Sets label of robot to display current Health and ID
	 */
	public void setLabel() {
		this.setLabel(this.health + " " + this.getID());
	}

	/**
	 * Turns robot west
	 */
	private void lookWest() {
		//Turns robot if it is not facing west
		while (this.getDirection() != Direction.WEST) {
			//If turning right is more efficient, robot will turn right
			if (this.getDirection() == Direction.SOUTH) {
				this.turnRight();
			}
			else {
				this.turnLeft();
			}		
		}		
	}

	/**
	 * Turns robot north
	 */
	private void lookNorth() {
		//Turns robot if it is not facing north
		while (this.getDirection() != Direction.NORTH) {
			//If turning right is more efficient, robot will turn right
			if (this.getDirection() == Direction.WEST) {
				this.turnRight();
			}
			else {
				this.turnLeft();
			}
		}
	}

	/**
	 * Turns robot south
	 */
	private void lookSouth() {
		//Turns robot if it is not facing south
		while (this.getDirection() != Direction.SOUTH) {
			//If turning right is more efficient, robot will turn right
			if (this.getDirection() == Direction.EAST) {
				this.turnRight();
			}
			else {
				this.turnLeft();
			}	
		}
	}

	/**
	 * Method to send robot to avenue
	 * @param ave destination avenue for robot
	 */
	private void goToAve(int ave) {
		//Runs until robot has reached avenues
		while (this.getAvenue() != ave) {
			//If robot is on the left of avenue, moves it right
			if (this.getAvenue() < ave) {
				this.lookEast();
				this.move();
			}
			//If robot is on right of avenues, moves it left
			else {
				this.lookWest();
				this.move();
			}
		}
	}

	/**
	 * Method to send robot to street
	 * @param street destination street for robot
	 */
	private void goToStreet(int street) {
		//Runs until the robot reaches specified street
		while (this.getStreet() != street) {
			//If the robot is above the street it will go south
			if (this.getStreet() < street) {
				this.lookSouth();
				this.move();
			}
			//Otherwise it will travel north
			else {
				this.lookNorth();
				this.move();
			}
		}	
	}
	/**
	 * Method to find distance between two robots
	 * @param robotAve avenue of current robot
	 * @param robotStreet street of current robot
	 * @param robotCheck the robot that the method will calculate the distance to
	 * @param data OppData of all robots
	 * @return interger value of Manhattan distance
	 */
	private int findDistance(int robotAve, int robotStreet, int robotCheck, OppData [] data) {
		int distance = 0;
		//Checks if the robot is alive, otherwise it return a large value
		if (data[robotCheck].getHealth() > 0) {
			distance = Math.abs(robotAve - data[robotCheck].getAvenue()) + Math.abs(robotStreet - data[robotCheck].getStreet());		
		}
		else {
			distance = 1000;
		}
		return distance;
	}

	/**
	 * Method to locate a certain ID in smart data
	 * @param sData SmartData array
	 * @param id being searched for
	 * @return index value of ID in sData
	 */
	private int sDataLocation(AtifData [] sData, int id) {
		int location = 0;
		//Checks through sData until ID is found
		for (int i = 0; i < sData.length; i++) {
			if (sData[i].getID() == id) {
				location = i;
				break;
			}
		}
		return location;
	}

	/**
	 * Method to calculate weighted value of a certain robot
	 * @param id of robot being checked
	 * @param data array of OppData
	 * @param sData array of sData
	 * @return double value of robot risk, lower value means better target
	 */
	private double trueValue(int id, OppData [] data, AtifData [] sData) {
		double value = 0;
		int health = data[id].getHealth();
		int distance= this.findDistance(this.getAvenue(), this.getStreet(), id, data); //Finds distance between my robot and target robot
		AtifData opp = sData[sDataLocation(sData, id)]; //Finds position of target in sData
		value += health*.1 + distance*.5+ (opp.getMoves()*3+opp.getAttack()+opp.getDefence())*(0.5); //Weights out different factors
		return value;
	}

	/**
	 * Method to sort OppData from best to worst target
	 * @param data OppData array
	 * @return newly sorted OppData array
	 */
	private OppData[] findBestTarget(OppData [] data) {

		//For every element in data
		for (int i = 1; i < data.length; i++) {
			OppData temp = data[i]; //Temp is object in opp Data
			double tempVal = trueValue(i, data, this.sData); //Value of temp as a double
			if (data[i].getHealth() <= 0) { //If robot is dead, arbritary large value is assigned
				tempVal = 10000;
			}

			//For all elements from 1-end of data array
			for (int j=i-1; j>=0; j--) {
				//If true value of object is greater than value of data[i]
				if (trueValue(j, data,this.sData) > tempVal) {
					//Moves object 
					data[j+1] = data[j];
				} else {
					data[j+1] = temp;
					break;
				}

				//Once whole array has been searched and did not break, first object is temp
				if (j==0) {
					data[0] = temp;
				}
			}
		}
		return data; //Return array of OppData
	}


	/**
	 * Distance between coordinates
	 * @param avenue 1
	 * @param avenue 2
	 * @param street 1
	 * @param street 2
	 * @return interger value of distance
	 */
	private int distanceToSpot(int ave1, int ave2, int street1, int street2) {
		int distance = (Math.abs(ave2-ave1) + Math.abs(street2-street1));
		return distance;
	}

	@Override
	/**
	 * Method to send robot to certain location
	 */
	public void goToLocation(int a, int s) {
		this.goToAve(a);
		this.goToStreet(s);
	}

	/**
	 * Method for robot to run away if it is at risk of dying
	 * @param data OppData array
	 * @param myID my robots ID
	 * @param steps my robots maximum steps allowed
	 * @param energy my robots current energy
	 * @return avenue and street in integer array
	 */
	private int[] runAway(OppData[] data, int myID, int steps, int energy) {
		int[] cords = new int[2];
		int closestRobotID = -1;
		int closestRobotDistance = 1000; // Set closest distance to arbitrary large value

		// Find the closest robot that is not the current robot
		for (int i = 0; i < data.length; i++) {
			if (i != myID && data[i].getHealth() > 0) {
				int distance = findDistance(this.getAvenue(), this.getStreet(), i, data);

				// Keeps lowest distance and robot that is closest stored in variables
				if (distance < closestRobotDistance) {
					closestRobotDistance = distance;
					closestRobotID = i;
				}
			}
		}


		// Checks if closest robot is found within 3 spots away
		if (closestRobotID != -1 && closestRobotDistance <= 3) {
			// Robot will not move if it has low energy
			if (energy <= 15) {
				cords[0] = this.getAvenue();
				cords[1] = this.getStreet();
			} else {
				// Robot will run away from the closest robot based on the current position
				if (this.getAvenue() >= 10) { //If it is on right side of map
					cords[0] = this.getAvenue() + steps;
					if (cords[0] >= 20) { //Goes toward right wall
						cords[0] = 19;
					}
				} else if (this.getAvenue() <= 9) { //If robot is on left side of map
					cords[0] = this.getAvenue() - steps;
					if (cords[0] < 0) {//Goes toward left wall
						cords[0] = 0;
					}
				} else {
					// If the robot is at a wall, will move up or down
					cords[0] = this.getAvenue();
					if (this.getStreet() >= 6) { //If robot is closer to bottom wall, moves down
						cords[1] = this.getStreet() + steps;
						if (cords[1] > 11) {
							cords[1] = 11;
						}
					} else if (this.getStreet() <= 5) { //if robot is closer to upper wall, moves up
						cords[1] = this.getStreet() - steps;
						if (cords[1] < 0) {
							cords[1] = 0;
						}
					} else if (this.getStreet() == 0) {
						// If the robot is next to the top wall, move towards the bottom
						cords[1] = this.getStreet() + steps;
						if (cords[1] > 11) {
							cords[1] = 11;
						}
					}
					else {
						// If the robot is next to the bottom wall, move towards the top
						cords[1] = this.getStreet() - steps;
						if (cords[1] < 0) {
							cords[1] = 0;
						}
					}
				}
			}
		} else {
			// Stay in the current position if no closest robot is found or it's far away
			cords[0] = this.getAvenue();
			cords[1] = this.getStreet();
		}

		return cords;
	}

	/**
	 * Finds the amount of moves all the robots moved in the previous turn
	 * @param data array of OppData
	 * @param dataCopy array of OppData from last turn
	 * @param sData array of SmartData
	 */
	private void calcNumMoves(OppData [] data, OppData [] dataCopy, AtifData [] sData) {
		int moves = 0;
		for (int i =0; i < sData.length; i++) {
			moves = this.distanceToSpot(data[i].getAvenue(), dataCopy[i].getAvenue(), data[i].getStreet(), dataCopy[i].getStreet());
			if (sData[i].getMoves() <= moves) { //If the moves the robot did in the last turn are greater then the current value, value is saved
				sData[i].setMoves(moves);
			}
		}

	}

	/**
	 * Method to find maximum distance robot can travel
	 * @param ave requested final avenue
	 * @param street requested final street
	 * @param steps maximum steps the robot can take
	 * @return farthest coordinates possible
	 */
	private int [] findMaxMoves(int ave, int street, int steps) {
		int [] moves = new int [3]; //Creates array
		int myAve = this.getAvenue();
		int myStreet = this.getStreet();
		//Finds distance to target
		int distAve = Math.abs(ave-this.getAvenue());
		int distStreet = Math.abs(street-this.getStreet());

		int changeA = 0;
		boolean reach = true; //Checks if robot will reach target
		if (steps < distAve) { //Checks if robot can reach taget avenue
			reach = false;
			changeA = Math.abs(steps-distAve);
			if (ave > myAve) { //Finds farthest avenue that can be reached
				ave-=changeA;
			}
			else {
				ave+=changeA;
			}
		}
		if (steps < (distAve+distStreet)) { //If can reach avenue, checks if robot can reach street
			reach = false; //Since robot cannot reach street, reach is false
			int changeS = (Math.abs(steps-(distStreet+distAve)))-changeA;
			if (street < myStreet) { //Sets street to farthest street robot can reach
				street+= changeS;
			}
			else {
				street-= changeS;
			}
		}

		moves[0] = ave; //Sets coordinates
		moves[1] = street;
		if (reach) {
			moves[2] = 10; //If robot can reach, third element in cord array is 10
		}
		return moves;

	}


	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {
		if (newGame) { //On first turn, creates SmartData array and copies OppData array
			dataCopy = new OppData[data.length];
			this.sData = new AtifData [data.length];
			for (int i = 0; i<sData.length; i++) {
				sData[i] = new AtifData(data[i].getID(), data[i].getAvenue(), data[i].getStreet(), data[i].getHealth());
			}
			System.arraycopy(data, 0, dataCopy, 0, data.length);
			newGame = false;
		} else {
			calcNumMoves(data, dataCopy, sData); //updates movement values for all robots
			System.arraycopy(data, 0, dataCopy, 0, data.length);
		}
		//Sets up variables
		int endAvenue = 0;
		int endStreet = 0;
		int fight = -1;

		//initializes coordinates list
		int [] cords = new int [2];

		//Finds maximum moves
		int steps = energy/5;
		if (this.getNumMoves() <= steps) {
			steps = this.getNumMoves();
		}
		//Robot does not run away by default
		boolean runAway = false;
		//If robot is on low health, low energy, or very high health, it will run away
		if (data[this.getID()].getHealth() < 40 || energy < 20 || data[this.getID()].getHealth() > 95) {
			cords = this.runAway(data, this.getID(), steps, energy);
			runAway = true;
			//Robot will not fight anyone
			fight = -1;
		}

		//Double checks the cords can be reached
		cords = this.findMaxMoves(cords[0], cords[1], steps);
		endAvenue = cords[0];
		endStreet = cords[1];

		//finds my robots position in data array
		int mySlot = 0;
		for (int i = 0; i<data.length;i++) {
			if (data[i].getID() == this.getID()) {
				mySlot = i;
				break;
			}
		}
		this.health = data[mySlot].getHealth();
		if (this.health <= 0) {
			this.setColor(getColor().black); //If robot is dead, will turn black
		}

		if (attacked) { //If robot was attacked last round, it will fight back no matter what
			runAway = false;
		}
		if (!runAway || cords[2] != -1) { //If robot is not running away and has a target
			data= findBestTarget(data); //Sorts data array
			OppData myOpp = data[0];
			if (this.getID() == data[0].getID()) {
				myOpp = data[1];
			} //Finds lowest value robot thats not me
			if (attacked) { //If robot that attacked me has over 20 health more then me, will not attack
				for (int i = 0; i <data.length; i++) {
					if (data[i].getID() == this.attacker) {
						myOpp = data[i];
						break;							
					}

				}
			}

			endAvenue = this.getAvenue();
			endStreet = this.getStreet();

			
			fight = myOpp.getID(); //Sets robot target ID
			endAvenue = myOpp.getAvenue();
			endStreet = myOpp.getStreet();


			cords = (this.findMaxMoves(endAvenue, endStreet, steps)); //Checks farthest cords that robot can go
			endAvenue = cords[0];
			endStreet = cords[1];
			if (myOpp.getStreet() != cords[1]) { //If robot will not reach opponent, robot will not fight
				fight = -1;
			}
			else if (myOpp.getAvenue() != cords[0]) {
				fight = -1;
			}
			else if (myOpp.getHealth() <= 0) { //If my enemy died, robot will not attack anyone
				fight = -1;
			}

			int attack = this.getAttack();
			if (energy <= 0) { //If robot has no energy, it will not fight
				fight = -1;
			}
		}

		int attack = this.getAttack();
		if (energy < 16) { //If energy is very low, will not attack at full power
			attack = 3; 
		}
		this.attacked= false;
		this.attacker= -1; //resets attacked variables
		return new TurnRequest(endAvenue, endStreet, fight , attack);
	}

	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		int defence = 0;
		if ( (this.health - healthLost) <= 0 ) { //If robot is dead, turns black
			this.setColor(getColor().black);
		}
		
		//If it is past the first turn and the robot fought someone
		if (!newGame && oppID != -1) {
			if (healthLost >= 0 ) { //If it was the one who got attacked
				this.attacked = true;
				this.attacker = oppID; //Robot will attack back next turn
				if (sData[oppID].getAttack() < numRoundsFought) { //If rounds fought is higher than current stored value
					sData[oppID].setAttack(numRoundsFought); //Set new highest value
				}
			}
			defence = (10 - sData[oppID].getAttack() - sData[oppID].getMoves());
			if (defence > 6) { //Defense is guessed, if it is higher then 6, becomes 6
				defence = 6;
			}
			sData[oppID].setDefence(defence); //Sets defense value
		}
	}
}
