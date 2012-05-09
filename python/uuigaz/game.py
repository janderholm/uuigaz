#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import socket
import pkg_resources

import pygame
import inputbox
import pygame.mixer

import boat_protos_pb2
import placement_grid
import game_grid
import grid
import settings as s

from optparse import OptionParser

res = lambda x: pkg_resources.resource_stream(__name__, x)


##########################
##### Monkeypatching #####
##########################

# HACK: Java has something called writeDelimitedTo that prepends size
#       as a varint, python api does not so we patch it in.

import google.protobuf.message

if not 'SerializeToSocket' in dir(google.protobuf.message.Message):
    import google.protobuf.internal.encoder
    import io


    def _monkey_wdelimit(self, soc):
        msg = self.SerializeToString()
        google.protobuf.internal.encoder._EncodeVarint(soc.send, len(msg))
        print "=================="
        print "Sending %s bytes of data" % len(msg)
        print self
        print "=================="
        soc.send(msg)

    google.protobuf.message.Message.SerializeToSocket = _monkey_wdelimit

if not 'ParseFromSocket' in dir(google.protobuf.message.Message):
    import google.protobuf.internal.decoder

    class SocketBuffer(object):
        def __init__(self, soc):
            self._buffer = io.BytesIO()
            self._soc = soc

        def __getitem__(self, pos):
            while len(self._buffer.getvalue()) < (pos + 1):
                self._buffer.write(self._soc.recv(1))
                self._soc.setblocking(1)
            return self._buffer.getvalue()[pos]

    def _monkey_pdelimit(self, soc):
        old = soc.gettimeout()
        buf = SocketBuffer(soc)
        nbytes, _ = google.protobuf.internal.decoder._DecodeVarint(buf, 0)
        print "=================="
        print "Receiving %d bytes of data." % nbytes
        soc.settimeout(0.0)
        msg = soc.recv(nbytes)
        soc.settimeout(old)
        self.ParseFromString(msg)
        print self
        print "=================="

    google.protobuf.message.Message.ParseFromSocket = _monkey_pdelimit


##########################
######### END ############
##########################

black = ( 0, 0, 0)
white = ( 255, 255, 255)
green = ( 0, 255, 0)
red = ( 255, 0, 0)
size=(600,600)

def set_grid(screen,clock,grid1):
    click_sound = pygame.mixer.Sound(res("resources/splash.wav"))
    click_sound = pygame.mixer.Sound("")
    image = pygame.image.load(res("resources/Battleships_start.png"))
    done = False
    while not done:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                done = True
            if event.type == pygame.MOUSEBUTTONDOWN:
                click_sound.play()
                pos = pygame.mouse.get_pos()
                grid1.grid_event(pos)
                print("Click ",pos,"Grid coordinates: ")
            if event.type == pygame.KEYDOWN:
                print("KEYDOWN")
                if event.key == pygame.K_UP:
                    grid1.set_direction(s.VERTICAL)
                if event.key == pygame.K_DOWN:
                    grid1.set_direction(s.HORIZONTAL)
                if event.key == pygame.K_RIGHT:
                    grid1.set_direction(s.HORIZONTAL)
                if event.key == pygame.K_LEFT:
                    grid1.set_direction(s.VERTICAL)

        # Set the screen background
        screen.fill(white)
        grid1.draw_grid()
        screen.blit(image, (110,110))
        grid1.draw_log()
        clock.tick(20)
        pygame.display.flip()

def set_grid_from_board(screen, grid1, grid2, board, other):
    for c, boat in enumerate(board.boats):
        x = boat.x
        y = boat.y
        
        if boat.direction == boat_protos_pb2.Board.Boat.RIGHT:
            for xn in xrange(boat.type):          
                grid1.grid[y][xn + x] = c
        elif boat.direction == boat_protos_pb2.Board.Boat.DOWN:
            for yn in xrange(boat.type):
                grid1.grid[yn + y][x] = c
        else:
            print "OOPS! Not implemented!"
               
    for co in board.cos:
        if co.hit:
            grid1.grid[co.y][co.x] = 6
            grid2.hitsTaken += 1
        else:
            grid1.grid[co.y][co.x] = 5 
        
    for co in other.cos:
        print co
        if co.hit:
            grid2.grid[co.x][co.y] = 6
            grid2.hitsGiven += 1
        else:
            grid2.grid[co.x][co.y] = 5
            

