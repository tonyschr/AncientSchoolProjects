# $Revision: 1.5 $
# $Log: Makefile,v $
# Revision 1.5  1998/04/27 19:37:06  schreine
# New features.
#
# Revision 1.3  1998/04/14 02:02:57  schreine
# Grid is not done yet
#

JC=	javac -O
CLASS= View.class Orthogonal.class Perspective.class CameraView.class \
Grid.class

all: $(CLASS)

clean:
	rm -rf *.class *~ *.jar

.SUFFIXES: .java .class

.java.class:
	$(JC) $<
