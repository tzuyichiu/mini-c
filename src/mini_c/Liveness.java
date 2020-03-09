package mini_c;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/** Analysis of variable liveness in a ERTL Graph */

class Liveness {
    Map<Label, LiveInfo> info;

    Liveness(ERTLgraph g) {
        this.info = new HashMap<>();
        
        g.graph.forEach((label, ertl) -> {
            this.info.put(label, new LiveInfo(ertl));
        });

        // Compute pred for each label
        g.graph.forEach((label, ertl) -> {
            for (Label s: this.info.get(label).succ) {
                this.info.get(s).pred.add(label);
            }    
        });

        // Algorithm of Kildall to compute ints and outs
        Set<Label> ws = new HashSet<>();
        for (Label l: g.graph.keySet()) {
            ws.add(l);
        }
        while (ws.size() > 0) {
            Label l = ws.iterator().next();
            ws.remove(l);
            LiveInfo li = this.info.get(l);
            Set<Register> old_in = li.ins;
            
            // update outs
            for (Label s: li.succ) {
                li.outs.addAll(this.info.get(s).ins);
            }

            // update ins
            li.ins.addAll(li.outs);
            li.ins.removeAll(li.defs);
            li.ins.addAll(li.uses);
            
            if (!li.ins.equals(old_in)) {
                ws.addAll(li.pred);
            }
        }
    }

    public Map<Label, LiveInfo> getInfo() {
        return this.info;
    }

    private void print(Set<Label> visited, Label l) {
        if (visited.contains(l)) return;
        visited.add(l);
        LiveInfo li = this.info.get(l);
        System.out.println("  " + String.format("%3s", l) + ": " + li);
        for (Label s: li.succ) print(visited, s);
    }

    void print(Label entry) {
        print(new HashSet<Label>(), entry);
    }
}

class LiveInfo {
    ERTL instr;
    Label[] succ;       // successors
    Set<Label> pred;    // predecessors
    Set<Register> defs; // definitions
    Set<Register> uses; // usages
    Set<Register> ins;  // alive variables in entry
    Set<Register> outs; // alive variables in exit

    LiveInfo(ERTL instr) {
        this.instr = instr;
        this.succ = instr.succ();
        this.defs = instr.def();
        this.uses = instr.use();
        this.pred = new HashSet<>();
        this.ins  = new HashSet<>();
        this.outs = new HashSet<>();
    }

    @Override
    public String toString() {
        String res = String.format("%-19s", this.instr);
        res += "  defs = ";
        res += String.format("%-18s", Arrays.toString(this.ins.toArray()));
        res += "  uses = ";
        res += String.format("%-18s", Arrays.toString(this.ins.toArray()));
        res += "  in = ";
        res += String.format("%-18s", Arrays.toString(this.ins.toArray()));
        res += "  out = ";
        res += String.format("%-18s", Arrays.toString(this.ins.toArray()));
        return res;
    }
}