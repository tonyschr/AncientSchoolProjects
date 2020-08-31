# $Revision $
# $Log $

JC= javac
CLASS= SceneReaderWriter.class StompReaderWriter.class \
CS510ReaderWriter.class WavefrontReaderWriter.class VRMLReaderWriter.class \
POVReaderWriter.class StompFileFilter.class CS410ReaderWriter.class

all: $(CLASS)

clean:
	rm -rf *.class *~ *.jar

.SUFFIXES: .java .class

.java.class:
	$(JC) $<
