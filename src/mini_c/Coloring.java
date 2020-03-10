package mini_c;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Coloring {
	Map<Register, Operand> colors = new HashMap<>();
	int nlocals = 0; // nombre d'emplacements sur la pile
	
	Coloring(Interference ig){
		//Filling the todo table with every pseudo register
		LinkedList<Register> todo = new LinkedList<>();
		Map<Register, LinkedList<Operand>> possibleColors = new HashMap<>();
		
		//Initialization
		for(Map.Entry<Register, Arcs> entry : ig.graph.entrySet()) {
			Register r = entry.getKey();
			Arcs edges = entry.getValue();
			if (r.isPseudo()) 
			{
				todo.add(r);
				LinkedList<Operand> available = new LinkedList<>();
				for(Register rAlloc : Register.allocatable) {
					if (!edges.intfs.contains(rAlloc)) {		
						available.add(new Reg(rAlloc));
					}
				}
				possibleColors.put(r,available);
			}
		}
		
		//Coloration
		while (!todo.isEmpty()) {
			boolean colored = false;
			// 1. Look for nodes with one color possible and at least one preference
			Register oneColor = null;
			for (Register r : todo) {
				if (possibleColors.get(r).size() == 1) {
					//Note this register : could be used in 2.
					oneColor = r;
					if (!ig.graph.get(r).prefs.isEmpty()) {
						Operand color = possibleColors.get(r).pop();
						this.colors.put(r,color);
						for (Register rNeighbour : ig.graph.get(r).intfs) {
							if (possibleColors.containsKey(rNeighbour)) {							
								possibleColors.get(rNeighbour).remove(color);
							}
						}
						colored = true;
						todo.remove(r);
						break;
					}
				}
			}
			// 2. If we had a node with only one color but no preference, use it
			if(!colored && oneColor != null) {
				Operand color = possibleColors.get(oneColor).pop();
				this.colors.put(oneColor,color);
				for (Register rNeighbour : ig.graph.get(oneColor).intfs) {
					if (possibleColors.containsKey(rNeighbour)) {							
						possibleColors.get(rNeighbour).remove(color);
					}
				}
				colored = true;
				todo.remove(oneColor);
			}
			// 3. Else, look for a node with a preference for an already colored node
			if (!colored) {
				for (Register r : todo) {
					if (!ig.graph.get(r).prefs.isEmpty()) {
						Register rAlrColored = null;
						for (Register rPref : ig.graph.get(r).prefs) {
							if (!todo.contains(rPref) || rPref.isHW()) {
								// rPref already colored found
								rAlrColored = rPref;
								break;
							}
						}
						if (rAlrColored != null) {
							Operand color = null;
							if (rAlrColored.isPseudo()) color = this.colors.get(rAlrColored);
							else color = new Reg(rAlrColored);
							this.colors.put(r,color);
							for (Register rNeighbour : ig.graph.get(r).intfs) {
								if (possibleColors.containsKey(rNeighbour)) {							
									possibleColors.get(rNeighbour).remove(color);
								}
							}
							colored = true;
							todo.remove(r);
							break;
						}
					}
				}
			}
			// 4. Last case, look for a node that has a possible color
			if (!colored) {
				for (Register r : todo) {
					if (!possibleColors.get(r).isEmpty()) {
						Operand color = possibleColors.get(r).pop();
						this.colors.put(r,color);
						for (Register rNeighbour : ig.graph.get(r).intfs) {
							if (possibleColors.containsKey(rNeighbour)) {							
								possibleColors.get(rNeighbour).remove(color);
							}
						}
						colored = true;
						todo.remove(r);
						break;
					}
				}
			}
			// Failed to find a register to color, so spill a register
			if (!colored) {
				Register r = todo.pop();
				this.colors.put(r,new Spilled(8*this.nlocals));
				this.nlocals++;
			}
		}
	}
	
	void print() {
		System.out.println("coloring output:");
		for (Register r: this.colors.keySet()) {
			Operand o = this.colors.get(r);
			System.out.println("  " + r + " --> " + o);
		}
	}
}
