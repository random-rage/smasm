package ru.rage.smasm;

enum CmdType
{
    NOP,
    ADD,
    SUB,
    INC,
    DEC,
    LDR,
    STR,
    IN,
    OUT,
    JMP,
    IFZ,
    IFN,
    HLT
}

public class Command
{
    private CmdType _type;
    private Argument _arg;
    private String _label;

    Command(CmdType type, Argument arg)
    {
        _type = type;
        _arg = arg;
        _label = null;
    }

    Command(CmdType type, Argument arg, String label)
    {
        this(type, arg);
        _label = label;
    }

    private static int getCode(CmdType type)
    {
        switch (type) {
            case ADD:
                return 1;
            case SUB:
                return 2;
            case INC:
                return 3;
            case DEC:
                return 4;
            case LDR:
                return 5;
            case STR:
                return 6;
            case IN:
                return 7;
            case OUT:
                return 8;
            case JMP:
                return 9;
            case IFZ:
                return 10;
            case IFN:
                return 11;
            case HLT:
                return 12;
            default:
                return 0;
        }
    }

    int getOpcode()
    {
        int opcode = getCode(_type);

        if (_arg.getType() == ArgType.IMMEDIATE)
            opcode |= 0x10;

        return opcode;
    }

    Argument getArg()
    {
        return _arg;
    }

    void setArg(Argument arg)
    {
        _arg = arg;
    }

    boolean hasLabel()
    {
        return _label != null;
    }

    String getLabel()
    {
        return _label;
    }

    CmdType getType()
    {
        return _type;
    }

    @Override
    public String toString()
    {
        return _type.toString();
    }
}
