// Naveed Yahya-Zadeh
// Application.java

package p3_DirectAccessFile;

public class DirectFile {
	 public DirectFile(Disk disk, int recordSize, int keySize, 
			 int firstAllocated, int bucketsAllocated)
	{
		// Constructor takes in 5 parameters, initializes them below
		this.disk = disk;
		this.recordSize = recordSize;
		this.keySize = keySize;
		this.firstAllocated = firstAllocated;
		this.bucketsAllocated = bucketsAllocated;
		
		// These three fields are built using the values from the other fields
		this.buffer = new char[disk.getSectorSize()];
		this.firstOverflow = firstAllocated+bucketsAllocated;
		this.overflowBuckets = 0;
	}
	public boolean insertRecord(char[] record)
	{
		// pull key from record
		char[] key = pullKey(record);
		// hash the key to create a hash value (the sector number);
		int sectorNumber = hash(key);
		
		// if findRecord() returns true, then the records already exists.
		// .. no insertion is done and the method returns false
		if(findRecord(key)){
			return false;
		}
		
		// copy the desired sector to the buffer
		disk.readSector(firstAllocated+sectorNumber, buffer);
			
		// find the index of the first opening in the sector, or return -1 if sector full
		int firstOpen = firstOpen();
		// Find number of open array elements in sector
		int numOpenSpaces = numOpenSpaces();	
		
		// if sector has sufficient space, we insert the record at the first
		// .. space containing '\000' in the buffer
		if (numOpenSpaces >= recordSize){
			// Loop through record and copy every element to buffer
			for(int i=0; i<recordSize; i++){
				buffer[firstOpen+i] = record[i];
			}
			// Rewrite the updated buffer to the same location in the disk
			disk.writeSector(firstAllocated+sectorNumber, buffer);
		}
		
		// if original sector is full, push to an overflow sector
		else {
			// If there are no overflow buckets in use, create the first
			if (overflowBuckets == 0){
				overflowBuckets++;
			}
			// All the overflow buckets except the last one in the array will
			// .. be full based on the program's algorithm. Therfore, we only
			// .. check the last bucket for space.
			int nonFullBucketNumber = firstOverflow+overflowBuckets-1;
			
			// Read the overflow sector from the disk
			disk.readSector(nonFullBucketNumber, buffer);
			
			// Again, find firstOpen and numOpensSpaces for the new sector in the buffer
			firstOpen = firstOpen();
			numOpenSpaces = numOpenSpaces();
			
			// if there is not enough space, create one more bucket
			if (numOpenSpaces < recordSize){
				
				// Increment number of overflow buckets and sectorNumber that we will read from
				overflowBuckets++;
				nonFullBucketNumber++;
				
				// Read the new sector, and update firstOpen and numOpenSpaces
				//.. (which will be 0 and sectorSize respectively for new sectors)
				disk.readSector(nonFullBucketNumber, buffer);
				firstOpen = firstOpen();
				numOpenSpaces = numOpenSpaces();	
			}
			
			// Write from record to buffer then to sector
			for(int i=0; i<recordSize; i++){
				buffer[firstOpen+i] = record[i];
			}
			disk.writeSector(nonFullBucketNumber, buffer);
		}
		return true;
	}  
	
	// Using a key, find the corresponding record in the hashtable
	public boolean findRecord(char[] record) 
	{
		// pull key from record
		char[] key = pullKey(record);
	
		// hash the key to create a hash value (the sector number);
		int sectorNumber = hash(key);
		
		// read the corresponding sector
		disk.readSector(firstAllocated+sectorNumber, buffer);

		// searchSector takes in the spliced key and the original record, finds the 
		// .. desired key in the hash table, and updates the record parameter if the
		// .. record is found in the specified sector
		if(searchSector(record, key)){
			return true;
		}
		
		// If original sector is full, loop through all the overflow buckets
		if (numOpenSpaces() < recordSize){
			
			for(int i=firstOverflow; i<(firstOverflow+overflowBuckets); i++){
				// Read in each overflow bucket
				disk.readSector(i, buffer);
				// Search for the first opening in the overflow region and
				// .. update the record using searchSector
				if(searchSector(record, key)){
					return true;
				}
			}
		}
		
		// If none of the overflow sectors have the value, return false
		return false;		
	}
	
