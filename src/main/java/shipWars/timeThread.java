package shipWars;

public class timeThread extends Thread {
	
	// Main thread method
	@Override
	public void run() {
		System.out.println("New timeThread initiated !" );
		while(true){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Data.error(11);
			}
			shipWars.onMinecraftTick();
		}
	}
}
