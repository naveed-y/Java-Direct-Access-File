package p3_DirectAccessFile;

public class Disk{
	private int sectorCount;   // sectors on the disk
	private int sectorSize;    // characters in a sector
	private char[][] store;    // all disk data is stored here

	// for default sectorCount and sectorSize
	public Disk(){   
		
		// Default values
		this.sectorCount = 10000;
		this.sectorSize = 512;
		
		// Create 2d char array using default values
		store = new char[sectorCount][sectorSize];
		// Set all elements to null character
		for (int r=0; r<store.length; r++){
			for(int c=0; c<store[r].length; c++){
				store[r][c] = '\000';
			}
		}
	}

	// for any sectorCount and sectorSize
	public Disk(int sectorCount, int sectorSize){	
		
		// Overload the constructor so that the values can also be passed in
		this.sectorCount = sectorCount;
		this.sectorSize = sectorSize;
		
		// Same construction as above
		store = new char[sectorCount][sectorSize];
		for (int r=0; r<store.length; r++){
			for(int c=0; c<store[r].length; c++){
				store[r][c] = '\000';
			}
		}
	}

	// sector to buffer
	public void readSector(int sectorNumber, char[] buffer){	
		// Loop through array, store all values in buffer
		// .. Since buffer is a reference type, any local changes will persist
		// .. on the DirectFile
		for(int i=0; i<sectorSize; i++){
			buffer[i] = store[sectorNumber][i];
		}
	}                                                        

	// buffer to sector 
	public void writeSector(int sectorNumber, char[] buffer){  
		
		// opposite of above method
		for(int i=0; i<sectorSize; i++){
			store[sectorNumber][i] = buffer[i];
		}
	}                                                        

	// getter for sectorCount
	public int getSectorCount(){
		return sectorCount;
	}

	// getter for sectorSize
	public int getSectorSize(){
		return sectorSize;
	}
}