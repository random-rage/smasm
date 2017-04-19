package ru.rage.smasm;

enum ArgType
{
    NONE,
    INDIRECT,
    IMMEDIATE,
    CHAR,
    DATA
}

public class Argument
{
    private ArgType _type;
    private String _arg;
    private int _val;

    public Argument(String arg)
    {
        if (arg == null || arg.length() == 0)
        {
            _type = ArgType.NONE;
            _arg = "";
            _val = -1;
        }
        else if (arg.matches("\\[.+\\]"))
        {
            _type = ArgType.INDIRECT;
            _arg = arg.substring(1, arg.length() - 1);
            _val = Integer.parseInt(_arg);
        }
        else if (arg.matches("'.'"))
        {
            _type = ArgType.CHAR;
            _arg = arg.substring(1, arg.length() - 1);
            _val = _arg.codePointAt(0);
        }
        else if (arg.matches("[0-9]+"))
        {
            _type = ArgType.IMMEDIATE;
            _arg = arg;
            _val = Integer.parseInt(_arg);
        }
        else
        {
            _type = ArgType.DATA;
            _arg = arg;
            _val = -1;
        }
    }

    public Argument(int val, ArgType type)
    {
        _type = type;
        _arg = Integer.toString(val);
        _val = val;
    }

    ArgType getType()
    {
        return _type;
    }

    void setType(ArgType newType)
    {
        _type = newType;
    }

    @Override
    public String toString()
    {
        return _arg;
    }

    int getValue()
    {
        return _val;
    }
}
