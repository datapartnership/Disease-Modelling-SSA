package sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import behaviours.InfectiousBehaviourFramework;
import behaviours.MovementBehaviourFramework;
import objects.Household;
import objects.Infection;
import objects.Location;
import objects.Person;
import sim.engine.SimState;

public class WorldBankCovid19Sim extends SimState {

	// the objects which make up the system
	ArrayList <Person> agents;
	ArrayList <Household> households;
	
	ArrayList <Location> districts;
	
	public MovementBehaviourFramework movementFramework;
	public InfectiousBehaviourFramework infectiousFramework;
	public Params params;
	
	/**
	 * Constructor function
	 * @param seed
	 */
	public WorldBankCovid19Sim(long seed, Params params) {
		super(seed);
		this.params = params;
	}
	
	public void start(){
		
		// set up the behavioural framework
		movementFramework = new MovementBehaviourFramework(this);
		infectiousFramework = new InfectiousBehaviourFramework(this);
		
		// load the population
		load_population(params.population_filename);
		
		// if there are no agents, SOMETHING IS WRONG. Flag this issue!
		if(agents.size() == 0) {
			System.out.println("ERROR *** NO AGENTS LOADED");
			System.exit(0);
		}

		// set up the social networks
		InteractionUtilities.create_work_bubbles(this);
		InteractionUtilities.create_social_bubbles(this);

		// set up the infections
		for(int i = 0; i < 5; i++){
			int personIndex = random.nextInt(agents.size());
			Infection inf = new Infection(agents.get(personIndex), null, infectiousFramework.getEntryPoint());
			schedule.scheduleOnce(1, 10, inf);
		}
	}
	
	public void load_population(String agentsFilename){
		try {
			
			// holders for construction
			agents = new ArrayList <Person> ();
			households = new ArrayList <Household> ();
			
			// use a helpful holder to find households by their names
			HashMap <String, Household> rawHouseholds = new HashMap <String, Household> ();
			
			System.out.println("Reading in agents from " + agentsFilename);
			
			// Open the file
			FileInputStream fstream = new FileInputStream(agentsFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader agentData = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// get rid of the header
			s = agentData.readLine(); // TODO use header to specify where everything is, just in case!!!
			
			System.out.print("BEGIN READING IN PEOPLE...");
			
			// read in the raw data
			//int myIndex = 10;
			while ((s = agentData.readLine()) != null ){//&& myIndex > 0) {
				//myIndex--;
				
				// separate the columns from the raw text
				String[] bits = Params.splitRawCSVString(s);
				
				// make sure the larger units are set up before we create the individual

				// set up the Household for the Person
				String hhName = bits[4];
				Household h = rawHouseholds.get(hhName);

				// if the Household doesn't already exist, create it and save it
				if(h == null){
					
					// make sure to create it within the target district
					String myDistrictName = bits[5];
					Location myDistrict = params.districts.get(myDistrictName);
					
					// set up the Household
					h = new Household(hhName, myDistrict);
					rawHouseholds.put(hhName, h);
					households.add(h);
				}
				
				// identify the location in which the person, possibly, works
				
				String economicActivityLocationName = bits[7];
				Location econLocation = params.districts.get(economicActivityLocationName);
				// TODO: they might not work anywhere! Further, they might work in a particular subset of the location!
				
				// set up the person

				// create and save the Person agent
				Person p = new Person(Integer.parseInt(bits[1]), // ID 
						Integer.parseInt(bits[2]), // age
						bits[3], // sex
						bits[6],
						econLocation,
						this
						);
				h.addPerson(p);
				p.setLocation(h);
				p.setActivityNode(movementFramework.getEntryPoint());
				agents.add(p);
				
				// schedule the agent to run at the beginning of the simulation
				this.schedule.scheduleOnce(0, p);
				//this.schedule.scheduleRepeating(p);
				

			}
			
			// clean up after ourselves!
			agentData.close();
							
			System.out.println("FINISHED READING PEOPLE");
		} catch (Exception e) {
			System.err.println("File input error: " + agentsFilename);
		}
	}
	

	void reportOnInfected(){
		String makeTerribleGraphFilename = "/Users/swise/Downloads/nodes_latest.gexf";
		try {
			
			System.out.println("Printing out infects? from " + makeTerribleGraphFilename);
			
			// shove it out
			BufferedWriter badGraph = new BufferedWriter(new FileWriter(makeTerribleGraphFilename));

			//badGraph.write("ID;econ;age;infect;time;source");
			badGraph.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gexf xmlns=\"http://www.gexf.net/1.1draft\" version=\"1.1\">\n" + 
					"<graph mode=\"static\" defaultedgetype=\"directed\">\n" + 
					"<attributes class=\"node\" type=\"static\"> \n" +
				     "<attribute id=\"infected\" title=\"Infected\" type=\"string\"/>\n</attributes>\n");
			badGraph.write("<nodes>\n");
			for(Person p: agents){
				String myStr = p.toString();
				//myStr += ";" + p.getEconStatus() + ";" + p.getAge() + ";" + p.getInfectStatus();
				
				if(p.getInfection() != null){
					Person source = p.getInfection().getSource();
					String sourceName = null;
					if(source != null)
						sourceName = source.toString();
					//myStr += ";" + p.getInfection().getStartTime() + ";" + sourceName;
					myStr = p.getInfection().getBehaviourName();
				}
				else
					//myStr += "Susceptible;;";
					myStr = "Susceptible";
/*				for(Person op: p.getWorkBubble()){
					myStr += ";" + op.toString();
				}
	*/			
				badGraph.write("\t<node id=\"" + p.getID() + "\" label=\"" + p.toString() + 
						"\"> <attvalue for=\"infected\" value=\"" +myStr +  "\"/></node>\n");

				//badGraph.write("\n" + myStr);
			}
			badGraph.write("</nodes>\n");
			badGraph.write("<edges>\n");
			for(Person p: agents){
				int myID = p.getID();
				for(Person op: p.getWorkBubble()){
					badGraph.write("\t<edge source=\"" + myID + "\" target=\"" + op.getID() + "\" weight=\"1\" />\n");
				}
			}
			
			badGraph.write("</edges>\n");
			badGraph.write("</graph>\n</gexf>");
			badGraph.close();
		} catch (Exception e) {
			System.err.println("File input error: " + makeTerribleGraphFilename);
		}
	}
	
	public static void main(String [] args){
		if(args.length < 0){
			System.out.println("usage error");
			System.exit(0);
		}
		
		WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(System.currentTimeMillis(), new Params());
		
		System.out.println("Loading...");

		mySim.start();

		System.out.println("Running...");

		while(mySim.schedule.getTime() < 24 * 7 && !mySim.schedule.scheduleComplete()){
			mySim.schedule.step(mySim);
			double myTime = mySim.schedule.getTime();
			System.out.println("*****END TIME: DAY " + (int)(myTime / 6) + " HOUR " + (int)((myTime % 6) * 4) + " RAWTIME: " + myTime);
		}
		
		mySim.reportOnInfected();
		
		mySim.finish();
		
		System.out.println("...run finished");
		//System.exit(0);
	}
}