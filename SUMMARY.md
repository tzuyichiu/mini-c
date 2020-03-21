# Summary of our work

The compilation is seperated into five steps:
1. Examination of typing errors while constructing an abstract syntax tree
2. Transformation of the abstract syntax tree into a `RTLGraph` 
(*RTL: Register Transfer Language*)
3. Transformation of the `RTLGraph` into a `ERTLGraph` 
(*ERTL: Explicit Register Transfer Language*)
4. Transformation of the `ERTLGraph` into a `LTLGraph` 
(*LTL: Location Transfer Language*)
5. Transformation of the `LTLGraph` into a real `X86-64` assembly code

## Typing

### Overview

The class `Typing` contains the so-called `visitors` examining recursively the 
correctness of every possible typing error for expressions, statements, 
declarations and files. Refer to Mini-C standard definition for the 
inference rules based on which we decided to throw an error on typing.

After execution, the abstract syntax tree of type `File` defined in `Ttree.java` 
(after typing) will be constucted from `Pfile` defined in `Ptree.java` 
(after parsing).

### Implementation and difficulties

In order to implement a visitor `v`, the main idea is to call other concerned 
visitors to do the examination recursively. Inside `v`, each time we call 
another visitor `v'`, `v'` will examine the concerned part, throw errors if 
necessary, and update some global variables (or shared fields), so that `v` can 
do further examination with information provided in these fields updated by 
previous visitors. If some error occurs, `v` throws an error message, 
otherwise, it updates the global variables in its turn.

The method `Typ.equals(Typ)` was added in order to simplify syntax 
for type verifications.

By the way, we also encountered some difficulties while working with variables
inside different layers of blocs. Instead of only considering a dictionary 
containing all variables seen previously, we actually have to use a *stack* of 
dictionaries which is, in the end, the only suitable data structure to deal 
with nested code blocks. The reason is that if a variable has been defined 
twice inside different layers, only the newest has to be considered, and 
besides, while exiting a bloc, the newly defined local variables shouldn't be 
available anymore outside this bloc. We use a stack of dictionary so that we 
can look through all layers to find a variable and delete the latest variable 
dictionary while exiting the bloc.


## Construction of RTLGraph

### Overview

After the typing examination done, we try to produce a `RTLGraph` from the 
previously constructed abstract syntax tree, where two major things are used: 
pseudo-registers and labels. The pseudo-registers allow us to simulate the 
storage of variables, the return value and the arguments. The labels indicate 
where to go after the execution of the actual one. For this, we define a 
class `ToRTL` which implements the so-called `Visitor` defined in `Ttree.java`, 
visiting expressions, statements and declarations recursively, and constructs 
the graph as the visit persues.

### Implementation and difficulties

To construct a `RTLGraph`, we have to start from the last statement. One of the
difficulties we encountered consists in not noticing that we have to call 
visitors in the reverse order.

After reversing the visiting order, we could update global variables 
`this.lastFresh` (corresponding to the new created label) and `this.r` (the 
register where the result should be stored) before calling the next visitor 
recursively. In this way, the next node that we construct (by calling the 
corresponding visitor) will be able to access to this label (to which it should 
be directed) and store the result to the register.

The graph structure becomes more complicated when it comes to `while`. When 
implementing `goto` (at the end of the loop), since the graph is constructed in 
the reverse order, at the moment we want to tell `goto` where to go, the label 
`l1` after `goto` is not yet constructed (i.e. the evaluation of the `Expr`). 
We then came up with an idea, which is to create a totally independant label `l`
for `goto`, make the loop direct to it, and finally associate the corresponding 
RTL when `l1` is ready. And it works!


## RTLGraph to ERTLGraph

### Overview

In the previously constructed `RTLGraph`, we only made use of pseudo-registers,
but in order to explicitly decide which registers and at which position on the
stack to store variables, arguments and results, we transform every RTL 
instruction into one or more ERTL instructions where we specify the use of 
physical regisiters and the manipulation of the stack. The class `ToERTL`
implements a `RTLVisitor` defined in `RTL.java`, which visits different types
of RTL instructions.

### Implementation and difficulties

Now that we have obtained a skeleton of the final assembly architecture, in 
order to construct a corresponding `ERTLGraph`, we simply start from the entry 
label of the `RTLGraph`, keep the same label name, transform every RTL 
instruction into one or more ERTL instructions, and recursively visit the next 
label(s). The use of `this.rtlLabel` allows every visitor to know from which 
label it has been called, and possibly construct the corresponding ERTL instruction 
with the same label name, or choose another label in some cases.

The subtle point here is that when we are producing multiple ERTL 
instructions from only one RTL instruction, fresh labels have to be created. 
Similar to the construction of `RTLGraph`, these new instructions have to be 
created in the reverse order.

We also encountered another difficulty when translating the instruction
`Rgoto`. While testing with Mini-C programs that contain a *while* loop, we 
forgot not to visit a same label twice or more, making the visiting enter an
infinite loop. We solved this problem by using a table called `visitedLabels` 
that records every visited Labels.

