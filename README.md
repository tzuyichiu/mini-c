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