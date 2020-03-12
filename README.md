# MCC: Mini-C Compiler

## Overview

**Mini-C Compiler (MCC)** is a project done in Ecole Polytechnique, proposed in 
the course INF564: Compilation. It is a compiler of Mini-C, a sub-language of 
the C language whose standard is defined here : https://www.enseignement.polytechnique.fr/informatique/INF564/projet/sujet-v1.pdf.

Mini-C contains integers and pointers to structures, and is totally compatible 
with C. The **MCC** is written in Java, takes a Mini-C program (`.c`) in input 
and compiles it to X86-64 text assembly file (`.s`) that can then be translated 
to binary (using GCC for instance).

### Usage

Once inside the root directory, execute `make` to *compile (!)* this compiler. 
The **MCC** binary you get as a result can be run by making use of `mini-c` in
the root directory: 

```bash
./mini-c [OPTION] your_mini_c_file.c
```

where `[OPTION]` can define the verbosity (`--debug`) as well as the depth of compilation (`--interp-rtl`, etc.). See the `make` targets below to run the 
compiler on different depths of compilation. In any case, type the following 
command for help:

```bash
./mini-c --help
```

To compile directly a Mini-C program to a binary, run:

```bash
./mini-c myprogram.c && gcc myprogram.s -o myprogram
```

which will produce the binary `./myprogram` of your Mini-C program.


### Running tests

The following `make` targets were defined in order to test our compiler by 
executing `Main.java` with the debug mode:

- `make test`: generates the corresponding assembly code X86-64 inside a `.s` 
file within the same directory as the source testing file. For example, 
`test.c` will be transformed into `test.s`.
- `make test-typing`: without any interpretation, used to test typing errors.
- `make test-rtl`: generates the corresponding `RTLGraph`.
- `make test-ertl`: generates the corresponding `ERTLGraph`.
- `make test-ltl`: generates the corresponding `LTLGraph` and displays the `ERTLGraph` with register liveness, the register interferences and the register allocation deduced (called "coloring" where registers are colors).

The default testing file is `test.c`, however one can specify any other testing 
file by passing it to the argument `f`:

```bash
make test-choice f=path/to/your/testing/file
```

Besides, this project is provided with a directory `tests` including available 
testing files. The following commands allow to launch the testing procedure, 
you being placed inside `tests`: 
```bash
cd tests

> ./run -all ../mini-c               # To test everything
> ./run -1 ../mini-c                 # To test the syntax analysis
> ./run -2 ../mini-c                 # To test the typing
> ./run -i "../mini-c --interp-rtl"  # To test RTL
> ./run -i "../mini-c --interp-ertl" # To test ERTL
> ./run -3 ../mini-c                 # To test the X86-64 compilation
```


# Summary of our work

The compilation is seperated into five phases:
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

The class `Typing` contains the so-called `visitors` examining recursively the correctness of every possible typing error for expressions, statements, 
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
previously constructed abstract syntax tree, where two major things are used: pseudo-registers and labels. The pseudo-registers allow us to simulate the 
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
stack we will need to store variables, arguments and results, we transform
every RTL instruction into one or more ERTL instructions by specifying the use
of physical regisiters and the manipulation of the stack. The class `ToERTL`
implements a `RTLVisitor` defined in `RTL.java`, visiting different types
of RTL instructions.

### Implementation and difficulties

**TODO**


## ERTLGraph to LTLGraph

### Overview

**TODO**

### Implementation and difficulties

**TODO**

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