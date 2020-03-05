package mini_c;

import java.util.HashMap;

/** Un modèle mémoire bas niveau pour (ERTL,LTL)interp */

public class Machine {
  
  static final int word_size = 8;
  private final long maxptr;
  private long sbrk = 8l;
  private long[] mem;
  private HashMap<Register, Long> hwregs;
  HashMap<Register, Long> regs;

  Machine() {
    this(65536);
  }
  Machine(int words) {
    this.maxptr = word_size * words;
    this.mem = new long[words];
    this.hwregs = new HashMap<>();
    this.hwregs.put(Register.rsp, this.maxptr);
    this.regs = new HashMap<>();
  }
  
  /** alloue n octets */
  long malloc(int n) {
    if (n < 0 || n % word_size != 0) throw new Error("malloc: invalid argument");
    long p = this.sbrk;
    sbrk += n;
    return p;
  }
  
  void set(Register r, long v) {
    (r.isHW() ? this.hwregs : this.regs).put(r, v);
  }
  void set(Register r, boolean b) {
    this.regs.put(r, b ? 1L : 0L);
  }
  long get(Register r) {
    if (r.isHW()) return this.hwregs.containsKey(r) ? this.hwregs.get(r) : 0L;
    if (!this.regs.containsKey(r)) throw new Error("unknown register " + r);
    return this.regs.get(r);
  }

  private int index(long ptr, int ofs) {
    ptr += ofs;
    if (ptr <= 0 || (ptr >= this.sbrk && ptr < get(Register.rsp)|| ptr >= maxptr))
      throw new Error("seg fault");
    if (ptr % word_size != 0) throw new Error("pointer not aligned");
    return (int)(ptr / 8);
  }
    
  long load(long ptr, int ofs) {
    int i = index(ptr, ofs);
    return this.mem[i];
  }
  
  void store(long ptr, int ofs, long v) {
    int i = index(ptr, ofs);
    this.mem[i] = v;
  }
  
  void push(long v) {
    long ptr = get(Register.rsp) - 8L;
    set(Register.rsp, ptr);
    store(ptr, 0, v);
  }
  
  long pop() {
    long ptr = get(Register.rsp);
    long v = load(ptr, 0);
    set(Register.rsp, ptr + 8L);
    return v;
  }
  
  void push_register(Register r) {
    push(get(r));
  }
  
  void pop_in_register(Register r) {
    set(r, pop());
  }
  
  
}
