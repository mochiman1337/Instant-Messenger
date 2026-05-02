TCP CLient and Server app ver 1.0.1
===================================
Written by: Robert Lee

In order to run this program you need to have 3 Terminals up
1: TCPServer 2:TCPClient 3:TCPClient2

At this present time you can send messages, and files between clients once logged in.
DO NOT touch the SERVER Terminal when it starts running. Only interact with the Client Terminals

I have fixed the file name issue
Added console log "Username" has disconnected, when client exits terminal

Essential Git Commands for terminal to use:

  git clone "URL" = clones the repository to your IDE. You must be in the Directory you want before running
  git status = shows which files are modified, staged or untracked
  git commit -m "Your Message for your commit" = Adding a commit message before you push
  git add . = This is a shorcut to stage all modified files instead of individuals
  git push = uploads your commits to GitHub
  git pull = pulls the latest changes from repo and updates all your files to match changes

How to run:
Note: The Commands AUTH, MSG and FILE are CASE-SENSITIVE and files sent must also include File Extension!
  Use the following commands in your terminal:

    java -jar Server.jar
    Java -jar Client.jar

When sending the file. It'll be saved as "recieved_from_Sender_FileName"
This is so you can see the successful transfer of a file, without running into replacement issue.
Files sent will appear in the same folder as your JAR files

*Make sure to have the TextFile.txt in the same folder as the java folders to run it*
