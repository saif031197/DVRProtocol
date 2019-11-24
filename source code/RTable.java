import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RTable implements java.io.Serializable {

	/**
	 * 
	 */
	private HashMap<String, Double> orgNodes = new HashMap<String, Double>();
	//List of original nodes.
	private ArrayList<String> immNodes = new ArrayList<String>();
	//Array List maintaining immediate neighbours list
	private static final long serialVersionUID = 1L;
	private ArrayList<RTblEntry> table = new ArrayList<RTblEntry>();
	private double maxHop = 16.0; //Setting max hop as 16 to eliminate count to infinty
	private String rname;
	private transient BufferedReader bufferedRd;
	private String fname;

	public String getRouterName() {
		return rname;
	}

	
	public ArrayList<RTblEntry> getTable() {
		return table;
	}

	public void setTable(ArrayList<RTblEntry> table) {
		this.table = table;
	}

	public RTable() {

	}
	public void setRouterName(String routerName) {
		this.rname = routerName;
	}

	public ArrayList<String> getImmediateNeighbors() {
		return immNodes;
	}

	public void setImmediateNeighbors(ArrayList<String> immediateNeighbors) {
		this.immNodes = immediateNeighbors;
	}


	public RTable(RTable instance) {
		this.table = instance.table;
	}

	//construct table from file
	public void firstRouteTable(String inputfName) throws IOException {
		RTblEntry rObj;//First Entry
		String[] fileParts = inputfName.split(Pattern.quote("."));
		String inputrouterName = String.valueOf(fileParts[0].charAt(fileParts[0].length()-1));
		//The file that is called from the main is  the routerName
		this.fname = inputfName;
		this.rname = inputrouterName;
		//Setting the cost of the  router who is running this to 0
		rObj = new RTblEntry(inputrouterName, inputrouterName, "-", 0.0);
		immNodes.add(inputrouterName);
		table.add(rObj);//First entry in a routing table is that of itself which is 0.
		//Setting the cost of the  router who is running this to 0
		 orgNodes.put(inputrouterName, 0.0);//First entry of the node.

		//The other entries are its immediate neighbours
		FileReader file = new FileReader(new File(inputfName));		
		bufferedRd = new BufferedReader(file);
		String line;
		while((line = bufferedRd.readLine())!=null) {
			String[] lineParts = line.split("\\s+");
			//Each entry in the file contains two fields the name of the router and cost from current router 
			if(lineParts.length == 2) {
				String dest = lineParts[0];
				Double cost = Double.parseDouble(lineParts[1]);
				rObj = new RTblEntry(inputrouterName, dest, dest, cost);//We need to add the entries in the file as the router's name As for now,
				//this is the only information it has.
				immNodes.add(dest);
				table.add(rObj);
				 orgNodes.put(dest, cost);
			}
		}
	}
	//This is the method that gets called once a new node sends it routing table
	public synchronized void updRtrTbl(RTable mytable, RTable rcvdtable) {
		//Getting a list of my own neighbours
		ArrayList<String> ownNbours = new ArrayList<String>(mytable.getImmediateNeighbors());
//checking for new entries in the routing table and updating accordingly
ArrayList<String> recvDNbours = new ArrayList<String>(rcvdtable.getImmediateNeighbors());

for(String neighbor : recvDNbours) {
	//Adding an entry if the I donot have data on the recived table
	if(!ownNbours.contains(neighbor)) {
		//Creating a route table entry for this new neighbour information recieved.
		RTblEntry rObj = new RTblEntry(mytable.getRouterName(), neighbor, "-", maxHop);
		mytable.table.add(rObj);
		mytable.immNodes.add(neighbor);
	}
}
 //Change the distance vector of the table received by adding the distance from my own node to all other nodes.
RTable recvAndChanged = new RTable(rcvdtable);
Double addcost = 0.0;
//For loop for getting cost of each table
for (RTblEntry etr : rcvdtable.getTable()) {
	if(etr.getDest().equals(mytable.getRouterName())) {
		addcost = etr.getCost();
		break;
	}
}

//For Setting the new modified table as a sum of our cost to the node +cost to each neighbour
for (RTblEntry etr : recvAndChanged.getTable()) {
	etr.setNextHop(etr.getSource());
	Double costOriginal = etr.getCost();
	etr.setCost(costOriginal+addcost);
}

//Applying a fix to the count to infinity problem by making sure that if the next node in table received is the router receiving it.
//Set cost as infinty.
//Here max_value is infinity.
for (RTblEntry etr : recvAndChanged.getTable()) {
	if(etr.getNextHop().equals(mytable.getRouterName())) {
		etr.setCost(Double.MAX_VALUE);
	}
}

//This is the method that is used for learning and modifying our existing routing table
for (RTblEntry recventry : recvAndChanged.getTable()) {
	String destRecv = recventry.getDest();
	String hopRecv = recventry.getNextHop();
	Double costRecv = recventry.getCost();
	//Loop for checking with our own table
	for (RTblEntry ownentry : mytable.getTable()) {
		//If destination and source is same.
		if(ownentry.getDest().equals(ownentry.getSource())) {
			continue;
		}
		//This is the main if condition where we compare costs between modified and existing tables 
		//To find the least cost.
		if((ownentry.getDest().equals(destRecv))) {
			if(!ownentry.getNextHop().equals(hopRecv)) {
				if (ownentry.getCost() > costRecv) {
					ownentry.setCost(costRecv);
					ownentry.setNextHop(hopRecv);
				}
			}
			else {
				ownentry.setCost(costRecv);
				ownentry.setNextHop(hopRecv);
			}
		}
	}
}
	}

	//Display method for each router output
	public synchronized void displayTable(ArrayList<RTblEntry> t) {
		for (RTblEntry etr : t) {
			System.out.println("Best Path "+etr.getSource()+"-"+etr.getDest()+": the next hop is "+etr.getNextHop()+"--cost-- "+etr.getCost());
		}
	}

	//Check if the link is changed.This method is for new routing table objects to be created 
	//Once route changes 
	public synchronized void chckLinkcst() throws NumberFormatException, FileNotFoundException, IOException {
		//Saving file data to new routing object
		RTable newRTable = new RTable();
		RTblEntry rObj1;
		//A list of changed routers who have new table entries.
		ArrayList<String> listOfchangedRouters = new ArrayList<String>();
		String[] fileParts = this.fname.split(Pattern.quote("."));
		String rtName = String.valueOf(fileParts[0].charAt(fileParts[0].length()-1));
		//entry for the same router
		rObj1 = new RTblEntry(rtName, rtName, "-", 0.0);
		//Adding immediate neighbours to new routing table objects
		newRTable.table.add(rObj1);
		//reading the cost and node entries
		FileReader file = new FileReader(new File(this.fname));		
		bufferedRd = new BufferedReader(file);
		String line;
		while((line = bufferedRd.readLine())!=null) {
			String[] lineParts = line.split("\\s+");
			if(lineParts.length == 2) {
				String dest = lineParts[0];
				Double cost = Double.parseDouble(lineParts[1]);
				rObj1 = new RTblEntry(rtName, dest, dest, cost);
				//Adding source and destination to new table
				newRTable.table.add(rObj1);
			}
		}
        //If new costs are different from exisisting costs then change them 
		//and add it to listOfchangedRouters
		
		for (Map.Entry<String, Double> entr :  orgNodes.entrySet()) {
			for (RTblEntry newentry : newRTable.getTable()) {
				if(newentry.getDest().equals(entr.getKey())) {
					if(Double.compare(newentry.getCost(), entr.getValue()) != 0) {
						System.out.println("Link has been changed");
						listOfchangedRouters.add(entr.getKey());
					}
				}
			}
		}
        //If we have a list of routers who have changed then change their cost to new cost.   
				for (RTblEntry entr : table) {
			for (RTblEntry newentry : newRTable.getTable()) {
				if(newentry.getDest().equals(entr.getDest())) {
					if(listOfchangedRouters.contains(newentry.getDest())) {
						entr.setCost(newentry.getCost());
						 orgNodes.put(newentry.getDest(), newentry.getCost());
						System.out.println("Change in Link State Fixed!");
					}
				}
			}
		}
	}
}
