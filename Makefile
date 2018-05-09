m=1
t=./json.log
run:
	java -cp build/classes:mutants/$(m) org.json.JSONTest $(t) | tee build/$(m).log

compilex:
	javac -d build/xclasses src/JSON.java

compile:
	javac -cp build/xclasses -d build/classes src/JSONTest.java src/XJSON.java  src/JSONFilter.java

compilemutants:
	for i in mutants/*; do echo $$i; javac -d $$i/ $$i/org/json/*.java; done

teststr: | build
	python3 ./bin/grammar-fuzz.py | tee build/json.original.log | java -cp ./build/classes org.json.JSONFilter | tee json.log | nl

clean:
	rm -rf build

clobber:
	rm -rf build mutants json.log mutants.log

createmutants:
	./major/bin/javac -d build/mclasses -J-Dmajor.export.mutants=true -XMutator:ALL src/JSON.java
