
JAVAC := javac -cp lib/java-cup-11a-runtime.jar
PGM := java -cp lib/java-cup-11a.jar:src mini_c.Main

all: src/mini_c/*.java
	$(JAVAC) src/mini_c/*.java

.PHONY: test

test:
	$(PGM) --debug --interp-rtl $(f)
