# MCC : Mini-C Compiler

## Overview

**Mini C Compiler (MCC)** is a project done in Ecole Polytechnique, proposed in the course INF564:
Compilation. It is a compiler of Mini-C, a sub-language of the C language 
which standard is defined here : 
https://www.enseignement.polytechnique.fr/informatique/INF564/projet/sujet-v1.pdf.
Mini-C contains integers and pointers to structures, and is totally compatible 
with C. The MCC is written in Java, takes a Mini-C program in input and compiles it
to x86-64 text assembly file (.s) that can then be translated to binary (using GCC
for instance).

### Usage

Once inside the root directory, execute `make` to *compile (!)* this compiler. The MCC binary you get as a result in the root directory is called ```mini-c```. You can use mini-c as follows :

```bash
./mini-c [OPTION] your_mini_c_file.c
```

Where ```OPTION``` can define the verbosity (```--debug```) as well as the depth of compilation (```--interp-rtl```, etc.). See the make targets below to run the compiler on different depths of compilation. In any case, type the following command for help :

```bash
./mini-c --help
```

To compile directly a Mini-C program to a binary, you can use :

```bash
./mini-c myprogram.c && gcc myprogram.s -o myprogram
```

which will produce the binary `myprogram` of your Mini-C program that you can launch :

```bash
./myprogram
```

### Running tests

The following `make` targets were defined in order to test our compiler by executing 
`Main.java` with the debug mode:

- `make test`: generates the corresponding assembly code X86-64 inside a `.s` 
file within the same directory as the source testing file. For example, 
`test.c` will be transformed into `test.s`.
- `make test-typing`: without any interpretation, used to test typing errors.
- `make test-rtl`: generates the corresponding `RTLGraph`.
- `make test-ertl`: generates the corresponding `ERTLGraph`.
- `make test-ltl`: generates the corresponding `LTLGraph` and lets you see the ERTL graph with register liveness, the register interferences and the register allocation deduced (called "coloring" where registers are colors).

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
```

- For Typing: 
```bash
./run -2 ../mini-c
```
- For RTL: 
```bash
./run -i "../mini-c --interp-rtl"
```
- For ERTL: 
```bash
./run -i "../mini-c --interp-ertl"
```
- For X86-64: 
```bash
./run -3 ../mini-c
```


## Typing

### Overview

The file `Typing.java` contains the so-called `visitors` examining 
recursively the correctness of every possible typing error for expressions, 
statements, declarations and files. Refer to Mini-C standard definition for the 
inference rules based on which we decided to throw an error on typing.

After execution, the abstract syntax tree of type `File` defined in `Ttree.java` 
(after typing) will be constucted from `Pfile` defined in `Ptree.java` 
(after parsing).

### Implementation and difficulties

In order to implement a visitor `v`, the main idea is to call other concerned 
visitors to do the examination recursively. Inside `v`, each time we call 
another visitor `v'`, `v'` will examine the concerned part, throw errors if 
necessary, and update some global variables (or shared fields), so that `v` can 
do further examination with information provided in these fields updated by previous visitors. If some error occurs, `v` throws an error message, otherwise, it 
updates the global variables in its turn.

The method `Typ.equals(Typ)` was added in order to simplify syntax 
for type verifications.

By the way, we also encountered some difficulties while working with variables
inside different layers of blocs. Instead of only considering a dictionary 
containing all variables seen previously, we actually have to use a *stack* of 
dictionaries which is, in the end, the only suitable data structure to deal with nested code blocks. The reason is that if a variable has been defined twice inside 
different layers, only the newest has to be considered, and besides, while 
exiting a bloc, the newly defined local variables shouldn't be available 
anymore outside this bloc. We use a stack of dictionary so that we can look 
through all layers to find a variable and delete the latest variable dictionary 
while exiting the bloc.


## Production of RTL (Register Transfer Language) code

### Overview

From `File` (defined in `Ttree`) implemented previously, here we try to produce
a `RTLFile` (defined in `RTL`) which is a RTLgraph. As same as above, we make
use of **visitors** to construct recursively the graph. Whenever a statement or 
an expression is being transformed into assembly code, the corresponding 
visitor constructs a node for each line of assembly code.


### Implementation and difficulties

To construct a RTL graph, we have to start from the last statement. One of the
difficulties we encountered consists in not noticing that we have to call 
visitors in the reverse order.

After reversing the visiting order, we could update global variables `this.l` 
(corresponding to the actual label) and `this.r` (the register where the result 
should be stored) before calling the next visitor recursively. In this way, the 
next node that we construct (by calling the corresponding visitor) will be able 
to access to this label (to which it should be directed) and store the result
to the register.

The graph structure becomes more complicated when it comes to `while`. When 
implementing `goto` (at the end of the loop), since the graph is constructed in 
the reverse order, at the moment we want to tell `goto` where to go, the label 
`l1` after `goto` is not yet constructed (i.e. the evaluation of the `Expr`). 
We then came up with an idea, which is to create a totally independant label `l`
for `goto`, make the loop direct to it, and finally associate the corresponding 
RTL when `l1` is ready. And it works!
