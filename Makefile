
JAVAC := javac -cp lib/java-cup-11a-runtime.jar
PGM := java -cp lib/java-cup-11a.jar:src mini_c.Main
f := test.c

all: src/mini_c/*.java
	$(JAVAC) src/mini_c/*.java

.PHONY: test

test-typing:
	$(PGM) $(f)

test-rtl:
	$(PGM) --debug --interp-rtl $(f)

test-ertl:
	$(PGM) --debug --interp-ertl $(f)

test-ltl:
	$(PGM) --debug --interp-ltl $(f)