def play_game(screen,clock,soc,grid1,grid2):
    click_sound = pygame.mixer.Sound(res("resources/mortar.wav"))
    image = pygame.image.load(res('resources/Battleships_Paper_Game.png'))
    image = pygame.transform.scale(image, (size[0]-10,size[1]-10))
    done = False

    while not done:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                done=True
            if event.type == pygame.MOUSEBUTTONDOWN:
                click_sound.play()
                pos = pygame.mouse.get_pos()
                grid1.grid_event(pos)
                #print "Click ", pos, "Grid coordinates: "
        # Set the screen background
        msg = boat_protos_pb2.BaseMessage()
        try:
            soc.setblocking(0)
            msg.ParseFromSocket(soc)
            soc.setblocking(1)
            
            #grid1.clear_log()
            
            if msg.HasField("fire"):
                print >> grid1, "Taking fire!"
                x = msg.fire.x
                y = msg.fire.y
                if grid2.grid[x][y] >= 0:
                    grid2.grid[x][y] = 6
                else:
                    grid2.grid[x][y] = 5

            if msg.HasField("report"):
                if msg.report.hit:
                    print >> grid1, "You hit!"
                    grid1.hitsGiven += 1
                else:
                    grid1.flip_last(5)
                    print >> grid1, "You missed!"
                if msg.report.sunk:
                    print >> grid1, "Enemy ship sunk!"

            if msg.HasField("yourTurn") and msg.yourTurn:
                print >> grid1, "Make your move!"
                grid1.myturn = True

            if msg.HasField("endGame"):
                # Server has asked us to end the game.
                pass
            
        except socket.error:
            pass

        screen.fill(white)
        grid1.draw_grid()
        grid2.draw_grid()
        grid1.draw_log()
        grid2.draw_log()
        screen.blit(image,(2,2))
        clock.tick(20)
        pygame.display.flip()

def main(argv):
    parser = OptionParser(usage="%prog HOST PORT")

    (options, args) = parser.parse_args(argv)

    if len(args) != 3:
        parser.print_usage()
        print "Bad arguments!"
        return 1

    host = argv[1]
    port = int(argv[2])

    soc = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    soc.connect((host, port))


    pygame.mixer.init()
    pygame.init()
    screen=pygame.display.set_mode(size)
    pygame.display.set_caption("Uuigaz")
    clock = pygame.time.Clock()

    screen.fill(white)


    # Create Ident
    ident = boat_protos_pb2.Ident()
    ident.name = inputbox.input(screen)

    ident.SerializeToSocket(soc)

    # Parse Init
    init = boat_protos_pb2.Init()
    init.ParseFromSocket(soc)

    screen.fill(white)
    grid1 = placement_grid.Placement_grid(screen,29,29,1,132,160)
    grid2 = game_grid.Game_grid(screen,soc,33,33,1,250,250)
    
    if init.HasField("newGame"):
        set_grid(screen, clock, grid1)
        init.Clear()
        init.board.CopyFrom(grid1.get_msg())
        init.SerializeToSocket(soc)
    elif init.HasField("board"):
        print "BOARD ALREADY EXIST, PARSE IT INSTEAD."
        set_grid_from_board(screen, grid1, grid2, init.board, init.other)
    else:
        raise Exception("bad message received")
        return 1



    grid1.transform(22,22,1,23,23)
    play_game(screen,clock,soc,grid2,grid1)

    pygame.quit()
    return 0

if __name__ == '__main__':
    sys.exit(main(sys.argv))
