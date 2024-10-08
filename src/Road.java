import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Road {
	
	private static HashMap<char[], Road> roadMap = new HashMap<char[], Road>();
	protected static int yAccumulativePosition = 0;
	protected static int xAccumulativePosition = 0;
	
	protected char[] roadID = new char[4]; 	//The number for each street or avenue
	/*
	 * first char of the road ID 1=entrance, 2=exit, 3=street, 4=avenue
	 */
	protected char roadType;		//S=street, A=avenue
	protected char roadDir;		//direction of source of traffic
	//protected int accumulativePosition;
	protected int[] sectors = new int[3];
	//These two are essentially used to show the start and end points of a road.
	protected TrafficPoint entrancePoint = null;
	protected TrafficPoint exitPoint = null;
	//needed for car movement default Forward=2, Turning=1
	protected int numberOfForwardLanes = 2;
	protected int numberOfTurningLanes = 1;
	
	public static boolean addRoads(int number, int MinBlockSide, 
			int MaxBlockSide, char type) {
		if(number == 0 || MinBlockSide <= 0 || MaxBlockSide <= 0 || 
				(type != 'S' || type != 'A')) {
			int accPos = 0;
			if(type == 'S')
				accPos = yAccumulativePosition;
			else if(type == 'A')
				accPos = xAccumulativePosition;
			char dir = 'D';
			Road tempRoad;
			for(int i=1; i<=number; i++) {
				accPos += (int) (MinBlockSide+(MaxBlockSide-MinBlockSide)*Math.random());
				if(type == 'S')
					dir = (i%2==0)? 'E':'W';
				else if(type == 'A')
					dir = (i%2==0)? 'N':'S';
				tempRoad = new Road(i, dir, type, accPos);
				//middle coordinates for all lanes
				tempRoad.sectors[0] = accPos - 1 - Frame.carWidth - Frame.Clearance;
				tempRoad.sectors[1] = accPos;
				tempRoad.sectors[2] = accPos + 1 + Frame.carWidth + Frame.Clearance;
				roadMap.put(tempRoad.roadID, tempRoad);
			}
			accPos += (int) (MinBlockSide+(MaxBlockSide-MinBlockSide)*Math.random());
			if(type == 'S')
				yAccumulativePosition = accPos;
			else if(type == 'A')
				xAccumulativePosition = accPos;
			return true;
		} else {
			System.out.println("Wrong passed parameters to addRoads() funtion");
			return false;
		}
		
	}
	
	private Road(int ID, char roadDir, char type, int accPos) {
		if(type == 'S')
			this.roadID = Arrays.copyOfRange(getStreetID(ID), 0, 4);
		else if(type == 'A')
			this.roadID = Arrays.copyOfRange(getAvenueID(ID), 0, 4);
		this.roadType = type;
		this.roadDir = roadDir;
		//this.accumulativePosition = accPos;
	}
	
	public static Set<Map.Entry<char[] ,Road>> getEntrySet() {
		return roadMap.entrySet();
	}
	public static Set<char[]> getKeySet() {
		return roadMap.keySet();
	}
	public static Road getRoad(char[] ID) {
		return roadMap.get(ID);
	}
	
	public boolean setEntrancePoints(TrafficPoint entrancePoint) {
		if(this.entrancePoint != null)
			return false;
		else {
			this.entrancePoint = entrancePoint;
		}
		return true;
	}
	public boolean setExitPoints(TrafficPoint exitPoint) {
		if(this.exitPoint != null)
			return false;
		else {
			this.exitPoint = exitPoint;
		}
		return true;
	}
	
	private static char[] getStreetID(int i) {
		char[] roadIDS = getRoadIDFromInt(i);
		roadIDS[0] = '3';	//first char of the road ID 3=street
		return roadIDS;
	}
	private static char[] getAvenueID(int i) {
		char[] roadIDA = getRoadIDFromInt(i);
		roadIDA[0] = '4';
		return roadIDA;
	}
	
	private static char[] getRoadIDFromInt(int i) {
		char[] roadID = new char[4];
		char[] roadNumber = String.valueOf(i).toCharArray();
		int k=0;
		for(int j=0; j<(3-roadNumber.length); j++) {	//zero pending to hundreds and tens position
			roadID[j+1]='0';
			k++;
		}
		for(int j=k; j<3; j++)
			roadID[j+1]=roadNumber[j-k];
		return roadID;
	}
}