## ERTLGraph to LTLGraph

### Overview

Given an `ERTLGraph`, the two main differences with a X86-64 assembly code are
that there are still non-physical register left in the graph and that the control
flow is non-linear (there still exists control branches in conditions while the
x86-64 assembly code is supposed to be linear). The translation from `ERTL` to
`LTL` is going to act on the first point : find an explicit register for every
pseudo-register left in the graph, i.e. perform *register allocation*.

The register allocation is divided in three steps : 

 - determine the *liveness* of registers along the control graph, that is, find 
 	which registers are alive, dead, used or produced when the control flows 
 	through an instruction block.

 - determine the *interference* and *preference* of pseudo-registers which value
 	can or cannot be carried by the same physical register. These are built upon
 	the previous linevess analysis.
 	
 - perform the *coloring* of the register graph, that is, give every register 
 	(physical or abstract) a color (a corresponding physical register) such that
 	in the interference graph no pair of interfering registers are given the
 	same color. If the set of colors is too small, spill registers on the stack.
 	
The `ERTL` to `LTL` translation is then straightforward : we implemented an
`ERTLvisitor` called `ToLTL` performing the graph coloring of a given `ERTLgraph`,
then visiting it and replacing pseudo-registers with their color.

### Implementation and difficulties

Special care was required for `ERload`, `ERstore`, `ERget_param` and `ERpush_param` 
because a direct translation cannot be performed : too many spilled registers make 
the instruction require too many simultaneous access to memory, so we have to use
a temporary register (or two for `ERstore`).

In the `ERmbinop` case :

 - if we have a `mov x x` instruction, simplify it into a `goto` instruction
 - if we have a division with arguments `x` and `y`, make sure the second, `y`,
 	is not on the stack, otherwise use a temporary register
 - else the `ERmbinop` is treated like the previous ones with a special case
 	when the two arguments are on the stack and a temporary register has to be
 	used.

## LTLGraph to X86-64

### Overview

Now that every pseudo-register has been tranformed into physical registers or 
stack memory, the remaining work is to linearize the graph, and transform every 
LTL code into real assembly instructions. The work is done in the file 
`ToX86_64.java` which implements a `LTLVisitor` defined in `LTL.java`, visiting 
the previously constructed `LTLGraph`.

### Implementation and difficulties

Notice that the instructions that are not reused by any other instruction 
will not need to be labeled. Also, the fact that we translated 
`mov r1 r2 -> L` into `goto -> L` when `r1` and `r2` have the same color made 
the final LTL code filled with lots of `goto` which we can eliminate. It means
that a great part of LTL code isn't needed inside the final assembly code, so 
we make use of a set storing labels that are needed. Also, as in the previous
work, we make use of a set storing the labels already visited, in order to 
translate the same LTL code only once.

A function called `lin` deals with a label by calling and updating the two sets,
while interacting mutually and recursively with the visitors, whose work is 
to translate the corresponding LTL code. If a label hasn't been treated, the 
function marks it as visited and calls the visitor correponding to its 
instruction. Otherwise, this label is being reused, so it is needed inside the
final assembly code, and the function produces a `jmp` instruction to this 
label.

As for the visitors, of course we call the function `lin` at the end in order 
to treat recursively the next label(s). The major difficulties we encountered 
lie in three parts:
- linearization of branches
- recognition of labels that will be needed
- flag settings before applying instructions with condition code (cc)
- special treatment for multiplication and division

Firstly, during the linearization of branches, we deal with the label that 
hasn't yet been visited in priority, so that its instruction will
be placed just afterwards. However if this label corresponds to the positive 
test, then we have to naturally inverse the condition code. If both of them
have already been visited, we have no choice but to produce a `jmp` instruction
for the negative label.

Secondly, at the beginning we forgot to mark some of the labels as needed, so 
they didn't appear in the final assembly code, which thus resulted errors for 
undefined reference during the interpretation. We finally came out with the 
conclusion of two sufficient and necessary conditions that a label will be 
needed:
- every label to which a `jmp` is performed
- the label visited later during branching, when both branching labels haven't 
been visited

Thirdly, flags have to be set for the instructions including condition code, 
for instance `jcc` and `setcc`. When a comparison is needed, a `cmpq` is 
performed beforehand, otherwise we simply add `$0` to the register in order to 
set flags. `setcc` is more complicated, as it receives only a byte
register as its argument. We make use of the lower-order byte `%r15b` of the 
temporary register `%r15` which should be initially zeroed, and at the end 
moved to the destination register.

Finally, as for the multiplication, the destination must be a register instead 
of "spilled" memory address, so we make use of a temporary register to do the 
transfer. As for the division, `cqto` must be used to convert *quad* to *oct* 
for the divisor since `idiv` does a 128/64 bit division. Only the first 
register needs to be used because it is imposed that `%rax` be used to store 
the divident (before `idiv`) and the result (after `idiv`).
