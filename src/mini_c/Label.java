package mini_c;

/** étiquette */
public class Label {
	
	private static int next = 0;
	
	final String name;
	
	Label() {
		next++;
		this.name = "L" + next;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		Label that = (Label)o;
		return this.name.equals(that.name);
	}
	@Override
	public String toString() {
		return this.name;
	}

}
