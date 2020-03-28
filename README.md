# MCC: Mini-C Compiler

## Overview

**Mini-C Compiler (MCC)** is a project done in Ecole Polytechnique, proposed in 
the course INF564: Compilation. It is a compiler of Mini-C, a sub-language of 
the C language whose standard is defined here: 
https://www.enseignement.polytechnique.fr/informatique/INF564/projet/sujet-v1.pdf.

Mini-C contains integers and pointers to structures, and is totally compatible 
with C. Written in Java, **MCC** takes a Mini-C program (`.c`) in input 
and compiles it to an X86-64 text assembly file (`.s`). This file can then be 
translated to binary by using GCC for instance.

**Authors: Bastien SCHNITZLER, Tzu-yi CHIU (Ecole Polytechnique Promotion X2017)**

## Usage

Once inside the root directory, execute `make` to *compile (!)* this compiler. 
The **MCC** binary you get as a result can be run by making use of `mini-c` in
the root directory: 

```bash
./mini-c [OPTION] your_mini_c_file.c
```

where `[OPTION]` can define the verbosity (`--debug`) as well as the depth of 
compilation (`--interp-rtl`, etc.). See the `make` targets below to run the 
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


## Running tests

The following `make` targets were defined in order to test our compiler by 
executing `Main.java` with the debug mode:

- `make test`: generates the corresponding assembly code X86-64 inside a `.s` 
file within the same directory as the source testing file. For example, 
`test.c` will be transformed into `test.s`.
- `make test-typing`: without any interpretation, used to test typing errors.
- `make test-rtl`: generates the corresponding `RTLGraph`.
- `make test-ertl`: generates the corresponding `ERTLGraph`.
- `make test-ltl`: generates the corresponding `LTLGraph` and displays the 
`ERTLGraph` with register liveness, the register interferences and the register 
allocation deduced (called "coloring" where registers are colors).

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

## More details

More details can be found in `SUMMARY.md` about our work.
