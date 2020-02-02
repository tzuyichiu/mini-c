# Mini C

Mini C is a project done in Ecole Polytechnique, proposed in the course INF564:
Compilation. It is a compilator written in Java for a fragment of the language 
C, containing integers and pointers to structures, which is totally compatible 
with C. All description can be found on the page (in French): 
https://www.enseignement.polytechnique.fr/informatique/INF564/projet/sujet-v1.pdf

## Typing

The file *Typing.java* contains the so-called **visitors** examining recursively 
the correctness of every possible typing error for expressions, statements, 
declarations and files. Refer to the detailed page for the inference rules based
on which we decided to throw an error on typing.

In order to implement a visitor `v`, the main idea is to call other concerned 
visitors to do the examination recursively. Inside `v`, each time we call 
another visitor `v'`, `v'` will examine the concerned part, throw errors if 
necessary, and update some global variables (or shared fields), so that `v` can 
do further examination combining these fields updated by different concerned 
visitors. If some error occurs, `v` throws an error message, otherwise, it 
updates the global variables in its turn.