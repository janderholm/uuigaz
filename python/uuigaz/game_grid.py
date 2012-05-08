import grid
import pygame
import pygame.mixer
import settings as s
import boat_protos_pb2

class Game_grid(grid.Grid):
    def __init__(self,screen,cell_width,cell_height,grid_margin,x_offset,y_offset):
    	grid.Grid.__init__(self, screen,cell_width,cell_height,grid_margin,x_offset,y_offset)
        self.direction = s.HORIZONTAL
        self.boats = [s.CARRIER, s.BATTLESHIP, s.CRUISER, s.DESTROYER,s.SUBMARINE]
        self.current_boat = 0;
        self._basemsg = boat_protos_pb2.BaseMessage()

    def draw_grid(self):
         # Draw the grid
        for row in range(10):
            for column in range(10):
                color = s.white
                nbr = self.grid[row][column]
                if nbr == 0:
                    color = s.black
                elif nbr == 1:
                    color = s.blue
                elif nbr == 2:
                    color = s.red
                elif nbr == 3:
                    color = s.green
                elif nbr == 4:
                    color = s.gold
                pygame.draw.rect(self.screen,color,
                    [(self.grid_margin+self.cell_width)*column+self.grid_margin+self.x_offset,
                    (self.grid_margin+self.cell_height)*row+self.grid_margin+self.y_offset,
                    self.cell_width,
                    self.cell_height])

    #abstract method, position validation is done in superclass
    def _event(self,row,col):
        fire = boat_protos_pb2.Fire()
        fire.x = row
        fire.y = col
        self._basemsg.fire.CopyFrom(fire)
        self.grid[row][col] = 0

    def get_msg(self):
        return self._basemsg
        

        