	private char[] pullKey(char[] record){
		// Create new char[] from the record that solely contains the key
		char[] key = new char[keySize];
		
		// Fill key array with null characters
		for(int i=0; i<keySize; i++){
			key[i] = '\000';
		}
		
		// Loop through record array, pass values into key array until first null char
		// .. (end of key string) is reached
		for(int i=0; i<keySize; i++){
			if (record[i] == '\000'){
				break;
			}
			key[i]=record[i];
		}
		return key;
	}
	
	private boolean searchSector(char[] record, char[] key){
		// search a buffer for a specific key. If found, update corresponding record
		
		// This method runs through the buffer after a new sector is read into it.
		// .. there is a for loop that compares the key to the sector, and if every
		// .. element in the key matches a sub-array in buffer, the record is updated.
		int numCorrect = 0;
		int keyStartIndex = -1;
		
		for(int i=0; i<buffer.length; i++){
			// Search for matches between key and buffer
			if (buffer[i] == key[numCorrect]){
				// numCorrect == number of matches
				if (numCorrect == 0){
					// save first index if a match is found
					keyStartIndex = i;
				}
				// increment numCorrect until it spans the length of the key
				numCorrect++;
				if (numCorrect == keySize){
					// Nested for loop writes the stores record in the buffer to the parameter 'record'
					int ctr = 0;
					for(int j=keyStartIndex; j<keyStartIndex+recordSize; j++){
						record[ctr] = buffer[j];
						ctr++;
					}
					return true;
				}
			}
			else{
				// If any non-match is reached, reset the match counter to 0
				numCorrect = 0;
			}
		}
		return false;
		
		
		
	}
	// The hash function needs a String version of the character array, so this function
	// .. does just that. It also puts everything to uppercase to avoid case sensitivity when
	// .. using this program
	public String toString(char[] c){
		String s = "";
		for (int i=0; i<c.length; i++){
			s += c[i];
		}
		return s.toUpperCase();
	}
	
	// Takes in a key, and returns a hash value that corresponds with that key.
	// .. any time the same character array is passed in, the same hash value is 
	// .. produced. This is useful for finding records and for storing them
	private int hash(char[] key)
	{
		return (Math.abs((toString(key)).hashCode())%bucketsAllocated);
	}
	
	// Find the first opening in a sector
	private int firstOpen(){
		
		// We loop recordSize spaces at a time, because if the first element in each
		// .. record-sized sub-array is non-null, then we know that that segment
		// .. already contains a record
		for(int i=0; i<buffer.length-recordSize+1; i+=recordSize){
			
			// Search for a null character
			if(buffer[i] == '\000'){
					return i;	
			}
		}
		return -1;
	}
	
	// Find if there is enough space to insert a record
	private int numOpenSpaces(){
		int numOpenSpaces = 0;
		int ctr = 0;
		// Loop through for null characters, run a counter. Once the counter
		// .. reaches the size of a record, updates numOpenSpaces
		for(int i=0; i<buffer.length; i++){
			if(buffer[i] == '\000'){
				ctr++;
			}
			else{
				ctr = 0;
			}
			if (ctr == recordSize){
				numOpenSpaces+=recordSize;
			}
		}
		return numOpenSpaces;
	}

	
	private Disk disk;             // disk on which the file will be written
	private char[] buffer;         // disk buffer
	private int recordSize;        // in characters
	private int keySize;           // in characters
	// private int recordsPerSector; Not used. Instead array elements per sector are measured
	private int firstAllocated;    // sector number
	private int bucketsAllocated;  // buckets (i.e. sectors) originally allocated   
	private int firstOverflow;     // sector number
	private int overflowBuckets;   // count of overflow buckets in use
}
