
import grid
import pygame
import pygame.mixer
import settings as s
import boat_protos_pb2

class Placement_grid(grid.Grid):
    def __init__(self,screen,cell_width,cell_height,grid_margin,x_offset,y_offset):
    	grid.Grid.__init__(self, screen,cell_width,cell_height,grid_margin,x_offset,y_offset)
        self.direction = s.HORIZONTAL
        self.boats = [s.CARRIER, s.BATTLESHIP, s.CRUISER, s.DESTROYER,s.SUBMARINE]
        self.current_boat = 0;
        self._boardmsg = boat_protos_pb2.Board()
        x = screen.get_width() / 2
        y = screen.get_width() - (screen.get_width() / 7)
        self.msg_coords = (x, y)
        
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
        if self.current_boat == len(self.boats):
            return
        bt = self.boats[self.current_boat]
        self.clear_log()

        direction = self.direction
        print(row," ",col)

        if direction == s.HORIZONTAL and col + bt[1] <= 10:
            for i in range(0,bt[1]):
                if self.grid[row][col+i] != -1:
                    self.clear_log()
                    print >> self,  "Boats can't overlap"
                    return
            boat = self._boardmsg.boats.add()
            boat.x = row
            boat.y = col
            boat.direction = boat_protos_pb2.Board.Boat.RIGHT
            boat.type = bt[1]
            self.clear_log()

            for i in range(0,bt[1]):
                self.grid[row][col+i] = bt[0]

        elif direction == s.VERTICAL and row + bt[1] <= 10:
            for i in range(0,bt[1]):
                if self.grid[row+i][col] != -1:
                    self.clear_log()
                    print >> self, "Boats can't overlap"
                    return
            boat = self._boardmsg.boats.add()
            boat.x = row
            boat.y = col
            boat.direction = boat_protos_pb2.Board.Boat.DOWN
            boat.type = bt[1]
            self.clear_log()
           
            for i in range(0,bt[1]):
                self.grid[row+i][col] = bt[0]
        else:
            self.clear_log()
            print >> self, "Can't place boat there"
            return

        self.current_boat+=1

    def set_direction(self,direction):
        self.direction = direction;

    def get_msg(self):
        return self._boardmsg
        

        
