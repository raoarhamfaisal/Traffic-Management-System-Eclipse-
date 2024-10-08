/* This class will be responsible for drawing the grid 
 * Layout the streets, avenues, entry points
 * and traffic signals
 */

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.ImageIcon;

public class PaintGrid extends Canvas {
	
	private static final long serialVersionUID = 1L;
	/*private Thread gridPaint;
	private boolean isRunning = false;*/
	
	private Image northImg, southImg, eastImg, westImg;
	
	public PaintGrid() {
		loadImages();
	}
	
	Image offscreen;
	Graphics offgraphics;
	
	public void relax() {
		TrafficPoint tempPoint;
		Car tempCar = null, tempCar1 = null, inFront = null;
		int distance = Integer.MAX_VALUE;
		int tempDistance;
		boolean wait = false;
		
		for(Entry<char[], Car> entry1 : Car.getEntrySet()) {
			tempCar = entry1.getValue();
			if(tempCar.phase != 'Q') continue;		//not queued car
			tempPoint = tempCar.entrancePoint;
			//check if entrance point && road in front clear
			for(Entry<char[], Car> entry2 : Car.getEntrySet()) {
				tempCar1 = entry2.getValue();
				if(tempCar1.phase != 'M') continue;
				distance = tempPoint.distance(tempCar1);
				wait = Math.abs(distance) < (Frame.carLength+Frame.Clearance);
				if(wait) {
					break;
				}
			}
			if(!wait) {
				/*System.out.print(tempCar.carID);
				System.out.println(" car moving");*/
				if(tempCar.lane == 'M')
					tempCar.xy = Arrays.copyOfRange(tempPoint.sectors[1][1], 0, 2);
				else if((tempPoint.roadDir[0] == 'E' && tempCar.lane == 'R') || 
						(tempPoint.roadDir[0] == 'W' && tempCar.lane == 'L'))
					tempCar.xy = Arrays.copyOfRange(tempPoint.sectors[0][0], 0, 2);
				else if((tempPoint.roadDir[0] == 'E' && tempCar.lane == 'L') || 
						(tempPoint.roadDir[0] == 'W' && tempCar.lane == 'R'))
					tempCar.xy = Arrays.copyOfRange(tempPoint.sectors[2][0], 0, 2);
				else if((tempPoint.roadDir[0] == 'N' && tempCar.lane == 'R') || 
						(tempPoint.roadDir[0] == 'S' && tempCar.lane == 'L'))
					tempCar.xy = Arrays.copyOfRange(tempPoint.sectors[0][0], 0, 2);
				else if((tempPoint.roadDir[0] == 'N' && tempCar.lane == 'L') ||
						(tempPoint.roadDir[0] == 'S' && tempCar.lane == 'R'))
					tempCar.xy = Arrays.copyOfRange(tempPoint.sectors[0][2], 0, 2);
				tempCar.dir = tempPoint.roadDir[0];
				//direction same for entrance and exit points
				tempCar.nextPoint = (tempPoint.nextStreet == null)? 
						tempPoint.nextAvenue : tempPoint.nextStreet;
				//tempCar.phase = 'M'; fixed in enterGrid()
				tempCar.enterGrid();
				if(tempCar.dir == 'N' || tempCar.dir =='S') {
					tempCar.nextPoint.comingCars[1]++;
					if(tempCar.nextPoint == tempCar.turningPoint1)
						tempCar.nextPoint.expectedTurningCars[1]++;
					else
						tempCar.nextPoint.expectedStraightCars[1]++;
				} else if(tempCar.dir == 'E' || tempCar.dir == 'W') {
					tempCar.nextPoint.comingCars[0]++;
					//update each counter separately for streets and avenues
					//not correct -- update straight and turning
					if(tempCar.nextPoint == tempCar.turningPoint1)
						tempCar.nextPoint.expectedTurningCars[0]++;
					else
						tempCar.nextPoint.expectedStraightCars[0]++;
				}
				Car.mCarCount++;
			}
			wait = false;
		}
		for(Entry<char[], Car> entry1 : Car.getEntrySet()) {
			tempCar = entry1.getValue();
			if(tempCar.phase != 'M') continue;		//if car not moving
			if(tempCar.dir == 'N' || tempCar.dir == 'W')
				distance = Integer.MIN_VALUE;
			else if(tempCar.dir == 'S' || tempCar.dir == 'E')
				distance = Integer.MAX_VALUE;
			for(Entry<char[], Car> entry2 : Car.getEntrySet()) {
				tempCar1 = entry2.getValue();
				if(tempCar == tempCar1) continue;
				if(tempCar1.phase != 'M') continue;	//if car not moving
				//if(tempCar.road != entry1.getValue().road) continue;
				tempDistance = tempCar.distance(tempCar1);
				/*System.out.print(tempCar.carID);
				System.out.print("\t");
				System.out.print(entry1.getValue().carID);
				System.out.print("\t"+distance+"\t"+tempDistance);*/
				if(tempCar.dir == 'N' || tempCar.dir == 'W') {			//negative directions
					if(tempDistance < 0) {
						if(tempDistance > distance) {
							distance = tempDistance;
							inFront = tempCar1;
						}
						//distance = Math.max(distance, tempDistance);
					}
				} else if(tempCar.dir == 'S' || tempCar.dir == 'E') {	//positive directions
					if(tempDistance > 0) {
						if(tempDistance < distance) {
							distance = tempDistance;
							inFront = tempCar1;
						}
						//distance = Math.min(distance, tempDistance);
					}
				}
				//System.out.println("\t"+distance);
			}
			//Making distance = 0 if it equals Math.abs(Integer.MIN_VALUE)
			distance = (distance==Integer.MIN_VALUE)? Integer.MAX_VALUE:Math.abs(distance);
			inFront = (distance == Integer.MAX_VALUE)? null : inFront;
			tempCar.moveXY(distance, inFront);
			inFront = null;
		}
		//repaint();
		/*wait = false;
		for(Map.Entry<char[], Car> entry : Car.getEntrySet()) {
			wait = entry.getValue().phase != 'S';
			if(wait) break;
		}
		if (!wait && Car.carCount != 0) {
			this.stop();
			System.out.println("\nExecution stopped");
			return;
		}*/
	}
	@Override
    public void update(Graphics g) {
        // Check if the offscreen image has been created
        if (offscreen == null) {
            offscreen = createImage(getWidth(), getHeight());
            if (offscreen != null) {
                offgraphics = offscreen.getGraphics();
            }
        }

        // Ensure the offscreen graphics context is valid
        if (offgraphics != null) {
            // Clear the offscreen image
            offgraphics.clearRect(0, 0, getWidth(), getHeight());

            // Call paint with the offscreen graphics context
            paint(offgraphics);

            // Draw the offscreen image to the screen
            g.drawImage(offscreen, 0, 0, this);
        }
    }

	
	public void paint(Graphics g) {
		Dimension d = new Dimension(Road.xAccumulativePosition, 
				Road.yAccumulativePosition);
		if (offscreen == null) {
				//for window resizing
				offscreen = createImage(d.width, d.height);
				offgraphics = offscreen.getGraphics();
		}
		offgraphics.setColor(Color.black);
		offgraphics.fillRect(0, 0, d.width, d.height);
		paintRoad(offgraphics);
		paintLights(offgraphics);
		paintCars(offgraphics);
		g.drawImage(offscreen, 0, 0, null);
	}
	
