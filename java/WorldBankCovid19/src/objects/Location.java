package objects;

import java.util.ArrayList;

/**
 * A generic holder for spatial data. Locations keep track of the spatial location within which they exist.
 * TODO add relationships between/among locations.
 * 
 * @author swise
 */
public class Location {
	
	String myId;
	Location mySuperLocation; // the Location within which this Location exists
	ArrayList <Person> personsHere;
	
	
	// CONSTRUCTORS
	
	public Location(String id, Location mySuper){
		myId = id;
		mySuperLocation = mySuper;
		personsHere = new ArrayList <Person> ();
	}
	
	public Location(Location mySuper){
		this("", mySuper);
	}

	public Location(String id){
		this(id, null);
	}
	
	public Location(){
		this("", null);
	}
	

	/**
	 * Add a person to the Location. Returns false if the Person is already there.
	 * @param p The Person to add to the Location.
	 * @return whether addition was successful.
	 */
	public boolean addPerson(Person p){
		if(personsHere.contains(p))
			return false;
		return personsHere.add(p);
	}
	
	public boolean removePerson(Person p){
		return personsHere.remove(p);
	}
	
	public Location getSuper(){
		return mySuperLocation;
	}
	
	public String getId(){
		return myId;
	}
	
	public Location getRootSuperLocation(){
		Location l = this;
		while(l.getSuper() != null)
			l = l.getSuper();
		return l;
	}
	
	public ArrayList <Person> getPeople(){
		return personsHere;
	}
}