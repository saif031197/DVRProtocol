public class RTblEntry implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String dst;
	private String nxtHop;
	private double cst;
	private String src;
	
	
	public RTblEntry(String src, String dst, String nxtHop, Double cst) {
		this.src = src;
		this.dst = dst;
		this.nxtHop = nxtHop;
		this.cst = cst;
	}
	public void setSource(String source) {
		this.src = source;
	}

	public String getDest() {
		return dst;
	}

	public void setDest(String dest) {
		this.dst = dest;
	}

	public String getNextHop() {
		return nxtHop;
	}
	public String getSource() {
		return src;
	}

	

	public void setNextHop(String nextHop) {
		this.nxtHop = nextHop;
	}

	public double getCost() {
		return cst;
	}

	public void setCost(double cost) {
		this.cst = cost;
	}

}
