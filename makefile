

all: 
	make client
	make directoryserver
	make registrationserver

client:
	cd ProjectClient/ && javac MainClientThread.java

directoryserver:
	cd ProjectDirectory/ && javac MainDirectoryClass.java
	
registrationserver:
	cd ProjectRegistration/ && javac MainRegistrationThread.java

clean:
	cd ProjectClient/ && rm -f *.class
	cd ProjectDirectory/ && rm -f *.class
	cd ProjectRegistration/ && rm -f *.class
