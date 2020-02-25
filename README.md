# Mini C

## Overview

Mini C is a project done in Ecole Polytechnique, proposed in the course INF564:
Compilation. It is a compiler written in Java for a fragment of the language 
C, containing integers and pointers to structures, and is totally compatible 
with C. All description can be found on the page (in French): 
https://www.enseignement.polytechnique.fr/informatique/INF564/projet/sujet-v1.pdf

### Usage

Once inside the root directory, execute `make` to *compile (!)* this compiler. 

`make test` executes **Main.java** with the default file **test.c**.

Besides, this project is provided with a directory **tests** including 
available testing files. The following commands allow to launch the testing
procedure:
```bash
cd tests
./run -2 ../mini-c
```

## Typing

### Overview

The file **Typing.java** contains the so-called **visitors** examining 
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

Notice that we added a method **Typ.equals(Typ)** in order to simplify syntax 
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
visitor constructs a node for each line of assembly code. By adding the node to
our RTLgraph, we can access the label of the node within the return value.


### Implementation and difficulties

To construct a RTL graph, we have to start from the last statement. One of our
difficulties encountered consists in not noticing that we have to call visitors
corresponding to the successive statements in the reverse order at each block 
`Sblock` (inside every function declaration).

By reversing the visiting order, we update global variables `this.l` 
(corresponding to the actual label) and `this.r` (the register where the result 
should be stored) before calling the next visitor recursively. In this way, the 
next node that we construct (by calling the corresponding visitor) will be able 
to access to this label (to which it should be directed) and store the result
to the register.