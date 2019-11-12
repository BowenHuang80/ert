# Building
Follow the steps below to build and run the application

## Repository Clone
- Create and cd into a new directory
- git clone git@github.com:abhinavseewoosungkur/ert.git
- git checkout develop

## Import into Android Studio
- When the clone is complete, start Android Studio
- Import Non-Android Studio project (Although Android Studio has been used to create the project, this has to be done because of version control)
- Choose the folder which you cd'ed into and choose ert

## Build
- Connect your phone to your development machine or configure and emulator
- Press shift+F10 to run the application or click on green play icon on the toolbar

You should see the app pulling the data from the server. 
Nothing will get updated unless you change data on the server. To do that:
Connect with the Linux server. There are two methods

- using shell
- using MySQL workbench

## Shell

- ssh common@104.236.107.130
- password: common-ert

### Changing data
- open the mysql console -> mysql -u root -p
- password for mysql: testpass
- use ert
- select * from report;
- update data using mysql commands

or you can simply connect the mysql server using MySQL Workbench which is easier
## MySQL Workbench
- on MySQL workbench, create new mysql connection
- connection name: custom ert server
- connection method: standard TCP/IP over SSH
- SSH Hostname: 104.236.107.130:22
- SSH Username: common
- Click on Test Connection
- MySQL Workbench should ask for a password
- Type common-ert
- It should say connection parameters are correct
- Click on OK
- When it asks password for MySQL, type in testpass

Now use the MySQL workbench front end to manipulate the data.
