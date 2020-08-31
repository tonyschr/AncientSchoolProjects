# $Revision $
# $Log $

JC= javac -O
DIRS= file
CLASS= Polygon3d.class Primitive.class Scene.class Surface.class \
Vertex.class SurfaceList.class Spline.class SplineSurface.class \
Light.class PointLight.class Group.class

all: $(CLASS)
	for i in ${DIRS}; do \
		(cd $$i; make;); \
	done

clean:
	for i in ${DIRS}; do \
		(cd $$i; make clean;); \
	done
	rm -rf *.class *~ *.jar

.SUFFIXES: .java .class

.java.class:
	$(JC) $<

