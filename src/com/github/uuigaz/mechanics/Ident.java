package com.github.uuigaz.mechanics;

import com.github.uuigaz.messages.BoatProtos;

/**
 * Used for identifying clients in a easy way.
 */
public class Ident
{
    private final BoatProtos.Ident ident;

    private Ident(BoatProtos.Ident ident)
    {
        this.ident = ident;
    }

    public BoatProtos.Ident getMsg()
    {
        return ident;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        } else if (o instanceof Ident) {
            return ((Ident) o).ident.getName().equals(ident.getName());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return ident.getName().hashCode();
    }

    @Override
    public String toString()
    {
        return ident.getName();
    }

    public static Ident build(BoatProtos.Ident ident)
    {
        return new Ident(ident);
    }

    public static Ident build(String name)
    {
        BoatProtos.Ident.Builder ident = BoatProtos.Ident.newBuilder();
        ident.setName(name);
        return new Ident(ident.build());
    }
}
