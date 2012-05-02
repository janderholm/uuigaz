#! /usr/bin/python2.7 -tt
# -*- coding: utf-8 -*-

import pygame
# Define some colors
black = ( 0, 0, 0)
white = ( 255, 255, 255)
green = ( 0, 255, 0)
red = ( 255, 0, 0)

class Grid:
    def __init__(self,screen,cell_width,cell_height,grid_margin,x_offset,y_offset):
        self.screen = screen
        self.grid=[]
        self.cell_width=cell_width
        self.cell_height=cell_height
        self.grid_margin=grid_margin
        self.x_offset=x_offset
        self.y_offset=y_offset
        for row in range(10):
            # Add an empty array that will hold each cell
            # in this row
            self.grid.append([])
            for column in range(10):
                self.grid[row].append(0) # Append a cell

    def draw_grid(self):
         # Draw the grid
        for row in range(10):
            for column in range(10):
                color = white
                if self.grid[row][column] == 1:
                    color = red
                pygame.draw.rect(self.screen,color,
                    [(self.grid_margin+self.cell_width)*column+self.grid_margin+self.x_offset,
                    (self.grid_margin+self.cell_height)*row+self.grid_margin+self.y_offset,
                    self.cell_width,
                    self.cell_height])

    def grid_event(self,pos):
        if pos[0] < self.x_offset or pos[1] < self.y_offset:
            return
        posx = pos[0]-self.x_offset
        posy = pos[1]-self.y_offset
        if posx < 0:
            posx = 0;
        if posy < 0:
            posy = 0;
        # Change the x/y screen coordinates to grid coordinates
        column = posx // (self.cell_width+self.grid_margin)
        row = posy // (self.cell_height+self.grid_margin)
        # Sete t hat location to zero
        if row < 10 and column < 10:
            self.grid[row][column]=1
