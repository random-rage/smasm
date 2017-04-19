package ru.rage.smasm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Translator
{
    private static final int DATA_MEMORY_SIZE = 256;
    private static final int MIN_MEMORY_VALUE = 0;

    private List<Command> _cmds;
    private List<Data> _data;
    private HashMap<String, Integer> _labels;
    private int _programSize;

    Translator(List<Command> cmds, List<Data> data)
    {
        _programSize = 0;
        _cmds = cmds;
        _data = data;
    }

    private int findData(String name) {
        if (_data != null) {
            for (int i = 0; i < _data.size(); i++) {
                if (_data.get(i).getName().equals(name))
                    return i;
            }
        }
        return -1;
    }

    public void analyze() throws Exception
    {
        if (_data != null) {
            int freeMem = DATA_MEMORY_SIZE;
            for (Data d : _data) {
                freeMem -= d.getLength();
                if (freeMem < 0)
                    throw new Exception("Not enough memory for data \"" + d.getName() + "\"");
                if (d.getValue() > 255 || d.getValue() < 0)
                    throw new Exception("Data value must be in range [0..255] \"" + d.getName() + "\"");
                if (d.getName().contains("[") || d.getName().contains("]"))
                    throw new Exception("Data name contains illegal chars\"" + d.getName() + "\"");
            }
        }
        Command cmd;
        for (int i = 0; i < _cmds.size(); i++) {
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
                    _programSize++;
                    break;
                case JMP:
                    if ((cmd.getArg().getType() == ArgType.NONE) ||
                        (cmd.getArg().getType() == ArgType.CHAR))
                        throw new Exception("Only DATA or IMM argument allowed in \"" + cmd + "\"");
                    if (cmd.getArg().getType() == ArgType.DATA) {
                        int d = findData(cmd.getArg().toString());
                        Integer addr = _labels.get(cmd.getArg().toString());
                        if (addr != null)
                        {
                            cmd.setArg(new Argument(addr, ArgType.IMMEDIATE));
                            break;
                        }
                        else if (d > -1)
                        {
                            cmd.setArg(new Argument(d, ArgType.INDIRECT));
                            break;
                        }
                        throw new Exception("Label or data \"" + cmd.getArg() + "\" not found");
                    }
                    _programSize += 2;
                    break;
                case STR:
                    if ((cmd.getArg().getType() != ArgType.DATA) &&
                        (cmd.getArg().getType() != ArgType.INDIRECT))
                        throw new Exception("Only DATA or INDIRECT argument allowed in \"" + cmd + "\"");
                    if (cmd.getArg().getType() == ArgType.DATA) {
                        int d = findData(cmd.getArg().toString());
                        if (d > -1)
                            cmd.setArg(new Argument(d, ArgType.INDIRECT));
                        else
                            throw new Exception("Data \"" + cmd.getArg() + "\" not found");
                    }
                    _programSize += 2;
                    break;
                case ADD:
                case SUB:
                case LDR:
                    if (cmd.getArg().getType() == ArgType.NONE)
                        throw new Exception("Argument must be present in \"" + cmd + "\"");
                    if (cmd.getArg().getType() == ArgType.CHAR)
                        cmd.getArg().setType(ArgType.IMMEDIATE);
                    else if (cmd.getArg().getType() == ArgType.DATA) {
                        int d = findData(cmd.getArg().toString());
                        if (d > -1)
                            cmd.setArg(new Argument(d, ArgType.INDIRECT));
                        else
                            throw new Exception("Data \"" + cmd.getArg() + "\" not found");
                    }
                    _programSize += 2;
                    break;
            }
        }
    }

    public byte[] getData()
    {
        byte[] data = new byte[_data.size()];
        int i = 0;
        for (Data d: _data)
        {
            for (int j = 0; j < d.getLength(); j++)
                data[i++] = (byte)d.getValue();
        }
        return data;
    }

    public byte[] compile()
    {
        byte[] program = new byte[_programSize];
        int i = 0;
        for (Command cmd: _cmds)
        {
            program[i++] = (byte)cmd.getOpcode();
            if (cmd.getArg().getType() != ArgType.NONE)
                program[i++] = (byte)cmd.getArg().getValue();
        }
        return program;
    }
}
