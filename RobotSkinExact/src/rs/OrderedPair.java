package rs;

public class OrderedPair implements Comparable<OrderedPair> {
	public int i;
	public int j;
	public OrderedPair(int ni, int nj) {
		this.i=ni;
		this.j=nj;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		result = prime * result + j;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderedPair other = (OrderedPair) obj;
		if (i != other.i)
			return false;
		if (j != other.j)
			return false;
		return true;
	}
	
	public String toString(){
		return "("+i+","+j+")";
	}
	@Override
	public int compareTo(OrderedPair o) {
		if(i < o.i) return -1;
		if(i==o.i && j<o.j) return -1;
		if(i==o.i && j==o.j) return 0;
		return 1;
	}
	
	
	
}
