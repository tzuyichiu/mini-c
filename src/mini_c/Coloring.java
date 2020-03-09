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
		Set<Register> todo = new HashSet<>();
		Map<Register, LinkedList<Operand>> possibleColors = new HashMap<>();
		for(Map.Entry<Register, Arcs> entry : ig.graph.entrySet()) {
			todo.add(entry.getKey());
			for(Register rAlloc : Register.allocatable) {
				
			}
		}
	}
}
