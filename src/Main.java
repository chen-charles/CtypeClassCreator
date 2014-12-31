/*
C-type Class Creator
by Charles-Jianye Chen

EVERYTHING IS PUBLIC
KEEP THE CLASS BUFFER FILES IN CASE YOU WANT TO DERIVE FROM IT OR EDIT IT

OBJECTS ARE POINTERS, CALL CALLERS WITH THE POINTER AND THE PARAMS TO CALL A METHOD
MALLOC WILL BE CALLED DUE TO THE CREATION OF VARIABLE SPACE
CLASSNAME WILL BE USED TO INIT THE VARIABLE SPACE (RETURNING A *THIS POINTER)
USERS MUST EXPLICITILY CALL THE INITIALIZERS

Calling to a method: CLASSNAME_METHODNAME(*THIS, ARG1, ...)
Fetching a variable: THIS->VARNAME

Additional String Replacer is provided.
This replacer only serves simple purposes.  It will not try to keep track of anything else unless being asked to do so.
It is NOT checking line breaks.
Syntax:
...
...
// CLASSNAME 141225 POINTERNAME
//the line above is NECESSARY to indicate the operations, it must be in exactly the same format
//that is
//"//" + one space + "CLASSNAME" + one space + "141225"(magic) + one space + "POINTERNAME" + "\n"
...
...
POINTERNAME.METHODNAME(ARG1, ...)
POINTERNAME->VARIABLE
...
...
 */

import java.util.*;
import java.io.*;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        if (args.length != 0)
        {
            if (args[0].equals("-c") && args.length > 2)    //script-ed input   //java Main -c SCRIPTNAME
            {
                CtypeClassCreatorConsole cccc = new CtypeClassCreatorConsole();
                DataInputStream _bufin = new DataInputStream(new BufferedInputStream(new FileInputStream(args[1])));
                Scanner sc = new Scanner(_bufin);
                while (sc.hasNextLine())
                {
                    cccc.exec(sc.nextLine());
                    System.out.println(cccc.getLastResult());
                }
                cccc.exit();
                return;
            }
            else if (args[0].equals("-r") && args.length > 2)    //java Main -r CLASSNAME FILENAME
            {
                CtypeClassObject cco = CtypeClassCreatorConsole.inClass(args[1]);
                // figure out String s that needs attention
                String op = String.format("// %s 141225 ", cco.name);
                String POINTERNAME = null;
                HashMap<String, String> dict = new HashMap<String, String>();

                OutputStreamWriter out = new OutputStreamWriter(new DataOutputStream(new BufferedOutputStream(
                        new FileOutputStream(args[2]+".out"))));
                Scanner in = new Scanner(new BufferedInputStream(new FileInputStream(args[2])));

                while (in.hasNextLine())
                {
                    String line = in.nextLine();
                    if (line.contains(op) && line.indexOf(op) == 0)
                    {
                        POINTERNAME = line.replaceFirst(op, "").trim();
                        if (POINTERNAME.contains(" "))
                        {
                            System.out.println("Invalid Descriptor is Detected.  Exit.  ");
                            return;
                        }
                        dict.clear();   //everytime a new desc is detected, re-generate replacing information
                        for (Variable i: cco.var)
                        {
                            dict.put(String.format("%s.%s", POINTERNAME, i.name),
                                    String.format("(%s->%s)", POINTERNAME, i.name));
                        }
                        for (Method i: cco.method)
                        {
                            dict.put(String.format("%s.%s(", POINTERNAME, i.name),
                                    String.format("%s_%s(%s, ", cco.name, i.name, POINTERNAME));
                        }
                    }

                    if (POINTERNAME != null)
                        for (String i: dict.keySet())
                        {
                            line = line.replace(i, dict.get(i));
                        }
                    out.write(line+"\n");
                }
                out.close();
                System.out.println(".out output file successfully generated.  ");
                return;
            }
            else
            {
                System.out.println("Param Invalid.  ");
                return;
            }
        }
        CtypeClassCreatorConsole cccc = new CtypeClassCreatorConsole();
        while(cccc.next());

    }
}

class CtypeClassCreatorConsole extends Console
{
    public CtypeClassObject cco = new CtypeClassObject();

    public CtypeClassObject getCtypeClassObject()
    {
        return this.cco;
    }

    public int _new(String param)
    {
        //new (method/var) (type/returntype)  name [type name]...
        String[] args =  param.split(" ");
        if (args[0].equals("method"))
        {
            ArrayList<Variable> arr = new ArrayList<Variable>();
            for (int i = 3; i < args.length; i += 2)
            {
                arr.add(new Variable(args[i], args[i+1]));
            }
            cco.method(new Method(args[1], args[2], arr));
        }
        else if (args[0].equals("var"))
        {
            cco.var(new Variable(args[1], args[2]));
        }
        else
        {
            Exception();
            return CMD_RETURN_VOID;
        }

        return CMD_SUCCESS;
    }

