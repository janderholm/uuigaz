#! /usr/bin/python2.7 -tt
# -*- coding: utf-8 -*-

import pygame
import pygame.mixer
import grid

black = ( 0, 0, 0)
white = ( 255, 255, 255)
green = ( 0, 255, 0)
red = ( 255, 0, 0)

def main():
    pygame.mixer.init()
    click_sound = pygame.mixer.Sound("bomb3.wav")

    size=(600,600)
    pygame.init()

    screen=pygame.display.set_mode(size)
    image = pygame.image.load('Battleships_Paper_Game.png')
    image = pygame.transform.scale(image, (size[0]-10,size[1]-10))
    pygame.display.set_caption("Uuigaz")
    #Loop until the user clicks the close button.
    done=False
    # Used to manage how fast the screen updates
    clock=pygame.time.Clock()

    grid1 = grid.Grid(screen,22,22,1,24,24)
    grid2 = grid.Grid(screen,33,33,1,250,250)

    # -------- Main Program Loop -----------
    while done==False:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                done=True
            if event.type == pygame.MOUSEBUTTONDOWN:
                click_sound.play()
                pos = pygame.mouse.get_pos()
                grid1.grid_event(pos)
                grid2.grid_event(pos)

                print("Click ",pos,"Grid coordinates: ")
        # Set the screen background
        screen.fill(white)

        grid1.draw_grid()
        grid2.draw_grid()

        screen.blit(image,(2,2))
        
        clock.tick(20)
        pygame.display.flip()

    pygame.quit()
main()