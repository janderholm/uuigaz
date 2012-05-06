#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys

import pygame
import pygame.mixer
import placement_grid 
import grid
import settings as s

black = ( 0, 0, 0)
white = ( 255, 255, 255)
green = ( 0, 255, 0)
red = ( 255, 0, 0)
size=(600,600)

def set_grid(screen,clock,grid1):
    click_sound = pygame.mixer.Sound("resources/bomb3.wav")
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

def main():
    pygame.mixer.init()
    pygame.init()
    screen=pygame.display.set_mode(size)
    pygame.display.set_caption("Uuigaz")
    clock = pygame.time.Clock()

    screen.fill(white)
    grid1 = placement_grid.Placement_grid(screen,29,29,1,132,160)
    grid2 = grid.Grid(screen,33,33,1,250,250)

    set_grid(screen, clock, grid1)
    
    grid1.transform(22,22,1,23,23)
    play_game(screen,clock,grid2,grid1)

    pygame.quit()
    return 0

if __name__ == '__main__':
    sys.exit(main())
