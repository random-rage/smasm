package ru.rage.smasm;

public class Data
{
    private String _name;
    private int _val;
    private int _len;

    public Data(String name, String val)
    {
        _name = name;
        _val = (val.matches("'.'")) ? val.codePointAt(1) : Integer.parseInt(val);
        _len = 1;
    }

    public Data(String name, String val, String len)
    {
        this(name, val);
        _len = Integer.parseInt(len);
    }

    public String getName()
    {
        return _name;
    }

    public int getValue()
    {
        return _val;
    }

    public int getLength()
    {
        return _len;
    }
}
