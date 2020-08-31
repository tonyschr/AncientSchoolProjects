# $Revision $
# $Log $

JC=	javac 
CLASS=	Command.class CommandExecutor.class NewCommand.class \
AddVertexCommand.class  \
LoadSaveCommand.class TransformCommand.class \
SelectCommand.class SelectRegionCommand.class DeselectAllCommand.class \
MakePlaneCommand.class MakeSplineCommand.class MakeSplineSurfaceCommand.class \
ExtrudeCommand.class LatheYCommand.class FlipNormalCommand.class \
TriangulateCommand.class SubdivideCommand.class DeleteSelectedCommand.class \
MergeVerticesCommand.class MakeShapeCommand.class \
CopyCommand.class CutCommand.class PasteCommand.class \
SelectConnectedCommand.class JoinVerticesCommand.class \
SplineExtrudeCommand.class SmoothCommand.class \
PreviewCommand.class BevelCommand.class AddCameraCommand.class \
AddLightCommand.class PluginCommand.class GroupSelectedCommand.class \
UngroupSelectedCommand.class HideSelectedCommand.class UnhideAllCommand.class \
InvertSelectedCommand.class DeselectLastCommand.class

all: $(CLASS)

clean:
	rm -rf *.class *~ *.jar

.SUFFIXES: .java .class

.java.class:
	$(JC) $<


