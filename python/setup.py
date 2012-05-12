#!/usr/bin/env python

from setuptools import setup

setup(name='Uuigaz',
      version='0.1',
      description='Client for Uuigaz, a Battleships inspired game',
      url='http://fulkerson.github.com/uuigaz/',
      install_requires=['pygame'],
      packages=['uuigaz'],
      package_data={'uuigaz': ['resources/*']},
      scripts=['scripts/uuigaz'],
     )
