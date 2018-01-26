For Lauch4j to work on 64bits Linux system, you have to install 32bits libraries

For Ubuntu (used on 16.04 LTS)

$ sudo dpkg --add-architecture i386
$ sudo apt-get update
$ sudo apt-get install lib32z1 lib32ncurses5 libbz2-1.0:i386

And add the java-libpst library to your local maven repo 
(compile the develop branch in https://github.com/rjohnsondev/java-libpst and install the package...)
