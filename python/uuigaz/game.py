#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import socket

import pygame
import inputbox
import pygame.mixer

import boat_protos_pb2
import placement_grid 
import grid
import settings as s

from optparse import OptionParser


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
        soc.send(msg)

    google.protobuf.message.Message.SerializeToSocket = _monkey_wdelimit
    
if not 'ParseFromSocket' in dir(google.protobuf.message.Message):
    import google.protobuf.internal.decoder

    class SocketBuffer(object):
        def __init__(self, soc):
            self._buffer = io.BytesIO()
            self._soc = soc
        
        def __getitem__(self, pos):
            print "get " + str(pos)
            while len(self._buffer.getvalue()) < (pos + 1):
                print "get " + str(pos)
                self._buffer.write(self._soc.recv(1))
            print "done getting"
            return self._buffer.getvalue()[pos]
            
    def _monkey_pdelimit(self, soc):
        buf = SocketBuffer(soc)
        print "parsing"
        bytes, _ = google.protobuf.internal.decoder._DecodeVarint(buf, 0)
        print "ZOMG IT PARSED: " + str(bytes)
        msg = soc.recv(bytes)
        return self.ParseFromString(msg)

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
    click_sound = pygame.mixer.Sound("resources/mortar.wav")
    image = pygame.image.load('resources/Battleships_start.png')
    done = False
    while done==False:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                done=True
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

def play_game(screen,clock,grid1,grid2):
    click_sound = pygame.mixer.Sound("resources/bomb3.wav")
    image = pygame.image.load('resources/Battleships_Paper_Game.png')
    image = pygame.transform.scale(image, (size[0]-10,size[1]-10))
    done = False
    while done==False:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                done=True
            if event.type == pygame.MOUSEBUTTONDOWN:
                click_sound.play()
                pos = pygame.mouse.get_pos()
                grid1.grid_event(pos)
                print("Click ",pos,"Grid coordinates: ")
        # Set the screen background
        screen.fill(white)
        grid1.draw_grid()
        grid2.draw_grid()
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
    
    print init
    

    screen.fill(white)
    grid1 = placement_grid.Placement_grid(screen,29,29,1,132,160)
    grid2 = grid.Grid(screen,33,33,1,250,250)

    set_grid(screen, clock, grid1)
    grid1.get_msg()
    
    grid1.transform(22,22,1,23,23)
    play_game(screen,clock,grid2,grid1)

    pygame.quit()
    return 0

if __name__ == '__main__':
    sys.exit(main(sys.argv))
