# $Revision$

JC=	javac
DIRS=	movingobject
CLASS=	AdvancedWindow.class  JavaRobot3.class  \
Computer.class        JavaRobot4.class      Vec.class \
DialogWindow.class    WeaponI.class \
Human.class           MOList.class          WebWars.class \
JavaRobot1.class      Player.class          WebWarsFrame.class \
JavaRobot2.class

app:
	@make $(CLASS)

	for i in ${DIRS}; do \
		(cd $$i; make;); \
	done

AdvancedWindow.class: AdvancedWindow.java
	${JC} AdvancedWindow.java

Computer.class: Computer.java
	${JC} Computer.java

DialogWindow.class: DialogWindow.java
	${JC} DialogWindow.java

Human.class: Human.java
	${JC} Human.java

JavaRobot1.class: JavaRobot1.java
	${JC} JavaRobot1.java

JavaRobot2.class: JavaRobot2.java
	${JC} JavaRobot2.java

JavaRobot3.class: JavaRobot3.java
	${JC} JavaRobot3.java

JavaRobot4.class: JavaRobot4.java
	${JC} JavaRobot4.java

MODescriptor.class: MODescriptor.java
	${JC} MODescriptor.java

MOList.class: MOList.java
	${JC} MOList.java

Player.class: Player.java
	${JC} Player.java

Vec.class: Vec.java
	${JC} Vec.java

WeaponI.class: WeaponI.java
	${JC} WeaponI.java

WebWars.class: WebWars.java
	${JC} WebWars.java

WebWarsFrame.class: WebWarsFrame.java
	${JC} WebWarsFrame.java

clean:
	for i in ${DIRS}; do \
		(cd $$i; make clean;); \
	done
	rm -rf *.class *~ *.jar

