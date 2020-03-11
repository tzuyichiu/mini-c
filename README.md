# Mini C

## Overview

Mini C is a project done in Ecole Polytechnique, proposed in the course INF564:
Compilation. It is a compiler written in Java for a fragment of the language 
C, containing integers and pointers to structures, and is totally compatible 
with C. All description can be found on the page (in French): 
https://www.enseignement.polytechnique.fr/informatique/INF564/projet/sujet-v1.pdf

### Usage

Once inside the root directory, execute `make` to *compile (!)* this compiler. 

Four `make` targets were defined in order to test our compiler by executing 
`Main.java` with the debug mode:

- `make test`: generates the corresponding assembly code X86-64 inside a `.s` 
file within the same directory as the source testing file. For example, 
`test.c` will be transformed into `test.s`.
- `make test-typing`: without any interpretation, used to test typing errors.
- `make test-rtl`: generates the corresponding `RTLGraph`.
- `make test-ertl`: generates the corresponding `ERTLGraph`.

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
statements, declarations and files. Refer to the detailed page for the 
inference rules based on which we decided to throw an error on typing.

After execution, the abstract syntax tree of type `File` defined in `Ttree.java` 
(after typing) will be constucted from `Pfile` defined in `Ptree.java` 
(after parsing).

### Implementation and difficulties

In order to implement a visitor `v`, the main idea is to call other concerned 
visitors to do the examination recursively. Inside `v`, each time we call 
another visitor `v'`, `v'` will examine the concerned part, throw errors if 
necessary, and update some global variables (or shared fields), so that `v` can 
do further examination combining these fields updated by different concerned 
visitors. If some error occurs, `v` throws an error message, otherwise, it 
updates the global variables in its turn.

Notice that we added a method `Typ.equals(Typ)` in order to simplify syntax 
for type verifications.

By the way, we also encountered some difficulties while working with variables
inside different layers of blocs. Instead of only considering a dictionary 
containing all variables seen previously, we actually have to use a stack of 
dictionaries. The reason is that if a variable has been defined twice inside 
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