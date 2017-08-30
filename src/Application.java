// Naveed Yahya-Zadeh
// Application.java

package p3_DirectAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Application {


	// Direct access file that performs all reading and writing operations to the disk
	private static DirectFile df;
	// Size of the each sector and number of sectors in the disk that will be used
	private static int sectorCount = 2000, sectorSize = 512;
	// Size of the record and its unique fields
	private static int recordSize = 60, keySize = 27, countrySize = 27, altitudeSize = 6;
	// Initial location in disk where records are saved
	private static int firstAllocated = 1024, bucketsAllocated = 600;
	// Scanner for user input
	private static final Scanner sc = new Scanner(System.in);
	
	
	
	public static void main(String[] args) {
		// Create the DAF using the specific arguments for the mountain scenario
		df = new DirectFile(new Disk(sectorCount, sectorSize), recordSize, keySize, 
				 firstAllocated, bucketsAllocated);
		// Import all the mountain data from the file
		mountainData();
		// Open the interactive menu
		menu();
		
	}
	
	public static void menu(){
		while(true){
			// Display a looping menu with a working user interface
			System.out.println("Menu:");
			System.out.println("insert record     i             find record       f");
			System.out.println("quit              q");
			System.out.print("-> "); // Prompts user to enter something
			String in = sc.nextLine(); // Scans the next token of the input each time the loop occurs
			switch(in){
			case "i":
				// Build and insert a record supplied by the user
				String record = buildRecord(0,"");
				insertData(record);
				continue;
			case "f":
				// Find a record with a key supplied by a user
				String key = buildRecord(1,"");
				searchForKey(key);
				continue;
			case "q": // quit
				return;
			}
		}
		
	}
	
	// This method takes in a string from a file/user, and rewrites it in the
	// .. proper insertable record formatting (still using '#' symbols)
	// .. the insertData function actually handles the passing of the record
	// .. into the disk
	private static String buildRecord(int keyOrRec /*record = 0, key = 1*/, 
			String userOrFile /*User = "", File: pass in a record*/){
		
		// Build a return string from its components, mountain (key), 
		// ..country/altitude (record)
		String s;
		String mountain;
		String country ="";
		String altitude="";
		
		// If the user is to input these values, this block runs
		// .. data is scanned in from system.in and entered into the variables
		if (userOrFile.equals("")){
			System.out.print("Name of mountain: ");
			mountain = sc.nextLine();
			// If method requires an entire record, user  must supply country
			// .. and altitude as well as mountain
			if (keyOrRec==0){
				System.out.print("Name of country: ");
				country = sc.nextLine();
				System.out.print("Altitude of mountain: ");
				altitude = sc.nextLine();
			}
			
		}
		
		// If data is being passed in from a file, chop the String up via
		// .. the indices of '#' 
		else{
			s = userOrFile;
			mountain =  s.substring(0,s.indexOf('#'));
			s = s.substring(s.indexOf('#')+1);
			country =  s.substring(0,s.indexOf('#'));
			s = s.substring(s.indexOf('#')+1);
			altitude = s;
		}
		
		// If any of the values are longer than the required lengths for the
		// properly formatted recorded, splice them to the required length
		mountain = mountain.length() > keySize ? mountain.substring(0,keySize) : mountain;
		if (keyOrRec == 0){
			country = country.length() > countrySize ? country.substring(0,countrySize) : country;
			altitude = altitude.length() > altitudeSize ? altitude.substring(0,altitudeSize) : altitude;
			
			// If a full record is desired, rebuild the String accordingly
			s = mountain + "#" + country + "#" + altitude;
			
		}
		else{
			s = mountain;
		}
		return s;
		
	}
	
	// Collect data from mountain file, reformat the data, and insert it into disk
	private static void mountainData(){
		// gets data from file
		Scanner sc;
		
		// Open file
		try {
			sc = new Scanner(new File("mountains.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		// buildRecord() to reformat, insertData() to insert into disk
		while(sc.hasNext()){			
			insertData(buildRecord(0,sc.nextLine()));
		}
		sc.close();
	}
	
	// Rebuilds the String into a character array and pushes into the disk using
	// .. DirectFile's insertRecord() method
	private static void insertData(String s){
		
		// this is the return array
		char[] record = new char[recordSize];
		
		// j used to iterate through String s
		int j = 0;
		
		// hitPound tells the for loop to pause iterating through String s
		// .. when '#' is reached
		boolean hitPound = false;
		
		// i used to iterate through char[] record
		for(int i=0; i<recordSize; i++){
			
			// Watch for StringIndexOutOfPounds exceptions. This occurs when j iterates
			// .. too far into 's'. In this case, we simply append nulls to the end of char[]
			// .. in the mountain scenario, null characters are appended at the end of altitude
			// .. until the final length is 6
			try{
				// Search for '#'s, update hitPound accordingly
				if (s.charAt(j) == '#'){
					hitPound = true;
				}
				else{
					hitPound = false;
				}
				// If not a pound value, pass the charAt the String to the char array
				if(!hitPound){
					record[i] = s.charAt(j);
					// If at the end of key or country index in char array, iterate j twice
					// instead of once
					if (i == (keySize)-1 /* i == 26 */|| i == (keySize*2)-1 /* i == 53*/){
						j++;
					}
					j++;
				}
				else{
					// If we are at a pound but we are at end of key/country index, still iterate j
					// .. past the '#' symbol
					if (i == (keySize)-1 /* i == 26 */|| i == (keySize*2)-1 /* i == 53*/){
						hitPound = false;
						j++;
					}
					// Appen null charcters to char array while at a pound
					else{
						record[i] = '\000';
					}
					
				}
			}
			catch (StringIndexOutOfBoundsException e){
				record[i] = '\000';
			}

			
			
			
			
		}
		df.insertRecord(record);
		
	}
	
	// Takes in a String input, creates a char[] from the string, and uses df.findRecord
	// .. search the disk for that key. If found, pull from key, reformat, and print to screen
	private static void searchForKey(String s){
		char[] key = new char[recordSize];
		int j = 0; // Used to iterate through String
		// Build key from string
		// i iterates through char array
		for(int i=0; i<recordSize; i++){
			try{
				key[i] = s.charAt(j);
				j++;
			}
			// Append nulls for all the values in array after the key
			catch (StringIndexOutOfBoundsException e){
				key[i] = '\000';
			}
		}
		// Search for key, if found, run following block
		if(df.findRecord(key)){
			boolean hitNull = false; // If hit null, pause iteration for string formatting
			int numCommas = 0; // Counts number of commas for formatting
			
			for (int i=0; i<key.length; i++){
				if(key[i] == '\000'){
					hitNull = true;
				}
				else{
					hitNull = false;
				}
				if(!hitNull){
					// If haven't hit null, print each key to the screen
					System.out.print(key[i]);
					// Following if statement catches special cases for i, and creates appropriate
					// .. formatting (commas, altitude:, or ft.) depending on where we are in the 
					// .. char array
					if (i==key.length-1 || key[i+1] == '\000' || i == keySize-1 || i == keySize+countrySize-1){
						if(numCommas==0){
							System.out.print(", ");
							numCommas++;
						}
						else if(numCommas==1){
							System.out.print(", altitude: ");
							numCommas++;
						}
						else if (numCommas==2){
							System.out.print(" ft.");
						}
					}
				}
				
			}
			// Newline to finish
			System.out.print("\n");
		}
		else{
			System.out.println("Record not found");
		}
		
	}
}
