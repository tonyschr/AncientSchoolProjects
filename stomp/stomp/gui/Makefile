# $Revision: 1.4 $
# $Log: Makefile,v $
# Revision 1.4  1998/04/27 19:36:53  schreine
# New features.
#
# Revision 1.3  1998/04/23 17:33:44  schreine
# Numeric options dialog, made background of all dialogs correct
#
# Revision 1.2  1998/04/06 18:29:45  schreine
# Added commenting, finishing up code necessary for the basic GUI.
#

JC=	javac -O
CLASS= Appearance.class ButtonBar.class ColorBox.class ColorDialog.class \
LockButton.class MessageDialog.class PrimitiveDialog.class \
SurfaceDialog.class ViewContainer.class StatusBar.class \
Numeric.class Options.class AboutDialog.class RenderDialog.class \
AnimationPanel.class

all: $(CLASS)

clean:
	rm -rf *.class *~ *.jar

.SUFFIXES: .java .class

.java.class:
	$(JC) $<
