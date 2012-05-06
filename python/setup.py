#!/usr/bin/env python

from distutils.core import setup

setup(name='Uuigaz',
      version='0.1',
      description='Client for Uuigaz, a Battleships inspired game',
      url='http://fulkerson.github.com/uuigaz/',
      packages=['uuigaz'],
      package_data={'uuigaz': ['resources/*']},
      scripts=['scripts/uuigaz'],
     )
