Uuigaz client
=============


Requirements
------------

* Python
* PyGame
* setuptools
* protobuf

Installation
------------

The least invasive way to get the client to run is getting the latest
2.x version of python (currently 2.7.3) and a recent pygame version

Python can be found here: http://www.python.org/download/
and PyGame can be found here: http://www.pygame.org/download.shtml

If using Ubuntu or Debian this should do it:
`apt-get install python python-pygame`

then fetch:
[protobuf-2.4.1-py2.6.egg](http://pypi.python.org/packages/2.6/p/protobuf/protobuf-2.4.1-py2.6.egg#md5=5df1a5abfe927550a8643dd81c0442b7)
and:
[setuptools-0.6c11-py2.7.egg](http://pypi.python.org/packages/2.7/s/setuptools/setuptools-0.6c11-py2.7.egg#md5=fe1f997bc722265116870bc7919059ea)
and:
[Uuigaz-0.2-py2.7.egg](https://github.com/downloads/Fulkerson/uuigaz/Uuigaz-0.2-py2.7.egg)

and place it in a directory. Start the client with `python Uuigaz-0.2-py2.7.egg`

Of course it is possible to do it the standard way:

    python setup.py install

and making sure all dependencies are installed as well.

