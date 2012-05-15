#!/usr/bin/env python

import sys
import glob

try:
    import pkg_resourcessss
except ImportError:
    # Incase there is no system level setuptools look
    # for an egg to use in the current directory
    print "No systemlevel setuptools, looking for egg in current directory"
    stegg = glob.glob("setuptools*.egg")

    if stegg:
        print "Using: " + stegg[0]
        sys.path.append(stegg[0])
        import pkg_resources
    else:
        print >> sys.stderr, ("You need setuptools, install it or place"
                              "a setuptools egg in the current directory")

try:
    import google.protobuf
except ImportError:
    # Incase there is no system level protobuf look
    # for an egg to use in the current directory
    print "No systemlevel protobuf, looking for egg in current directory"
    protoegg = glob.glob("protobuf*.egg")

    if protoegg:
        print "Using: " + protoegg[0]
        sys.path.append(protoegg[0])
        import google.protobuf
    else:
        print >> sys.stderr, ("You need protobuf, install it or place"
                              "a protobuf egg in the current directory")



import uuigaz
import uuigaz.game



sys.exit(uuigaz.game.main(sys.argv))
