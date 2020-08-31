#Stomp3D top-level makefile

JC=	javac -O
DIRS= data3d gui view transform command anim
CLASS= FastVector.class Mode.class Stomp.class GUIListener.class \
StompProperties.class SutherlandHodgman.class

all: $(CLASS)
	for i in ${DIRS}; do \
		(cd $$i; make;); \
	done

doc:
	javadoc -d Documentation stomp stomp.data3d \
stomp.data3d.file stomp.gui stomp.view stomp.transform stomp.command

count:
	wc -l *.java data3d/*.java data3d/file/*.java gui/*.java \
transform/*.java view/*.java command/*.java anim/*.java ../plugins/*.java

clean:
	for i in ${DIRS}; do \
		(cd $$i; make clean;); \
	done
	rm -rf *.class *~ *.jar

.SUFFIXES: .java .class

.java.class:
	$(JC) $<