	private void paintRoad(Graphics g){
		Dimension d = getSize();
		Road tempRoad;
		int roadCoord = 1+(int)(1.5*(Frame.carWidth+Frame.Clearance));
		int roadWidth = 2+3*(Frame.carWidth+Frame.Clearance);
		for(Map.Entry<char[], Road> entry : Road.getEntrySet()) {
			tempRoad = entry.getValue();
			if(tempRoad.roadType == 'S') {
				//Drawing streets
				g.setColor(Color.gray);
				g.fillRect(0, tempRoad.sectors[1] - roadCoord, d.width, roadWidth);
				g.setColor(Color.WHITE);
				if(tempRoad.roadDir == 'E')
					g.drawImage(eastImg, Road.xAccumulativePosition-30, 
							tempRoad.sectors[1] -8, null);
				else if(tempRoad.roadDir == 'W')
					g.drawImage(westImg, 0, tempRoad.sectors[1]-8, null);
			} else if(tempRoad.roadType == 'A') {
				//Drawing avenues
				g.setColor(Color.gray);
				g.fillRect(tempRoad.sectors[1] - roadCoord, 0, roadWidth, d.height);
				g.setColor(Color.WHITE);
				if(tempRoad.roadDir == 'N')
					g.drawImage(northImg, tempRoad.sectors[1]-8, 
							0, null);
				else if(tempRoad.roadDir == 'S')
					g.drawImage(southImg, tempRoad.sectors[1]-8, 
							Road.yAccumulativePosition-30, null);
			}
		}
		int yellowLine = 1 + (int)(0.5*(Frame.carWidth+Frame.Clearance));
		for(Map.Entry<char[], Road> entry : Road.getEntrySet()) {
			tempRoad = entry.getValue();
			if(tempRoad.roadType == 'S') {
				//Drawing streets
				g.setColor(Color.yellow);
				g.drawLine(0, tempRoad.sectors[1]-yellowLine, d.width, 
						tempRoad.sectors[1]-yellowLine);
				g.drawLine(0, tempRoad.sectors[1]+yellowLine, d.width, 
						tempRoad.sectors[1]+yellowLine);
			} else if(tempRoad.roadType == 'A') {
				//Drawing avenues
				g.setColor(Color.yellow);
				g.drawLine(tempRoad.sectors[1]-yellowLine, 0, 
						tempRoad.sectors[1]-yellowLine, d.height);
				g.drawLine(tempRoad.sectors[1]+yellowLine, 0, 
						tempRoad.sectors[1]+yellowLine, d.height);
			}
		}
	}
	
