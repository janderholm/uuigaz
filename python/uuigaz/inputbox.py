#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys

import pygame, pygame.font, pygame.event, pygame.draw
from pygame.locals import *

import settings
import pygame.mixer



def draw_box(screen, message):
    pygame.draw.rect(screen, (0,0,0),
           ((screen.get_width() / 2) - 100,
            (screen.get_height() / 2) - 10,
            200,20), 0)
    pygame.draw.rect(screen, (255,255,255),
               ((screen.get_width() / 2) - 102,
                (screen.get_height() / 2) - 12,
                204,24), 1)
    fontobject=pygame.font.SysFont('Arial', 18)
    if len(message) != 0:
        screen.blit(fontobject.render(message, 1, (255, 255, 255)),
                ((screen.get_width() / 2) - 100, (screen.get_height() / 2) - 10))
    pygame.display.flip()


def input(screen):
    pygame.draw.rect(screen, (0,0,0),
           ((screen.get_width() / 2) - 100,
            (screen.get_height() / 2) - 10,
            200,20), 0)
    pygame.draw.rect(screen, (255,255,255),
               ((screen.get_width() / 2) - 102,
                (screen.get_height() / 2) - 12,
                204,24), 1)
    pygame.display.flip()
    done = False
    s = ""
    while done==False:
        event = pygame.event.wait()
        #pygame.event.clear()
        if event.type == pygame.QUIT:
            done=True
        if event.type == pygame.KEYDOWN:
            inkey = event.key
            if inkey == pygame.K_RETURN or inkey == pygame.K_KP_ENTER:
                done=True
                draw_box(screen,s)
            elif inkey <= 127:
                s += chr(inkey)
                draw_box(screen,s)
        #screen.fill(settings.white)
    return s

def main():
    pygame.init()
    screen = pygame.display.set_mode((600,600))
    pygame.display.set_caption("Uuigaz")
    input(screen)
    pygame.quit()
    return 0

if __name__ == '__main__':
    sys.exit(main())


