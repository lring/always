To use Always-On git repository on Reeti:

* First, update the /etc/apt/source.list file (search for: ubuntu 11.10 
source.list) with the corresponding standard content of your Ubuntu 
version of the OS.

* Second, do: sudo apt-get update

* Third, install git: sudo apt-get install git

* Then, do the followins:

git config --global user.name "<your name here>"
git config --global user.email "<your e-mail address here>"

* To test use: 

git config -l

* Create a folder, Always-On, and CD to that folder, then:

git clone https://github.com/always-on/always.git

* CD to the folder of the project, i.e. always, then:

git remote rename origin always-on

git remote add <mshayganfar> https://github.com/mshayganfar/always.git

git checkout -b <branch name, e.g. develop>

git pull <mshayganfar> <branch name, e.g. develop>

=====================================================================

Now, to commit:

git add --all ==> <or a particular file or folder> 

git commit -m "<comment>"

git push mshayganfar <branch name>
