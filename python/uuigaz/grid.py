# -*- coding: utf-8 -*-

import pygame
import pygame.font
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
        self.msg_coords = (10, 10)
        self._log = ''
        for row in range(10):
            # Add an empty array that will hold each cell
            # in this row
            self.grid.append([])
            for column in range(10):
                self.grid[row].append(-1) # Append a cell

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
        posx = pos[0]-self.x_offset
        posy = pos[1]-self.y_offset
        if pos[0] < self.x_offset or pos[1] < self.y_offset:
            return
        if posx > (self.cell_width+self.grid_margin)*10:
            return
        if posy > (self.cell_height+self.grid_margin)*10:
            return

        if posx < 0:
            posx = 0
        if posy < 0:
            posy = 0
        # Change the x/y screen coordinates to grid coordinates
        column = posx // (self.cell_width+self.grid_margin)
        row = posy // (self.cell_height+self.grid_margin)
        self._event(row,column)

    def _event(self,row,col):
        assert False, "Not implemented"

    def transform(self,cell_width,cell_height,grid_margin,x_offset,y_offset):
        self.cell_width = cell_width
        self.cell_height = cell_height
        self.grid_margin = grid_margin
        self.x_offset = x_offset
        self.y_offset = y_offset

    def get_grid(self):
        return self.grid

    def set_grid(self,new_grid):
        self.grid = new_grid

    def write(self, msg):
        # XXX: IT'S A TRAP!
        self._log += msg

    def clear_log(self):
        self._log = ''

    def draw_log(self):
        lines = self._log.splitlines()
        while len(lines) > 8:
            lines.pop(0)
        self._log = '\n'.join(lines)
        self._log += '\n'
        print self._log
        for i, l in enumerate(lines):
            font = pygame.font.Font(None, 28)
            text = font.render(l,  True, (10, 10, 10))
            x, y = self.msg_coords
            y += i*28
            if self._centerx:
                textpos = text.get_rect(centerx=x, centery=y)
            else:
                textpos = text.get_rect(x=x, centery=y)
            self.screen.blit(text, textpos)
