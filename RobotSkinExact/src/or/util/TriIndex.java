package or.util;

public class TriIndex {
	public int i1,i2,i3;

	public TriIndex(int i1, int i2, int i3) {
		super();
		this.i1 = i1;
		this.i2 = i2;
		this.i3 = i3;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i1;
		result = prime * result + i2;
		result = prime * result + i3;
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
		TriIndex other = (TriIndex) obj;
		if (i1 != other.i1)
			return false;
		if (i2 != other.i2)
			return false;
		if (i3 != other.i3)
			return false;
		return true;
	}
	
	

}