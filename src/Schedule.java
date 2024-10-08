import java.util.Map.Entry;

public class Schedule {
	
	private int greenTime = 5000;
	private int yellowTime = 2000;
	private int sleepTime = 100;
	private double x = 1;
	private double serviceRate;
	
	//private boolean isRunning = false;
	
	char scheduleType = 'D';
	/*
	 * D=dumb scheduling
	 * S=self scheduling
	 * C=coordinated scheduling
	 * V=convoy scheduling
	 */
	
	public Schedule(char scheduleType, int greenTime, int yellowTime) {
		this.scheduleType = scheduleType;
		this.greenTime = greenTime;
		this.yellowTime = yellowTime;
		sleepTime = 100;
		serviceRate = Math.pow(Frame.Lambda, -1);
		System.out.println("Service Rate\t"+serviceRate);
		System.out.println("Time\tTotal Cars\tWaiting\tIn\tMoving\tTotal Out");
	}
	
	public void workTime() {
		TrafficPoint tempPoint = null;
		boolean wait = false;
		if(scheduleType == 'D') {
			for(Entry<char[], TrafficPoint> entry : TrafficPoint.getEntrySet()) {
				if(entry.getValue().control[0] == 'E') continue;
				tempPoint = entry.getValue();
				tempPoint.cycleTime += sleepTime;
				if((tempPoint.control[0] == 'Y' || tempPoint.control[1] == 'Y') &&
						tempPoint.cycleTime > (yellowTime+sleepTime)) {
					tempPoint.nextControl();
					tempPoint.cycleTime = 0;
				} else if((tempPoint.control[0] == 'G' || tempPoint.control[1] == 'G') && 
						tempPoint.cycleTime > (greenTime+sleepTime)) {
					tempPoint.nextControl();
					tempPoint.cycleTime = 0;
				}
			}
		} else if(scheduleType == 'S') {
			for(Entry<char[], TrafficPoint> entry : TrafficPoint.getEntrySet()) {
				if(entry.getValue().control[0] == 'E') continue;
				tempPoint = entry.getValue();
				tempPoint.cycleTime += sleepTime;
				if((tempPoint.control[0] == 'Y' || tempPoint.control[1] == 'Y') &&
						tempPoint.cycleTime > (yellowTime+sleepTime)) {
					tempPoint.nextControl();
					tempPoint.cycleTime = 0;
					/*if(scheduleType == 'C') { keep separate
						if(tempPoint.control[0] == 'G') {
							tempPoint.nextStreet.expectedCars[0] = tempPoint.comingCars[0];
							tempPoint.nextStreet
						}
					}*/
				} else if((tempPoint.control[0] == 'G' || tempPoint.control[1] == 'G') &&
						tempPoint.cycleTime > (greenTime+sleepTime)) {
					tempPoint.nextControl();
					tempPoint.cycleTime = 0;
				} else if((tempPoint.control[0] != 'Y' && tempPoint.control[1] != 'Y') &&
						tempPoint.cycleTime > (yellowTime+sleepTime)) {
					if(tempPoint.control[0] == 'R' && 
							(tempPoint.comingCars[0] > tempPoint.comingCars[1])) {
						tempPoint.nextControl();
						tempPoint.cycleTime = 0;
					} else if(tempPoint.control[1] == 'R' &&
							(tempPoint.comingCars[1] > tempPoint.comingCars[0])) {
						tempPoint.nextControl();
						tempPoint.cycleTime = 0;
					}
				}
			}
		} else if(scheduleType == 'C') {
			for(Entry<char[], TrafficPoint> entry : TrafficPoint.getEntrySet()) {
				if(entry.getValue().control[0] == 'E') continue;
				tempPoint = entry.getValue();
				tempPoint.cycleTime += sleepTime;
				if((tempPoint.control[0] == 'Y' || tempPoint.control[1] == 'Y') &&
						tempPoint.cycleTime > (yellowTime+sleepTime)) {
					tempPoint.nextControl();
					tempPoint.cycleTime = 0;
				} else if((tempPoint.control[0] == 'G' || tempPoint.control[1] == 'G') && 
						tempPoint.cycleTime > (greenTime+sleepTime)) {
					tempPoint.nextControl();
					tempPoint.cycleTime = 0;
				} else if((tempPoint.control[0] != 'Y' && tempPoint.control[1] != 'Y') && 
						tempPoint.cycleTime > (yellowTime+sleepTime)) {
					if(tempPoint.control[0] == 'R' && 
							((tempPoint.expectedCars[0]+tempPoint.comingCars[0]) > 
							(tempPoint.expectedCars[1]+tempPoint.comingCars[1]))) {
						tempPoint.nextControl();
						tempPoint.cycleTime = 0;
					} else if(tempPoint.control[1] == 'R' && 
							((tempPoint.expectedCars[1]+tempPoint.comingCars[1]) > 
							(tempPoint.expectedCars[0]+tempPoint.comingCars[0]))) {
						tempPoint.nextControl();
						tempPoint.cycleTime = 0;
					}
				}
			}
		} else if(this.scheduleType == 'V') {
			for(Entry<char[], TrafficPoint> entry : TrafficPoint.getEntrySet()) {
				if(entry.getValue().control[0] == 'E') continue;
				tempPoint = entry.getValue();
				tempPoint.cycleTime += sleepTime;
				if((tempPoint.control[0] == 'Y' || tempPoint.control[1] == 'Y') && 
						tempPoint.cycleTime > (yellowTime+sleepTime)) {
					for(Entry<Integer, Convoy> entry1 : Convoy.getEntrySet()) {
						if(tempPoint.roadDir[0] != entry1.getValue().listOfCars[0].dir ||
								tempPoint.roadDir[1] != entry1.getValue().listOfCars[0].dir) continue;
						if(tempPoint.roadDir[0] == 'E' || tempPoint.roadDir[1] == 'S') { //negative directions
							wait = (tempPoint.distance(entry1.getValue().listOfCars[0]) > 0 && 
									tempPoint.distance(entry1.getValue().lastCar()) < 0);
						} else if(tempPoint.roadDir[0] == 'W' || tempPoint.roadDir[1] == 'N') {
							wait = (tempPoint.distance(entry1.getValue().listOfCars[0]) < 0 && 
									tempPoint.distance(entry1.getValue().lastCar()) > 0);
						}
						if(wait) break;
					}
					if(!wait) {
						tempPoint.nextControl();
						tempPoint.cycleTime = 0;
					}
				} else if((tempPoint.control[0] == 'G' || tempPoint.control[1] == 'G') && 
						tempPoint.cycleTime > (greenTime+sleepTime)) {
					tempPoint.nextControl();
					tempPoint.cycleTime = 0;
				}
				wait = false;
			}
		}
	}
	
	public void whatCars() {
		double temp = 0;
		this.x = (int) Frame.systemTime / 100;		//multiply by a random number to avoid getting zero (0)
													//try curve fitting for x stretching by 10
		temp = (int) ((Frame.NumberOfCars*Math.random()*serviceRate*Math.exp(-1*serviceRate*this.x)));
//		temp = (int) (Frame.NumberOfCars);
		
		if(temp > 0) Car.addCars((int)temp);
		System.out.println(Frame.systemTime+"\t"+Car.carCount+"\t\t"
				+(Car.carCount-Car.mCarCount-Car.sCarCount)
				+"\t"+temp+"\t"+Car.mCarCount+"\t"+Car.sCarCount);
	}
	
	/*@Override
	public void run() {
		while (isRunning) {
			workTime();
			whatCars();
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	public void start() {
		if(lightSchedule == null)
			lightSchedule = new Thread(this);
		isRunning = true;
		lightSchedule.start();
	}
	public void pause() {
		lightSchedule.interrupt();
		isRunning = false;
	}*/
}
