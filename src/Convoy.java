import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Convoy {
	private static HashMap <Integer, Convoy> allConvoys = new HashMap<Integer, Convoy>();
	private static int numOfConvoys = 0;
	
	protected Car[] listOfCars = new Car[15];
	protected int carsInConv = 0;
	protected Integer convID;
	
	public static Set<Map.Entry<Integer, Convoy>> getEntrySet() {
		return allConvoys.entrySet();
	}
	
	public Convoy(Car firstInConv) {
		this.listOfCars[carsInConv] = firstInConv;
		carsInConv++;
//		convID = new Integer(++numOfConvoys);
		convID = Integer.valueOf(++numOfConvoys);

		allConvoys.put(convID, this);
	}
	
	public Convoy joinConvoy(Car inConv) {
		if(carsInConv >= listOfCars.length || inConv.dir != this.listOfCars[0].dir)
			return null;
		else {
			for(int i=0; i<carsInConv; i++) {
				if(Math.abs(this.listOfCars[i].distance(inConv)) <= Frame.carLength) return null;
			}
			this.listOfCars[this.carsInConv] = inConv;
			this.carsInConv++;
			return this;
		}
	}
	
	public void changeSpeedForAll(int[] dxy) {
		int comDist = 0;
		for(int i=1; i<this.carsInConv; i++) {
			if(this.listOfCars[i] != null)
				comDist += Math.abs(this.listOfCars[i-1].distance(this.listOfCars[i]));
		}
		if(comDist < (Frame.carLength*this.carsInConv)) {
			this.leave();
		}
		for(int i=1; i<this.carsInConv; i++) {
			if(this.listOfCars[i] != null) {
				this.listOfCars[i].dxy = Arrays.copyOfRange(dxy, 0, 2);
			}
		}
	}
	
	public Car lastCar() {
		return this.listOfCars[this.carsInConv];
	}
	
	public void leave() {
		int i=0;
		
		for(i=0; i<this.listOfCars.length; i++) {
			if(this.listOfCars[i] != null) {
				this.listOfCars[i].convoy = null;
				this.listOfCars[i] = null;
			}
		}
		this.carsInConv = 0;
		allConvoys.remove(this.convID);
		
		/*while(this.listOfCars[i] != wasIn) {
			i++;
			if(i>=this.listOfCars.length) break;
		}
		if(i>=this.listOfCars.length) return;
		if(i>0) {
			this.carsInConv = i;
			this.backXY = this.listOfCars[i].xy;
		}
		while(i<this.listOfCars.length) {
			if(this.listOfCars[i] != null) {
				this.listOfCars[i].convoy = null;
				this.listOfCars[i] = null;
			}
			i++;
		}*/
	}
}
