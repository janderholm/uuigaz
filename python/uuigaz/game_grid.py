import grid
import pygame
import pygame.mixer
import settings as s
import boat_protos_pb2

class Game_grid(grid.Grid):
    def __init__(self,screen,soc,cell_width,cell_height,grid_margin,x_offset,y_offset):
        grid.Grid.__init__(self, screen,cell_width,cell_height,grid_margin,x_offset,y_offset)
        self.direction = s.HORIZONTAL
        self.boats = [s.CARRIER,
                      s.BATTLESHIP,
                      s.CRUISER,
                      s.DESTROYER,
                      s.SUBMARINE]
        self.current_boat = 0;
        self.soc = soc;
        self.myturn = False
        self.hitsTaken = 0
        self.hitsGiven = 0
        x = screen.get_width() / 30
        y = screen.get_width() - (screen.get_width() / 2)
        self._centerx = False
        self.msg_coords = (x, y)

    def draw_grid(self):
         # Draw the grid
        for row in xrange(10):
            for column in xrange(10):
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
                elif nbr == 5:
                    color = s.sea
                pygame.draw.rect(self.screen,color,
                    [(self.grid_margin+self.cell_width)*column+self.grid_margin+self.x_offset,
                    (self.grid_margin+self.cell_height)*row+self.grid_margin+self.y_offset,
                    self.cell_width,
                    self.cell_height])

    def flip_last(self, color):
        """Change last set cell to color"""
        print "FLIP TO COLOR " + str(color)
        row, col = self.last
        self.grid[row][col] = color
        

    #abstract method, position validation is done in superclass
    def _event(self,row,col):
        if self.myturn:
            if self.grid[row][col] >= 0:
                # Already fired
                print >> self, "Already fired there"
                return
            self.last = (row, col)
            self.myturn = False
            fire = boat_protos_pb2.Fire()
            fire.x = row
            fire.y = col
            self.grid[row][col] = 0
            msg = boat_protos_pb2.BaseMessage()
            msg.fire.CopyFrom(fire)
            msg.SerializeToSocket(self.soc)
        else:
            print >> self, "Not my turn"

    def get_msg(self):
        return self._basemsg