    public int remove(String param)
    {
        String[] args = param.split(" ");
        if (args[0].equals("method"))
        {
            //args[1]: type; args[2]: name;
            ArrayList<Method> arr = new ArrayList<Method>();
            for (Method i: cco.method)
            {
                if (i.type.equals(args[1]) && i.name.equals(args[2])) arr.add(i);
            }
            for (Method i: arr) cco.method.remove(i);
        }
        else if (args[0].equals("var"))
        {
            ArrayList<Variable> arr = new ArrayList<Variable>();
            for (Variable i: cco.var)
            {
                if (i.type.equals(args[1]) && i.name.equals(args[2])) arr.add(i);
            }
            for (Variable i: arr) cco.var.remove(i);
        }
        else
        {
            int t;
            try
            {
                t = Integer.parseInt(args[0]);
            }
            catch (Exception err)
            {
                Exception(err.toString());
                return CMD_RETURN_VOID;
            }
            for (Variable i: cco.var)
            {
                if (i.uID == t)
                {
                    cco.var.remove(i);
                    break;
                }
            }
            for (Method i: cco.method)
            {
                if (i.uID == t)
                {
                    cco.method.remove(i);
                    break;
                }
            }
        }
        return CMD_SUCCESS;
    }

    public int print(String param)
    {
        //print (method/var) name
        String[] args = param.split(" ");
        if (args[0].equals("method"))
        {
            for (Method i: cco.method)
            {
                if (i.name.equals(args[1]))
                {
                    System.out.println(i);
                }
            }
        }
        else if (args[0].equals("var"))
        {
            for (Variable i: cco.var)
            {
                if (i.name.equals(args[1]))
                {
                    System.out.println(i);
                }
            }
        }
        else
        {
            Exception();
            return CMD_RETURN_VOID;
        }

        return CMD_SUCCESS;
    }

    public int out(String param)
    {
        //out filename
        String[] args = param.split(" ");
        String filename = args[0];
        cco.rename(filename);

        OutputStreamWriter outH = null;
        OutputStreamWriter outC = null;
        ObjectOutputStream bufout = null;
        try
        {
            outC = new OutputStreamWriter(new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename + ".c"))));
            outH = new OutputStreamWriter(new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename + ".h"))));
            bufout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename+".buffer")));

            outH.write(cco.generateH());
            outC.write(cco.generateC());
            bufout.writeObject(cco);

            outH.close();
            outC.close();
            bufout.close();
        }
        catch (FileNotFoundException err)
        {
            Exception(err.toString());
            return CMD_RETURN_VOID;
        }
        catch (IOException err)
        {
            Exception(err.toString());
            return CMD_RETURN_VOID;
        }

        return CMD_SUCCESS;
    }

    public static CtypeClassObject inClass(String classname) throws Exception
    {
        ObjectInputStream bufin = null;
        CtypeClassObject cco = null;
        bufin = new ObjectInputStream(new BufferedInputStream(new FileInputStream(classname + ".buffer")));
        cco = (CtypeClassObject)bufin.readObject();
        return cco;
    }

    public int derive(String param)
    {
        cco.dependency.add(param);
        return CMD_SUCCESS;
    }

    public int in(String param) throws Exception
    {
        //in filename
        String[] args = param.split(" ");
        this.cco = inClass(args[0]);

        return CMD_SUCCESS;
    }

    public boolean exec(String s)
    {
        return inputHandler(s);
    }

    protected int inputHandler(String cmd, String param)
    {
        int result = super.inputHandler(cmd, param);
        if (result != CMD_NOT_HANDLED) return result;

        result = CMD_RETURN_VOID;
        try
        {
            if (cmd.equals("out"))
            {
                result = out(param);
            } else if (cmd.equals("in"))
            {
                result = in(param);
            } else if (cmd.equals("new"))
            {
                result = _new(param);
            } else if (cmd.equals("remove"))
            {
                result = remove(param);
            } else if (cmd.equals("print"))
            {
                result = print(param);
            } else if (cmd.equals("derive"))
            {
                result = derive(param);
            } else if (cmd.equals("display"))
            {
                System.out.println("VARIABLES DECLARED: ");
                for (Variable i : cco.var) System.out.println("\t" + i);
                System.out.println("\nMETHODS DECLARED: ");
                for (Method i : cco.method) System.out.println("\t" + i);
                System.out.println();
                result = CMD_SUCCESS;
            } else if (cmd.equals("exit"))
            {
                result = exit(param);
            } else if (cmd.equals("help"))
            {
                result = CMD_SUCCESS;
                System.out.println("in/out [classname] - BufferIO");
                System.out.println("derive [headername(with path is ok)] - Derive from an existing header");

                System.out.println("print [method/var] [name] - display specified method/var info");
                System.out.println("display - display all method/var info");

                //notify and ask for confirmation for any overlaps
                System.out.println("new method [type] [name] [typ_param1] [param1] ... - declare a new method");
                System.out.println("new var [type] [name] - declare a new var");

                System.out.println("remove [method/var] [type] [name] - remove ALL method(s)/var(s) with provided conditions");

                System.out.println("remove [uID] - remove a specified method/var by their unique ID");

                System.out.println("exit - exit console");
                System.out.println();
            } else
            {
                return CMD_NOT_HANDLED;  //not handled
            }
        }
        catch (Exception err)
        {
            Exception(err.toString());
        }
        return result;
    }
}