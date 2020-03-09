package mini_c;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class Arcs{
	  Set<Register> prefs = new HashSet<>();
	  Set<Register> intfs = new HashSet<>();
}

public class Interference{
	Map<Register, Arcs> graph;
	
	void addPref(Register r1, Register r2) {
		if (this.graph.containsKey(r1) && !this.graph.get(r1).prefs.contains(r2)) {
			this.graph.get(r1).prefs.add(r2);
		}
		else {
			Arcs arcs = new Arcs();
			arcs.prefs.add(r2);
			this.graph.put(r1, arcs);
		}
		if (this.graph.containsKey(r2) && !this.graph.get(r2).prefs.contains(r1)) {
			this.graph.get(r2).prefs.add(r1);
		}
		else {
			Arcs arcs = new Arcs();
			arcs.prefs.add(r1);
			this.graph.put(r2, arcs);
		}
	}
	
	void addIntf(Register r1, Register r2) {
		if (this.graph.containsKey(r1) && !this.graph.get(r1).intfs.contains(r2)) {
			this.graph.get(r1).intfs.add(r2);
		}
		else {
			Arcs arcs = new Arcs();
			arcs.intfs.add(r2);
			this.graph.put(r1, arcs);
		}
		if (this.graph.containsKey(r2) && !this.graph.get(r2).intfs.contains(r1)) {
			this.graph.get(r2).intfs.add(r1);
		}
		else {
			Arcs arcs = new Arcs();
			arcs.intfs.add(r1);
			this.graph.put(r2, arcs);
		}
	}
	
	Interference(Liveness lg){
		for(Map.Entry<Label, LiveInfo> entry : lg.info.entrySet()) {
			if (entry.getValue().instr instanceof ERmbinop && 
					((ERmbinop) entry.getValue().instr).m.equals(Mbinop.Mmov) && 
					!((ERmbinop) entry.getValue().instr).r1.equals(((ERmbinop) entry.getValue().instr).r2)) {
				//We have the case "mov w v"
				Register w = ((ERmbinop) entry.getValue().instr).r1;
				Register v = ((ERmbinop) entry.getValue().instr).r2;
				this.addPref(w,v);
				Iterator<Register> itr = entry.getValue().outs.iterator();
				while (itr.hasNext()) {
					if (!itr.next().equals(w)) {
						this.addIntf(v,itr.next());
					}
				}
			}
			else {
				//Not in case mov w v, add all interference edge
				Iterator<Register> itrDefs = entry.getValue().defs.iterator();
				while (itrDefs.hasNext()) {
					Iterator<Register> itrOuts = entry.getValue().outs.iterator();
					while (itrOuts.hasNext()) {
						this.addIntf(itrDefs.next(),itrOuts.next());
					}
				}
			}
		}
	}
	
	void print() {
	    System.out.println("interference:");
	    for (Register r: graph.keySet()) {
	      Arcs a = graph.get(r);
	      System.out.println("  " + r + " pref=" + a.prefs + " intf=" + a.intfs);
	    }
	  }
	
}
