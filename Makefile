allmutantclasses=$(addsuffix /org/json/JSON.class,$(addprefix mutants/,$(mutants)))
.PRECIOUS: $(allmutantclasses)
.SECONDARY:  $(allmutantclasses)

m=1
t=./json.log
run:
	java -cp build/classes:mutants/$(m) org.json.JSONTest $(t) > build/$(m).log

compilex:
	javac -d build/xclasses src/JSON.java

compile:
	javac -cp build/xclasses -d build/classes src/JSONTest.java src/XJSON.java  src/JSONFilter.java

compilemutants: $(allmutantclasses)
	@ls $(allmutantclasses)
	#for i in mutants/*; do echo $$i; javac -d $$i/ $$i/org/json/*.java; done

teststr: | build
	python3 ./bin/grammar-fuzz.py | tee build/json.original.log | java -cp ./build/classes org.json.JSONFilter | tee json.log | nl

clean:
	rm -rf build

clobber:
	rm -rf build mutants json.log mutants.log

createmutants:
	./major/bin/javac -d build/mclasses -J-Dmajor.export.mutants=true -XMutator:ALL src/JSON.java

build/%.lst: mutants/%/org/json/JSON.class | build
	$(MAKE) run m=$*
	mv build/$*.log build/$*.lst

mutants:=$(patsubst mutants/%,%,$(wildcard mutants/*))

mutants/%/org/json/JSON.class: mutants/%/org/json/JSON.java
	javac -d mutants/$*/ $<

allresult:=$(addsuffix .lst,$(addprefix build/,$(mutants)))

all: $(allresult)
	@echo done

build: ; mkdir -p build

echo:
	@echo $(allmutantclasses)


mkills.csv:
	./bin/count_killing.rb > mkills.tmp
	mv mkills.tmp mkills.csv
