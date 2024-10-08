import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TrafficPoint {
	
	private static HashMap<char[], TrafficPoint> trafficPoints = new HashMap<char[], TrafficPoint>();
	
	protected Road street = null;
	protected Road avenue = null;
	protected TrafficPoint nextStreet = null;
	protected TrafficPoint nextAvenue = null;
	protected int cycleTime = 0;
	
	protected char[] pointID = new char[8];
	/*
	 * TrafficControl is a class to represent intersection points
	 * ID divided into two 4-char IDs
	 * first 4-chars [0:3] represents the street ID
	 * second 4-chars [4:7] represents the avenue ID
	 * first char of the road ID 1=entrance, 2=exit, 3=street, 4=avenue
	 */
	protected char[] roadDir = new char[2];
	//first char street direction, second char avenue direction
	//protected int[] xy = new int[2];
	protected int[][][] sectors = new int[3][3][2];
	protected boolean[][] flag = new boolean[3][3];
	protected char[] control = new char[2];
	/*
	 * first char street light status R=Red G=Green Y=Yellow
	 * second char avenue light status R=Red G=Green Y=Yellow
	 * control 'EN'=entrance 'EX'=exit points
	 */
	//protected Car firstInLineS = null;
	//protected Car firstInLineA = null;
	//protected boolean carInQueueS = false;
	//protected boolean carInQueueA = false;
	//protected int queuedCarsS = 0;
	//protected int queuedCarsA = 0;
	protected int[] comingCars = new int[]{0, 0};
	protected int[] expectedStraightCars = new int[]{0, 0};
	protected int[] expectedTurningCars = new int[]{0, 0};
	protected int[] expectedCars = new int[]{0, 0};
	/*
	 * parameter for second and third scheduling for previous traffic points 
	 * to pass on # cars to be released after green time 
	 * [0] for street
	 * [1] for avenue
	 */
	//queue of cars waiting
	
	public static boolean addControlPoints(Set<Map.Entry<char[] ,Road>> set) {
		if(set == null)
			return false;
		//declaring and initializing street and avenue entrance and exit points
		System.out.println("Initializing Entrance and Exit Points");
		Road tempRoad;
		TrafficPoint tempPoint1, tempPoint2;
		int difference = 0;
		for(Map.Entry<char[], Road> entry : set) {
			tempRoad = entry.getValue();
			if(tempRoad.roadType == 'S') {
				tempPoint1 = new TrafficPoint(tempRoad, null, new char[]{'E','N'});
				tempRoad.setEntrancePoints(tempPoint1);
				for(int i=0; i<3; i++) {
					for(int j=0; j<3; j++) {
						tempPoint1.sectors[i][j][0] = (tempRoad.roadDir == 'E')? 
								Road.xAccumulativePosition : 0;
						tempPoint1.sectors[i][j][1] = tempRoad.sectors[i];
						tempPoint1.flag[i][j] = false;
					}
				}
				trafficPoints.put(tempPoint1.pointID, tempPoint1);
				tempPoint1 = new TrafficPoint(tempRoad, null, new char[]{'E','X'});
				tempRoad.setExitPoints(tempPoint1);
				for(int i=0; i<3; i++) {
					for(int j=0; j<3; j++) {
						tempPoint1.sectors[i][j][0] = (tempRoad.roadDir == 'W')? 
								Road.xAccumulativePosition : 0;
						tempPoint1.sectors[i][j][1] = tempRoad.sectors[i];
						tempPoint1.flag[i][j] = false;
					}
				}
				trafficPoints.put(tempPoint1.pointID, tempPoint1);
			}
			else if(tempRoad.roadType == 'A') {
				tempPoint1 = new TrafficPoint(null, tempRoad, new char[]{'E','N'});
				tempRoad.setEntrancePoints(tempPoint1);
				for(int i=0; i<3; i++) {
					for(int j=0; j<3; j++) {
						tempPoint1.sectors[i][j][0] = tempRoad.sectors[j];
						tempPoint1.sectors[i][j][1] = (tempRoad.roadDir == 'N')?
								0 : Road.yAccumulativePosition;
						tempPoint1.flag[i][j] = false;
					}
				}
				trafficPoints.put(tempPoint1.pointID, tempPoint1);
				tempPoint1 = new TrafficPoint(null, tempRoad, new char[]{'E','X'});
				tempRoad.setExitPoints(tempPoint1);
				for(int i=0; i<3; i++) {
					for(int j=0; j<3; j++) {
						tempPoint1.sectors[i][j][0] = tempRoad.sectors[j];
						tempPoint1.sectors[i][j][1] = (tempRoad.roadDir == 'S')?
								0 : Road.yAccumulativePosition;
						tempPoint1.flag[i][j] = false;
					}
				}
				trafficPoints.put(tempPoint1.pointID, tempPoint1);
			}
		}
		System.out.println("Initializing Intersection points");
		for(Map.Entry<char[], Road> entry1 : set) {
			for(Map.Entry<char[], Road> entry2 : set) {
				if(entry1.getValue().roadType == 'S' && 
						entry2.getValue().roadType == 'A') {
					tempPoint1 = new TrafficPoint(entry1.getValue(), entry2.getValue(), 
							new char[]{'R','Y'});
					for(int i=0; i<3; i++) {
						for(int j=0; j<3; j++) {
							tempPoint1.sectors[i][j][0] = entry2.getValue().sectors[j];
							tempPoint1.sectors[i][j][1] = entry1.getValue().sectors[i];
							tempPoint1.flag[i][j] = false;
						}
					}
					trafficPoints.put(tempPoint1.pointID, tempPoint1);
				}
			}
		}
		//setting next street and avenue intersection points
		for(Map.Entry<char[], TrafficPoint> entry1 : trafficPoints.entrySet()) {
			for(Map.Entry<char[], TrafficPoint> entry2 : trafficPoints.entrySet()) {
				tempPoint1 = entry1.getValue();
				tempPoint2 = entry2.getValue();
				difference = tempPoint1.distance(tempPoint2);
				if(tempPoint1.sectors[1][1][0] == tempPoint2.sectors[1][1][0]) {	//points on same avenue
					if(tempPoint1.roadDir[1] == 'N') { 		//same x position = same direction
						if(difference < 0) {
							if(tempPoint1.nextAvenue == null)
								tempPoint1.nextAvenue = tempPoint2;
							else if(difference > tempPoint1.distance(tempPoint1.nextAvenue))
								tempPoint1.nextAvenue = tempPoint2;
						}
					} else if(tempPoint1.roadDir[1] == 'S') {
						if(difference > 0) {
							if(tempPoint1.nextAvenue == null)
								tempPoint1.nextAvenue = tempPoint2;
							else if(difference < tempPoint1.distance(tempPoint1.nextAvenue))
								tempPoint1.nextAvenue = tempPoint2;
						}
					}
				} else if(tempPoint1.sectors[1][1][1] == tempPoint2.sectors[1][1][1]) {	//points on same street
					if(tempPoint1.roadDir[0] == 'E') {
						if(difference > 0) {
							if(tempPoint1.nextStreet == null)
								tempPoint1.nextStreet = tempPoint2;
							else if(difference < tempPoint1.distance(tempPoint1.nextStreet))
								tempPoint1.nextStreet = tempPoint2;
						}
					} else if(tempPoint1.roadDir[0] == 'W') {
						if(difference < 0) {
							if(tempPoint1.nextStreet == null)
								tempPoint1.nextStreet = tempPoint2;
							else if(difference > tempPoint1.distance(tempPoint1.nextStreet))
								tempPoint1.nextStreet = tempPoint2;
						}
					}
				}
			}
		}
		/*for(Map.Entry<char[], TrafficPoint> entry : trafficPoints.entrySet()) {
			tempPoint1 = entry.getValue();
			System.out.print(tempPoint1.pointID);
			System.out.print("\t");
			System.out.print(tempPoint1.roadDir[0]+" "+tempPoint1.roadDir[1]+"\t");
			if(tempPoint1.nextAvenue != null)
				System.out.print(tempPoint1.nextAvenue.pointID);
			else
				System.out.print("null");
			System.out.print("\t");
			if(tempPoint1.nextStreet != null)
				System.out.println(tempPoint1.nextStreet.pointID);
			else
				System.out.println("null");
		}*/
		return true;
	}
	
	private TrafficPoint(Road street, Road avenue, char[] control) {
		char[] streetID = new char[]{'0','0','0','0'};
		char[] avenueID = new char[]{'0','0','0','0'};
		if(avenue == null) {						//initialize as street
			streetID = street.roadID;
		} else if(street == null) {					//initialize as avenue
			avenueID = avenue.roadID;
		} else {									//initialize for both
			streetID = street.roadID;
			avenueID = avenue.roadID;
		}
		for(int i=0; i<pointID.length; i++){		//copying road IDs as traffic light ID
			if(i<4)
				this.pointID[i]=streetID[i];
			else
				this.pointID[i]=avenueID[i-4];
		}
		if(avenue == null) {			//initialize as street entrance or exit
			if(control[1] == 'N')
				this.pointID[0] = '1';
			else if(control[1] == 'X')
				this.pointID[0] = '2';
			//this.xy[1] = street.accumulativePosition;
			//based on entrance, exit and direction position assigned
			/*if((street.roadDir == 'E' && control[1] == 'N') || 		//from east entrance
					(street.roadDir == 'W' && control[1] == 'X'))	//from west exit
				this.xy[0] = Road.xAccumulativePosition;
			else if((street.roadDir == 'E' && control[1] == 'X') || 	//from east exit
					(street.roadDir == 'W' && control[1] == 'N'))	//from west entrance
				this.xy[0] = 0;*/
			this.roadDir[0] = street.roadDir;
			this.roadDir[1] = this.roadDir[0];
		} else if(street == null) {		//initialize as avenue entrance or exit
			if(control[1] == 'N')
				this.pointID[4] = '1';
			else if(control[1] == 'X')
				this.pointID[4] = '2';
			//this.xy[0] = avenue.accumulativePosition;
			/*if((avenue.roadDir == 'N' && control[1] == 'N') || 
					(avenue.roadDir == 'S' && control[1] == 'X'))
				this.xy[1] = 0;
			else if((avenue.roadDir == 'S' && control[1] == 'N') || 
					(avenue.roadDir == 'N' && control[1] == 'X'))
				this.xy[1] = Road.yAccumulativePosition;*/
			this.roadDir[0] = avenue.roadDir;
			this.roadDir[1] = this.roadDir[0];
		} else {						//initialize as intersection
			streetID = street.roadID;
			avenueID = avenue.roadID;
			//this.xy[0] = avenue.accumulativePosition;
			//this.xy[1] = street.accumulativePosition;
			this.roadDir[0] = street.roadDir;
			this.roadDir[1] = avenue.roadDir;
		}
		this.street = street;
		this.avenue = avenue;
		this.control = Arrays.copyOfRange(control, 0, 2);
	}
	
	public static Set<Map.Entry<char[] ,TrafficPoint>> getEntrySet() {
		return trafficPoints.entrySet();
	}
	
	
	
	public boolean nextControl() {
		if(this.control[0] == 'E')		//not street or avenue
			return false;
		else if(control[0] == 'R') {
			if(control[1] == 'R') {
				control[0] = 'G';
				this.expectedCars[0] = 0;
				this.nextStreet.expectedCars[0] += this.expectedStraightCars[0];
				this.nextAvenue.expectedCars[1] += this.expectedTurningCars[0];
			} else if(control[1] == 'G')
				control[1] = 'Y';
			else if(control[1] == 'Y') {
				control = new char[]{'G','R'};
				this.expectedCars[0] = 0;
				this.nextStreet.expectedCars[0] += this.expectedStraightCars[0];
				this.nextAvenue.expectedCars[1] += this.expectedTurningCars[0];
			}
		} else if(control[0] == 'G') {
			control[0] = 'Y';
		} else if(control[0] == 'Y') {
			control = new char[]{'R','G'};
			this.expectedCars[1] = 0;
			this.nextAvenue.expectedCars[1] += this.expectedStraightCars[1];
			this.nextStreet.expectedCars[0] += this.expectedTurningCars[1];
		} else
			control = new char[]{'R','R'};
		/*System.out.print(this.pointID);
		System.out.println(" #Cars S= "+this.comingCars[0]+" A= "+this.comingCars[1]);*/
		return true;
	}
	
	public int distance(TrafficPoint tempPoint) {
		if(this.sectors[1][1][0] == tempPoint.sectors[1][1][0])
			return this.sectors[1][1][1] - tempPoint.sectors[1][1][1];
		else if(this.sectors[1][1][1] == tempPoint.sectors[1][1][1])
			return this.sectors[1][1][0] - tempPoint.sectors[1][1][0];
		else
			return Integer.MAX_VALUE;
	}
	public int distance(Car tempCar) {
		if(tempCar.dir == 'N') {
			if(tempCar.xy[0] == this.sectors[0][0][0])
				return this.sectors[0][0][1] - tempCar.xy[1];
			else if(tempCar.xy[0] == this.sectors[0][1][0])
				return this.sectors[0][1][1] - tempCar.xy[1];
			else if(tempCar.xy[0] == this.sectors[0][2][0])
				return this.sectors[0][2][1] - tempCar.xy[1];
		} else if(tempCar.dir == 'S') {
			if(tempCar.xy[0] == this.sectors[2][0][0])
				return this.sectors[2][0][1] - tempCar.xy[1];
			else if(tempCar.xy[0] == this.sectors[2][1][0])
				return this.sectors[2][1][1] - tempCar.xy[1];
			else if(tempCar.xy[0] == this.sectors[2][2][0])
				return this.sectors[2][2][1] - tempCar.xy[1];
		} else if(tempCar.dir == 'E') {
			if(tempCar.xy[1] == this.sectors[0][2][1])
				return this.sectors[0][2][0] - tempCar.xy[0];
			else if(tempCar.xy[1] == this.sectors[1][2][1])
				return this.sectors[1][2][0] - tempCar.xy[0];
			else if(tempCar.xy[1] == this.sectors[2][2][1])
				return this.sectors[2][2][0] - tempCar.xy[0];
		} else if(tempCar.dir == 'W') {
			if(tempCar.xy[1] == this.sectors[0][0][1])
				return this.sectors[0][0][0] - tempCar.xy[0];
			else if(tempCar.xy[1] == this.sectors[1][0][1])
				return this.sectors[1][0][0] - tempCar.xy[0];
			else if(tempCar.xy[1] == this.sectors[2][0][1])
				return this.sectors[2][0][0] - tempCar.xy[0];
		}
		return Integer.MAX_VALUE;
	}
	//FUNCTION NEEDS CODE OPTIMIZATION
	public boolean[] intersectionLogic(Car tempCar, int[] dist) {
		/*
		 * [0] true=move, false=don't move, [1] true=dequeue, false=don't dequeue
		 */
		boolean[] TT = new boolean[]{true, true};
		boolean[] TF = new boolean[]{true, false};
		boolean[] FF = new boolean[]{false, false};
		if(this.control[1] == 'X') return TF;
		int i, j, di, dj;
		for(i=0; i<4; i++) {
			if(i == 3) break;
			if(tempCar.xy[0] <= this.sectors[0][i][0]) break;
		}
		for(j=0; j<4; j++) {
			if(j == 3) break;
			if(tempCar.xy[1] <= this.sectors[j][0][1]) break;
		}
		for(di=0; di<4; di++) {
			if(di == 3) break;
			if((tempCar.xy[0]+dist[0]) <= this.sectors[0][di][0]) break;
		}
		for(dj=0; dj<4; dj++) {
			if(dj == 3) break;
			if((tempCar.xy[1]+dist[1]) <= this.sectors[dj][0][1]) break;
		}
		
		if(tempCar.dir == 'N') {
			if(dj == 0 && this.distance(tempCar) > Frame.fullDistance)	//didn't reach intersection
					return TF;
		} else if(tempCar.dir == 'S') {
			if(dj == 3 && this.distance(tempCar) < -1*Frame.fullDistance)
				return TF;
		} else if(tempCar.dir == 'E') {
			if(di == 3 && this.distance(tempCar) < -1*Frame.fullDistance)
				return TF;
		} else if(tempCar.dir == 'W') {
			if(di == 0 && this.distance(tempCar) > Frame.fullDistance)
				return TF;
		}
		//limit speed in intersection
		if(Math.abs(tempCar.dxy[0]+tempCar.dxy[1]) >= Frame.carWidth) 
			TF = FF;
		
		if(this != tempCar.turningPoint1 && this != tempCar.turningPoint2) {	//going straight
			if(tempCar.dir == 'N') {		//difference between js, i == di
				if(dj == 0 && this.control[1] == 'R') {
					return FF;
				} else if(j == dj) {			//moving in same square - sector
					return TF;
				} else if(dj == 0 || dj == 1) {
					if(!this.flag[dj][i]) {
						this.flag[dj][i] = true;
						return TF;
					} return FF;
				} else if(dj == 2) {
					if(!this.flag[dj][i]) {
						this.flag[0][i] = false;
						this.flag[dj][i] = true;
						return TF;
					} return FF;
				} else if(dj == 3) {
					this.flag[1][i] = false;
					this.flag[2][i] = false;
					return TT;
				}
			} else if(tempCar.dir == 'S') {
				if(dj == 3 && this.control[1] == 'R') {
					return FF;
				} else if(j == dj) {
					return TF;
				} else if(dj == 3 || dj == 2) {
					if(!this.flag[dj][i]) {
						this.flag[dj][i] = true;
						return TF;
					} return FF;
				} else if(dj == 1) {
					if(!this.flag[dj][i]) {
						this.flag[2][i] = false;
						this.flag[dj][i] = true;
						return TF;
					} return FF;
				} else if(dj == 0) {
					this.flag[1][i] = false;
					this.flag[2][i] = false;
					return TT;
				}
			} else if(tempCar.dir == 'E') {
				if(di == 3 && this.control[0] == 'R') {
					return FF;
				} else if(i == di) {
					return TF;
				} else if(di == 3 || di == 2) {
					if(!this.flag[j][di]) {
						this.flag[j][di] = true;
						return TF;
					} return FF;
				} else if(di == 1) {
					if(!this.flag[j][di]) {
						this.flag[j][2] = false;
						this.flag[j][di] = true;
						return TF;
					} return FF;
				} else if(di == 0) {
					this.flag[j][1] = false;
					this.flag[j][2] = false;
					return TT;
				}
			} else if(tempCar.dir == 'W') {
				if(di == 0 && this.control[0] == 'R') {
					return FF;
				} else if(i == di) {
					return TF;
				} else if(di == 0 || di == 1)	{
					if(!this.flag[j][di]) {
						this.flag[j][di] = true;
						return TF;
					} return FF;
				} else if(di == 2) {
					if(!this.flag[j][di]) {
						this.flag[j][0] = false;
						this.flag[j][di] = true;
						return TF;
					} return FF;
				} else if(di == 3) {
					this.flag[j][1] = false;
					this.flag[j][2] = false;
					return TT;
				}
			}
		} else if(this == tempCar.turningPoint1 || this == tempCar.turningPoint2) {
			if(tempCar.dir == 'N') {		//difference between js, i == di
				if(dj == 0 && this.control[1] == 'R') {
					return FF;
				}
				//determine the turning lane
				if(j == dj && dj == 0 && tempCar.remainingTurns == 1) {
					int rand = (int)(Math.random()*3);
					if(rand == 0) tempCar.lane = 'L';
					else if(rand == 1) tempCar.lane = 'M';
					else if(rand == 2) tempCar.lane = 'R';
					/*System.out.print(tempCar.carID);
					System.out.println(" turning lane "+tempCar.lane);*/
				} else if(tempCar.remainingTurns > 1) {
					if(tempCar.turningPoint1.roadDir[0] == 'E')
						tempCar.lane = 'L';
					else if(tempCar.turningPoint1.roadDir[0] == 'W')
						tempCar.lane = 'R';
				}
				
				if(j == dj)				//moving in same square - sector
					return TF;
				
				if(dj == 0) {
					if(!this.flag[dj][i]) {
						this.flag[dj][i] = true;
						return TF;
					} return FF;
				} else if(dj == 1) {
					if(!this.flag[dj][i]) {
						this.flag[dj][i] = true;
						if(tempCar.lane == 'L') {
							tempCar.road = this.street;
							//tempCar.nextPoint = this.nextStreet;
							tempCar.remainingTurns--;
							tempCar.dir = this.roadDir[0];
							tempCar.switchSpeed();
							tempCar.xy = Arrays.copyOfRange(this.sectors[dj-1][i], 0, 2);
							this.flag[dj][i] = false;
							return TT;
						} 
						return TF;
					} return FF;
				} else if(dj == 2) {
					if(!this.flag[dj][i]) {
						this.flag[0][i] = false;
						this.flag[dj][i] = true;
						if(tempCar.lane == 'M') {
							tempCar.road = this.street;
							//tempCar.nextPoint = this.nextStreet;
							tempCar.remainingTurns--;
							tempCar.dir = this.roadDir[0];
							tempCar.switchSpeed();
							tempCar.xy = Arrays.copyOfRange(this.sectors[dj-1][i], 0, 2);
							this.flag[dj][i] = false;
							this.flag[dj-1][i] = false;
							return TT;
						}
						return TF;
					} return FF;
				} else if(dj == 3) {
					this.flag[1][i] = false;
					this.flag[2][i] = false;
					//if not right or middle lane then got to be left
					tempCar.road = this.street;
					//tempCar.nextPoint = this.nextStreet;
					tempCar.remainingTurns--;
					tempCar.dir = this.roadDir[0];
					tempCar.switchSpeed();
					tempCar.xy = Arrays.copyOfRange(this.sectors[dj-1][i], 0, 2);
					this.flag[dj-1][i] = false;
					return TT;
				} else return FF;
			} else if(tempCar.dir == 'S') {
				if(dj == 3 && this.control[1] == 'R') {
					return FF;
				}
				//determine the turning lane
				if(j == dj && dj == 3 && tempCar.remainingTurns == 1) {
					int rand = (int)(Math.random()*3);
					if(rand == 0) tempCar.lane = 'L';
					else if(rand == 1) tempCar.lane = 'M';
					else if(rand == 2) tempCar.lane = 'R';
					/*System.out.print(tempCar.carID);
					System.out.println(" turning lane "+tempCar.lane);*/
				} else if(tempCar.remainingTurns > 1) {
					if(tempCar.turningPoint1.roadDir[0] == 'E')
						tempCar.lane = 'R';
					else if(tempCar.turningPoint1.roadDir[0] == 'W')
						tempCar.lane = 'L';
				}
				if(j == dj)				//moving in same square - sector
					return TF;
				
				if(dj == 3) {
					if(!this.flag[dj][i]) {
						this.flag[dj][i] = true;
						return TF;
					} return FF;
				} else if(dj == 2) {
					if(!this.flag[dj][i]) {
						this.flag[dj][i] = true;
						if(tempCar.lane == 'R') {
							tempCar.road = this.street;
							//tempCar.nextPoint = this.nextStreet;
							tempCar.remainingTurns--;
							tempCar.dir = this.roadDir[0];
							tempCar.switchSpeed();
							tempCar.xy = Arrays.copyOfRange(this.sectors[dj][i], 0, 2);
							this.flag[dj][i] = false;
							return TT;
						} 
						return TF;
					} return FF;
				} else if(dj == 1) {
					if(!this.flag[dj][i]) {
						this.flag[2][i] = false;
						this.flag[dj][i] = true;
						if(tempCar.lane == 'M') {
							tempCar.road = this.street;
							//tempCar.nextPoint = this.nextStreet;
							tempCar.remainingTurns--;
							tempCar.dir = this.roadDir[0];
							tempCar.switchSpeed();
							tempCar.xy = Arrays.copyOfRange(this.sectors[dj][i], 0, 2);
							this.flag[dj][i] = false;
							this.flag[dj+1][i] = false;
							return TT;
						}
						return TF;
					} return FF;
				} else if(dj == 0) {
					this.flag[1][i] = false;
					this.flag[2][i] = false;
					if(tempCar.lane == 'L') {
						tempCar.road = this.street;
						//tempCar.nextPoint = this.nextStreet;
						tempCar.remainingTurns--;
						tempCar.dir = this.roadDir[0];
						tempCar.switchSpeed();
						tempCar.xy = Arrays.copyOfRange(this.sectors[dj][i], 0, 2);
						this.flag[dj][i] = false;
						this.flag[dj+1][i] = false;
						return TT;
					}
					return TT;
				}
			} else if(tempCar.dir == 'E') {
				if(di == 3 && this.control[0] == 'R') {
					return FF;
				}
				//determine the turning lane
				if(i == di && di == 3 && tempCar.remainingTurns == 1) {
					int rand = (int)(Math.random()*3);
					if(rand == 0) tempCar.lane = 'L';
					else if(rand == 1) tempCar.lane = 'M';
					else if(rand == 2) tempCar.lane = 'R';
					/*System.out.print(tempCar.carID);
					System.out.println(" turning lane "+tempCar.lane);*/
				} else if(tempCar.remainingTurns > 1) {
					if(tempCar.turningPoint1.roadDir[1] == 'N')
						tempCar.lane = 'L';
					else if(tempCar.turningPoint1.roadDir[1] == 'S')
						tempCar.lane = 'R';
				}
				if(i == di)				//moving in same square - sector
					return TF;
				
				if(di == 3) {
					if(!this.flag[j][di]) {
						this.flag[j][di] = true;
						return TF;
					} return FF;
				} else if(di == 2) {
					if(!this.flag[j][di]) {
						this.flag[j][di] = true;
						if(tempCar.lane == 'R') {
							tempCar.road = this.street;
							//tempCar.nextPoint = this.nextStreet;
							tempCar.remainingTurns--;
							tempCar.dir = this.roadDir[1];
							tempCar.switchSpeed();
							tempCar.xy = Arrays.copyOfRange(this.sectors[j][di], 0, 2);
							this.flag[j][di] = false;
							return TT;
						} 
						return TF;
					}
				}
				else if(di == 1) {
					if(!this.flag[j][di]) {
						this.flag[j][2] = false;
						this.flag[j][di] = true;
						if(tempCar.lane == 'M') {
							tempCar.road = this.street;
							//tempCar.nextPoint = this.nextStreet;
							tempCar.remainingTurns--;
							tempCar.dir = this.roadDir[1];
							tempCar.switchSpeed();
							tempCar.xy = Arrays.copyOfRange(this.sectors[j][di], 0, 2);
							this.flag[j][di] = false;
							this.flag[j][di+1] = false;
							return TT;
						} 
						return TF;
					} return FF;
				} else if(di == 0) {
					this.flag[j][1] = false;
					this.flag[j][2] = false;
					tempCar.road = this.street;
					//tempCar.nextPoint = this.nextStreet;
					tempCar.remainingTurns--;
					tempCar.dir = this.roadDir[1];
					tempCar.switchSpeed();
					tempCar.xy = Arrays.copyOfRange(this.sectors[j][di], 0, 2);
					this.flag[j][di] = false;
					this.flag[j][di+1] = false;
					return TT;
				}
			} else if(tempCar.dir == 'W') {
				if(di == 0 && this.control[0] == 'R') {
					return FF;
				}
				//determine the turning lane
				if(i == di && di == 0 && tempCar.remainingTurns == 1) {
					int rand = (int)(Math.random()*3);
					if(rand == 0) tempCar.lane = 'L';
					else if(rand == 1) tempCar.lane = 'M';
					else if(rand == 2) tempCar.lane = 'R';
					/*System.out.print(tempCar.carID);
					System.out.println(" turning lane "+tempCar.lane);*/
				} else if(tempCar.remainingTurns > 1) {
					if(tempCar.turningPoint1.roadDir[1] == 'N')
						tempCar.lane = 'L';
					else if(tempCar.turningPoint1.roadDir[1] == 'S')
						tempCar.lane = 'R';
				}
				if(i == di)				//moving in same square - sector
					return TF;
				
				
				if(di == 0)	{
					if(!this.flag[j][di]) {
						this.flag[j][di] = true;
						return TF;
					} return FF;
				} else if(di == 1) {
					if(!this.flag[j][di]) {
						this.flag[j][di] = true;
						if(tempCar.lane == 'L') {
							tempCar.road = this.street;
							//tempCar.nextPoint = this.nextStreet;
							tempCar.remainingTurns--;
							tempCar.dir = this.roadDir[1];
							tempCar.switchSpeed();
							tempCar.xy = Arrays.copyOfRange(this.sectors[j][di-1], 0, 2);
							this.flag[j][di] = false;
							this.flag[j][di-1] = false;
							return TT;
						}
						return TF;
					} return FF;
				} else if(di == 2) {
					if(!this.flag[j][di]) {
						this.flag[j][0] = false;
						this.flag[j][di] = true;
						if(tempCar.lane == 'M') {
							tempCar.road = this.street;
							//tempCar.nextPoint = this.nextStreet;
							tempCar.remainingTurns--;
							tempCar.dir = this.roadDir[1];
							tempCar.switchSpeed();
							tempCar.xy = Arrays.copyOfRange(this.sectors[j][di-1], 0, 2);
							this.flag[j][di] = false;
							this.flag[j][di-1] = false;
							return TT;
						}
						return TF;
					} return FF;
				} else if(di == 3) {
					this.flag[j][1] = false;
					this.flag[j][2] = false;
					tempCar.road = this.street;
					//tempCar.nextPoint = this.nextStreet;
					tempCar.remainingTurns--;
					tempCar.dir = this.roadDir[1];
					tempCar.switchSpeed();
					tempCar.xy = Arrays.copyOfRange(this.sectors[j][di-1], 0, 2);
					this.flag[j][di-1] = false;
					return TT;
				}
			}
			//-------------------------
		}
		return FF;
	}
}
