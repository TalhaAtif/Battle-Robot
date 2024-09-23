package summative;

import becker.robots.City;
import becker.robots.Direction;
import becker.robots.icons.Icon;

import java.math.*;

public class TalhaT300 extends FighterRobot {


	private int health = 0;
	private int myId;

	public TalhaT300(City c, int a, int s, Direction d, int id, int MAX_HEALTH) {
		super(c, a, s, d, id, 1,4,5);
		this.health = MAX_HEALTH;
		myId = this.getID();
		this.setColor(getColor().YELLOW);
		this.setLabel();
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

	private int findDistance(int robotMove, int robotCheck, OppData [] data) {
		int distance = 0;
		if (data[robotCheck].getHealth() > 0) {
			distance = Math.abs(data[robotMove].getAvenue() - data[robotCheck].getAvenue()) + Math.abs(data[robotMove].getStreet() - data[robotCheck].getStreet());		
		}
		else {
			distance = 1000;
		}
		return distance;
	}

	private double trueValue(int id, OppData [] data) {
		double value = 0;
		int health = data[id].getHealth();
		int distance= this.findDistance(this.myId, id, data);
		value += health*.1 + distance*.5;
		return value;
	}


	private OppData[] findBestTarget(OppData [] data) {

		for (int i = 1; i < data.length; i++) {
			OppData temp = data[i];
			double tempVal = trueValue(i, data);

			for (int j=i-1; j>=0; j--) {
				if (trueValue(j, data) > tempVal) {
					data[j+1] = data[j];
				} else {
					data[j+1] = temp;
					break;
				}

				if (j==0) {
					data[0] = temp;
				}
			}
		}
		return data;
	}

	private int findClosestOpp(OppData [] data) {
		int [] distances = new int [data.length];
		int closest = -1;
		int closestAmt = 1000000;
		for (int i = 0; i < data.length; i++) {
			if (i != this.getID()) {
				int distance = findDistance(this.myId, i, data);
				if (distance < closestAmt) {
					closestAmt = distance;
					closest = i;
				}
			}
		}		
		boolean won = true;
		for (int j = 0; j < data.length; j++) {
			if (data[j].getHealth() > 0 && j != this.getID() ) {
				won = false;
			}
		}
		if (won) {
			closest = -1;
		}
		return closest;
	}
	@Override
	public void goToLocation(int a, int s) {
		this.goToAve(a);
		this.goToStreet(s);
	}

	private int [] findMaxMoves(int ave, int street, int steps) {
		int [] moves = new int [3];
		int myAve = this.getAvenue();
		int myStreet = this.getStreet();

		int distAve = Math.abs(ave-this.getAvenue());
		int distStreet = Math.abs(street-this.getStreet());

		int changeA = 0;
		boolean reach = true;
		if (steps < distAve) {
			reach = false;
			changeA = Math.abs(steps-distAve);
			if (ave > myAve) {
				ave-=changeA;
			}
			else {
				ave+=changeA;
			}
		}
		if (steps < (distAve+distStreet)) {
			reach = false;
			int changeS = (Math.abs(steps-(distStreet+distAve)))-changeA;
			if (street < myStreet) {
				street+= changeS;
			}
			else {
				street-= changeS;
			}
		}

		moves[0] = ave;
		moves[1] = street;
		if (reach) {
			moves[2] = 10;
		}
		return moves;

	}
	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {
		data = this.findBestTarget(data);
		OppData myOpp = data[0];
		if (myOpp.getID() == this.getID()) {
			myOpp = data[1];
		}
		int steps = energy/5;
		if (this.getNumMoves() <= steps) {
			steps = this.getNumMoves();
		}
		int endAvenue = this.getAvenue();
		int endStreet = this.getStreet();

		int mySlot = 0;
		for (int i = 0; i<data.length;i++) {
			if (data[i].getID() == this.getID()) {
				mySlot = i;
			}
		}

		this.health = data[mySlot].getHealth();
		int fight = this.findClosestOpp(data);

		if (fight != -1) {
			endAvenue = myOpp.getAvenue();
			endStreet = myOpp.getStreet();

			int [] cords = (this.findMaxMoves(endAvenue, endStreet, steps));
			endAvenue = cords[0];
			endStreet = cords[1];
			if (cords[2] != 10) {
				fight = -1;
			}
			else {
				fight = myOpp.getID();
			}

		}

		return new TurnRequest(endAvenue, endStreet, fight , this.getAttack());
	}

	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		if ( (this.health - healthLost) <= 0 ) {
			this.setColor(getColor().black);
		}
	}
}
