uuigaz
======

Battleships inspired game.

Website: http://fulkerson.github.com/uuigaz/

Requirements
------------

### Server:
* Java
* [Protocol Buffers](https://developers.google.com/protocol-buffers/)

### Client:
* Python
* [Protocol Buffers](https://developers.google.com/protocol-buffers/)
* [PyGame](http://www.pygame.org/)


Downloads
---------

The server is available as a jar with all dependencies included here:
https://github.com/Fulkerson/uuigaz/downloads.
There's also a test server/bot available.



Build instructions
------------------

This project uses [protobuf](https://developers.google.com/protocol-buffers/)
in order to serialize messages. It should be available in most repositories
(in Debian/Ubuntu as **libprotobuf-java**) or at https://developers.google.com/protocol-buffers/.

### Server and Bot
First make sure to point the ant build script to a protobuf jar by
inspecting and editing the properties at line 5 and 6 in build.xml
There's an already compiled protobuf-2.4.1.jar at the download page
if a platform specific version is not desired.

If using the version on the downloads page just put it inside a lib/
directory on the project root (i.e. the dir this readme is placed)
and modify **build.xml** accordingly:

    <!-- Shared library dir -->
    <property name="lib.dir" value="lib/"/>
    <property name="protobuf.jar" value="protobuf-java-2.4.1.jar"/> 


In order to build type `ant build`. Run `ant jar` to create **Server.jar**
and **ServerTest.jar** in the **jar/** directory.


### Client
The client is written in Python using PyGame and further instructions are available in the python subdir.
