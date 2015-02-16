
Architecture details:
Servers: 
	Registration Server: This server will run on port 60000
	Directory Server: This server will run on port 50000
Client:
	Project Client: This connects with Registration server and Direcory Server on two different ports so simultaneous connection is possible

***********************************************************************************************************************
Execution details:
	1. In the unix terminal got to the Projects folder after extracting the Submissions.tar
	2. type command "make clean"
	3. type command "make all"
	4. This generates the class files for all the three projects
	5. The Registration server has to be run on the following machine
		 machine name: lind40-04, ip address:134.84.62.104		
	6. On the above machine run the following RegistrationServer code.
	7. navigate in the Projects/ProjectRegistration folder in unix terminal
	8. enter command "java MainRegistrationThread" in the machine
	9. The Directory Server can be started on any machine that can access the Registration Server IP.		
	10. navigate in the Projects/ProjectDirectory folder in unix terminal
	11. enter Command "java MainDirectoryClass"
	12. The client can be run any machine. There is no restriction of ip address or anything like that.
	13. The ClientObject should be run only if the Credential server is running other wise it throws a sockettimeoutexception.
	14. To run the navigate into the /Projects/ProjectClient
	15. Then enter the command "java MainClientThread" into the unix terminal
	16. Then enter the commands as in the question paper:
	For example: register sandeep one2 one2
		     login sandeep one2
                     share f1.mp3
		     find f1.mp3
		     fastfiledownload f1.mp3
		     servershare
		     exit
****************************************************************************************	
Compilation Details and makefile:

Steps to compile:cd /Projects/
		make clean
		make all
Note:
	The file that has to be download should be present in the ProjectDirectoy/shared_dir folder else program will throw FileNotFoundException.
		
		

******************************************************************************************************************************		
	Assumptions:
		1. The Registration Server should be running before the Directory Serve.
		2.	The files that we are using should have their names in lower case otherwise client is not able to find the file.
		3. Examples of commands entered from client:
			register sanDeep 5211 5211
			login sandeep 5211
			share f1.mp3
			find f1.mp3
			fastfiledownload f1.mp3
			servershare
			exit
		4. fastfiledownload should not be entered before find command.
		5. one should always log in before using the comands like share or servershare or find or fastfiledownload
		

			
		
