package ru.rage.smasm;

import ru.rage.spoml.*;

import java.util.HashMap;
import java.util.List;

class Translator
{
    private List<Include> _includes;
    private List<Data>    _data;
    private List<Command> _cmds;
    private List<Extern>  _externs;

    Translator(List<Include> includes, List<Data> data, List<Command> cmds, List<Extern> externs)
    {
        _includes = includes;
        _data = data;
        _cmds = cmds;
        _externs = externs;
    }

    private int findData(String name)
    {
        for (int i = 0; i < _data.size(); i++)
        {
            if (_data.get(i).getName().equals(name))
                return i;
        }
        return -1;
    }

    void compile() throws Exception
    {
        HashMap<String, Integer> _labels = new HashMap<>();
        if (hasData())
        {
            for (Data d : _data)
            {
                if (d.getName().contains("[") || d.getName().contains("]"))
                    throw new Exception("Data name contains illegal chars\"" + d.getName() + "\"");
            }
        }
        Command cmd;
        for (int i = 0; i < _cmds.size(); i++)
        {
            cmd = _cmds.get(i);

            if (cmd.hasLabel())
                _labels.put(cmd.getLabel(), i);

            switch (cmd.getType())
            {
                case NOP:
                case INC:
                case DEC:
                case IN:
                case OUT:
                case IFZ:
                case IFN:
                case HLT:
                    if (cmd.getArg().getType() != ArgType.NONE)
                        throw new Exception("Argument \"" + cmd.getArg() + "\" not allowed in \"" + cmd + "\"");
                    break;
                case JMP:
                    if ((cmd.getArg().getType() == ArgType.NONE) ||
                        (cmd.getArg().getType() == ArgType.CHAR))
                        throw new Exception("Only DATA or IMM argument allowed in \"" + cmd + "\"");
                    if (cmd.getArg().getType() == ArgType.DATA)
                    {
                        Integer addr = _labels.get(cmd.getArg().toString());
                        if (addr != null)
                        {
                            cmd.setArg(new Argument(addr, ArgType.IMMEDIATE));
                            break;
                        }
                        else
                        {
                            int d = findData(cmd.getArg().toString());
                            if (d > -1)
                            {
                                cmd.setArg(new Argument(d, ArgType.INDIRECT));
                                break;
                            }
                        }
                        throw new Exception("Label or data \"" + cmd.getArg() + "\" not found");
                    }
                    break;
                case STR:
                    if ((cmd.getArg().getType() != ArgType.DATA) &&
                        (cmd.getArg().getType() != ArgType.INDIRECT))
                        throw new Exception("Only DATA or INDIRECT argument allowed in \"" + cmd + "\"");
                    if (cmd.getArg().getType() == ArgType.DATA)
                    {
                        int d = findData(cmd.getArg().toString());
                        if (d > -1)
                            cmd.setArg(new Argument(d, ArgType.INDIRECT));
                        else
                            throw new Exception("Data \"" + cmd.getArg() + "\" not found");
                    }
                    break;
                case ADD:
                case SUB:
                case LDR:
                    if (cmd.getArg().getType() == ArgType.NONE)
                        throw new Exception("Argument must be present in \"" + cmd + "\"");
                    if (cmd.getArg().getType() == ArgType.CHAR)
                        cmd.getArg().setType(ArgType.IMMEDIATE);
                    else if (cmd.getArg().getType() == ArgType.DATA)
                    {
                        int d = findData(cmd.getArg().toString());
                        if (d > -1)
                            cmd.setArg(new Argument(d, ArgType.INDIRECT));
                        else
                            throw new Exception("Data \"" + cmd.getArg() + "\" not found");
                    }
                    break;
            }
        }
    }

    boolean hasData()
    {
        return !_data.isEmpty();
    }
    boolean hasCode()
    {
        return !_cmds.isEmpty();
    }
    boolean hasIncludes()
    {
        return !_includes.isEmpty();
    }
    boolean hasExterns()
    {
        return !_externs.isEmpty();
    }

    List<Command> getCode()
    {
        return _cmds;
    }
    List<Data> getData()
    {
        return _data;
    }
    List<Include> getIncludes()
    {
        return _includes;
    }
    List<Extern> getExterns()
    {
        return _externs;
    }
}
