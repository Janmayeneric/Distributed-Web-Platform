package config;

import java.util.HashSet;
import java.util.Random;

/**
 * Here is some method to create the random string 
 * @author 10130
 *
 */
public class Generator {
	Random ran = new Random();
	/**
	 * to return a random character 
	 * it also include the symbol in ASCII table, but no space
	 * @return random character
	 */
	public String character() {
		int lower =50;
		int upper = 10;
		return Character.toString((char)(lower + ran.nextInt(upper)));
	}
	
	/**
	 * to return a random string
	 * @param min minimum length of string
	 * @param max maximum length of string
	 * @return random string have length between min and max
	 */
	public String string(int min, int max) {
		int length = min + ran.nextInt(max - min + 1);
		StringBuilder result = new StringBuilder();
		for(int i=0; i < length; i++) {
			result.append(character());
		}
		return result.toString();
	}
	
	/**
	 * to return the list of distinct string 
	 * @param number maximum number of the names want to generate
	 * @param min minimum length of each string
	 * @param max maximum length of each string
	 * @return
	 */
	public HashSet<String> stringsSet(int number,int min, int max) {
		int count = 0;
		HashSet<String> set= new HashSet<String>();
		while(count < number) {
			if(set.add(string(min,max))) {
				count ++;
			}
		}
		return set;
	}
}