	private void paintLights(Graphics g) {
		//draw traffic lights at intersections as green initial
		TrafficPoint tempPoint;
		for(Map.Entry<char[], TrafficPoint> entry : TrafficPoint.getEntrySet()) {
			tempPoint = entry.getValue();
			if(tempPoint.control[0] != 'E') {		//not entrance nor exit
				switch(tempPoint.control[1]) {
					case 'R':	g.setColor(Color.red);
								break;
					case 'G':	g.setColor(Color.green);
								break;
					case 'Y':	g.setColor(Color.yellow);
								break;
				}
				if(tempPoint.roadDir[1] == 'N') {
					g.fillRect(tempPoint.sectors[1][1][0]-3*Frame.carWidth, 
							tempPoint.sectors[1][1][1]-4*Frame.carWidth, 
							6*Frame.carWidth, Frame.carWidth);
				} else if(tempPoint.roadDir[1] == 'S') {
					g.fillRect(tempPoint.sectors[1][1][0]-3*Frame.carWidth, 
							tempPoint.sectors[1][1][1]+3*Frame.carWidth, 
							6*Frame.carWidth, Frame.carWidth);
				}
				switch(tempPoint.control[0]) {
					case 'R':	g.setColor(Color.red);
								break;
					case 'G':	g.setColor(Color.green);
								break;
					case 'Y':	g.setColor(Color.yellow);
								break;
				}
				if(tempPoint.roadDir[0] == 'E') {
					g.fillRect(tempPoint.sectors[1][1][0]+3*Frame.carWidth, 
							tempPoint.sectors[1][1][1]-3*Frame.carWidth, 
							Frame.carWidth, 6*Frame.carWidth);
				} else if(tempPoint.roadDir[0] == 'W') {
					g.fillRect(tempPoint.sectors[1][1][0]-4*Frame.carWidth, 
							tempPoint.sectors[1][1][1]-3*Frame.carWidth, 
							Frame.carWidth, 6*Frame.carWidth);
				}
			}
		}
	}
	
	private void paintCars(Graphics g) {
		Car tempCar;
		int HLength = 1+(int)(Frame.carLength/2);
		int HWidth = 1+(int)(0.5*(Frame.carWidth-Frame.Clearance));
		
		for(Entry<char[], Car> entry1 : Car.getEntrySet()) {
			tempCar = entry1.getValue();
			if(tempCar.phase != 'M') continue;
			/*switch(tempCar.remainingTurns) {
			case 0: g.setColor(new Color(0, 255, 255));
					break;
			case 1: g.setColor(new Color(0, 255, 127));
					break;
			case 2: g.setColor(new Color(0, 255, 0));
					break;
			}*/
			if(tempCar.convoy == null) g.setColor(Color.CYAN);
			else if(tempCar == tempCar.convoy.listOfCars[0]) g.setColor(Color.ORANGE);
			else g.setColor(Color.MAGENTA);
			if(tempCar.dir == 'E' || tempCar.dir == 'W')
				g.fillRect(tempCar.xy[0]-HLength, tempCar.xy[1]-HWidth, 
						Frame.carLength, Frame.carWidth);
			else if(tempCar.dir == 'N' || tempCar.dir == 'S')
				g.fillRect(tempCar.xy[0]-HWidth, tempCar.xy[1]-HLength, 
						Frame.carWidth, Frame.carLength);
		}
	}
	
	private void loadImages() {
		northImg = new ImageIcon("images/north.png").getImage();
        southImg = new ImageIcon("images/south.png").getImage();
        eastImg = new ImageIcon("images/east.png").getImage();
        westImg = new ImageIcon("images/west.png").getImage();
	}
	
	@Override   
    public Dimension getPreferredSize() {
        return new Dimension(Road.xAccumulativePosition, 
				Road.yAccumulativePosition);
    }
	
	/*@Override
	public void run() {
		while (isRunning) {
			relax();
			repaint();
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	public void start() {
		gridPaint = new Thread(this);
		isRunning = true;
		gridPaint.start();
	}
	
	public void stop() {
		gridPaint.interrupt();
		isRunning = false;
	}*/
}
