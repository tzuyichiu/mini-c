
JAVAC := javac -cp lib/java-cup-11a-runtime.jar
PGM := java -cp lib/java-cup-11a.jar:src mini_c.Main

all: src/mini_c/Typing.java src/mini_c/Main.java
	$(JAVAC) src/mini_c/*.java
	$(PGM) test.c

.PHONY: test

test:
	$(PGM) test.c
