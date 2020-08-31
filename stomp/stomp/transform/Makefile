# $Revision $
# $Log $

JC=	javac -O
CLASS=	Transformation.class Translation.class Rotation.class Scale.class \
Taper.class Shear.class

all: $(CLASS)

Transformation.class: Transformation.java
	$(JC) Transformation.java

Translation.class: Translation.java
	$(JC) Translation.java

Rotation.class: Rotation.java
	$(JC) Rotation.java

Scale.class: Scale.java
	$(JC) Scale.java

Taper.class: Taper.java
	$(JC) Taper.java

Shear.class: Shear.java
	$(JC) Shear.java

clean:
	rm -rf *.class *~ *.jar

